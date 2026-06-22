package com.example.banco_digital.integration;

import com.example.banco_digital.TestcontainersConfiguration;
import com.example.banco_digital.dto.request.ClienteRequest;
import com.example.banco_digital.dto.request.ContaRequest;
import com.example.banco_digital.dto.request.TransferenciaRequest;
import com.example.banco_digital.entity.StatusTransacao;
import com.example.banco_digital.entity.TipoMovimento;
import com.example.banco_digital.helper.ClienteFactory;
import com.example.banco_digital.helper.TransferenciaApiClient;
import com.example.banco_digital.repository.ContaRepository;
import com.example.banco_digital.repository.HistoricoRepository;
import com.example.banco_digital.repository.TransacaoRepository;
import com.example.banco_digital.service.ClienteService;
import com.example.banco_digital.service.ContaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class TransferenciaConcorrenciaTest {

    private static final int NUM_THREADS = 20;
    private static final int TRANSFERENCIAS_POSSIVEIS = 10;
    private static final BigDecimal VALOR = new BigDecimal("100.00");

    @Autowired
    private MockMvc rest;
    @Autowired
    private JsonMapper mapper;
    @Autowired
    private ClienteService clienteService;
    @Autowired
    private ContaService contaService;
    @Autowired
    private ContaRepository contaRepository;
    @Autowired
    private TransacaoRepository transacaoRepository;
    @Autowired
    private HistoricoRepository historicoRepository;
    @Autowired
    private TransferenciaApiClient transferenciaApiClient;

    private Long contaOrigemId;
    private Long contaDestinoId;

    @BeforeEach
    void setUp() {
        historicoRepository.deleteAll();
        transacaoRepository.deleteAll();
        contaRepository.deleteAll();

        Long clienteId = clienteService.criar(new ClienteRequest("Cliente Concorrencia", ClienteFactory.gerarCpf())).id();
        BigDecimal saldoOrigem = VALOR.multiply(BigDecimal.valueOf(TRANSFERENCIAS_POSSIVEIS));
        contaOrigemId = contaService.criar(
                new ContaRequest("ORIG-" + System.nanoTime(), clienteId, saldoOrigem)).id();
        contaDestinoId = contaService.criar(
                new ContaRequest("DEST-" + System.nanoTime(), clienteId, BigDecimal.ZERO)).id();
    }

    @Test
    @DisplayName("Sob concorrencia: sem saldo negativo, sem lost update, saldos finais consistentes")
    void transferenciasConcorrentesMantemConsistencia() throws InterruptedException {
        var executor = Executors.newFixedThreadPool(NUM_THREADS);
        var largada = new CountDownLatch(1);
        var concluidas = new CountDownLatch(NUM_THREADS);
        var sucessos = new AtomicInteger();
        var saldoInsuficiente = new AtomicInteger();
        var inesperados = new AtomicInteger();

        TransferenciaRequest req = new TransferenciaRequest(contaOrigemId, contaDestinoId, VALOR);

        for (int i = 0; i < NUM_THREADS; i++) {
            int idempotencyKey = i;
            executor.submit(() -> {
                try {
                    largada.await();
                    MvcResult resp = transferenciaApiClient.criar(req, String.valueOf(idempotencyKey));
                    var statusCode = resp.getResponse().getStatus();

                    if (statusCode == HttpStatus.CREATED.value()) {
                        sucessos.incrementAndGet();
                    } else if (statusCode == HttpStatus.UNPROCESSABLE_ENTITY.value()) {
                        saldoInsuficiente.incrementAndGet();
                    } else {
                        inesperados.incrementAndGet();
                    }
                } catch (Exception e) {
                    inesperados.incrementAndGet();
                } finally {
                    concluidas.countDown();
                }
            });
        }

        largada.countDown();
        assertThat(concluidas.await(60, TimeUnit.SECONDS)).as("todas as threads terminaram").isTrue();
        executor.shutdownNow();

        assertThat(inesperados.get()).as("respostas inesperadas").isZero();

        assertThat(sucessos.get()).isEqualTo(TRANSFERENCIAS_POSSIVEIS);
        assertThat(saldoInsuficiente.get()).isEqualTo(NUM_THREADS - TRANSFERENCIAS_POSSIVEIS);

        BigDecimal saldoOrigemFinal = contaRepository.findById(contaOrigemId).orElseThrow().getSaldo();
        BigDecimal saldoDestinoFinal = contaRepository.findById(contaDestinoId).orElseThrow().getSaldo();

        assertThat(saldoOrigemFinal).isEqualByComparingTo("0.00");
        assertThat(saldoOrigemFinal.signum()).isGreaterThanOrEqualTo(0);

        assertThat(saldoDestinoFinal)
                .isEqualByComparingTo(VALOR.multiply(BigDecimal.valueOf(TRANSFERENCIAS_POSSIVEIS)));

        assertThat(saldoOrigemFinal.add(saldoDestinoFinal))
                .isEqualByComparingTo(VALOR.multiply(BigDecimal.valueOf(TRANSFERENCIAS_POSSIVEIS)));

        assertThat(transacaoRepository.findAll())
                .hasSize(TRANSFERENCIAS_POSSIVEIS)
                .allMatch(t -> t.getStatus() == StatusTransacao.CONCLUIDA);
        assertThat(historicoRepository.findByContaIdOrderByDataCriacaoDesc(contaOrigemId))
                .hasSize(TRANSFERENCIAS_POSSIVEIS)
                .allMatch(h -> h.getTipoMovimento() == TipoMovimento.DEBITO);
        assertThat(historicoRepository.findByContaIdOrderByDataCriacaoDesc(contaDestinoId))
                .hasSize(TRANSFERENCIAS_POSSIVEIS)
                .allMatch(h -> h.getTipoMovimento() == TipoMovimento.CREDITO);
    }
}
