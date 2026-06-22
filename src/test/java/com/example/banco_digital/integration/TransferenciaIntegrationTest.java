package com.example.banco_digital.integration;

import com.example.banco_digital.TestcontainersConfiguration;
import com.example.banco_digital.dto.request.ClienteRequest;
import com.example.banco_digital.dto.request.TransferenciaRequest;
import com.example.banco_digital.dto.response.TransferenciaResponse;
import com.example.banco_digital.entity.StatusTransacao;
import com.example.banco_digital.entity.TipoMovimento;
import com.example.banco_digital.helper.ClienteFactory;
import com.example.banco_digital.helper.ContaFactory;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, TransferenciaApiClient.class})
class TransferenciaIntegrationTest {

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

        Long clienteId = clienteService.criar(new ClienteRequest("Maria Silva", ClienteFactory.gerarCpf())).id();

        contaOrigemId = contaService.criar(ContaFactory.gerarContaRequest(clienteId, new BigDecimal("1000.00"))).id();
        contaDestinoId = contaService.criar(ContaFactory.gerarContaRequest(clienteId, new BigDecimal("200.00"))).id();
    }

    @Test
    @DisplayName("Transferencia com sucesso atualiza saldos, cria 2 historicos e persiste a transacao")
    void transferenciaComSucesso() throws Exception {
        TransferenciaRequest req = new TransferenciaRequest(contaOrigemId, contaDestinoId, new BigDecimal("300.00"));

        MvcResult resp = transferenciaApiClient.criar(req, null);

        int statusCode = resp.getResponse().getStatus();

        TransferenciaResponse body = mapper.readValue(
                resp.getResponse().getContentAsString(),
                TransferenciaResponse.class
        );

        assertThat(statusCode).isEqualTo(HttpStatus.CREATED.value());
        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(StatusTransacao.CONCLUIDA);
        assertThat(body.correlationId()).isNotBlank();

        assertThat(contaRepository.findById(contaOrigemId).orElseThrow().getSaldo())
                .isEqualByComparingTo("700.00");
        assertThat(contaRepository.findById(contaDestinoId).orElseThrow().getSaldo())
                .isEqualByComparingTo("500.00");

        assertThat(transacaoRepository.findAll()).hasSize(1);

        var debitos = historicoRepository.findByContaIdOrderByDataCriacaoDesc(contaOrigemId);
        var creditos = historicoRepository.findByContaIdOrderByDataCriacaoDesc(contaDestinoId);
        assertThat(debitos).hasSize(1);
        assertThat(debitos.get(0).getTipoMovimento()).isEqualTo(TipoMovimento.DEBITO);
        assertThat(debitos.get(0).getSaldoPosterior()).isEqualByComparingTo("700.00");
        assertThat(creditos).hasSize(1);
        assertThat(creditos.get(0).getTipoMovimento()).isEqualTo(TipoMovimento.CREDITO);
        assertThat(creditos.get(0).getSaldoPosterior()).isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("Conta de destino inexistente retorna 404 e nao altera saldo")
    void transferenciaContaInexistenteRetorna404() throws Exception {
        TransferenciaRequest req = new TransferenciaRequest(contaOrigemId, 999_999L, new BigDecimal("100.00"));

        MvcResult resp = transferenciaApiClient.criar(req, null);

        int statusCode = resp.getResponse().getStatus();

        assertThat(statusCode).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(contaRepository.findById(contaOrigemId).orElseThrow().getSaldo())
                .isEqualByComparingTo("1000.00");
        assertThat(transacaoRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("Saldo insuficiente retorna 422 e nao altera saldo")
    void transferenciaSaldoInsuficienteRetorna422() throws Exception {
        TransferenciaRequest req = new TransferenciaRequest(contaOrigemId, contaDestinoId, new BigDecimal("5000.00"));

        MvcResult resp = transferenciaApiClient.criar(req, null);

        int statusCode = resp.getResponse().getStatus();

        assertThat(statusCode).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
        assertThat(contaRepository.findById(contaOrigemId).orElseThrow().getSaldo())
                .isEqualByComparingTo("1000.00");
        assertThat(transacaoRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("Transferencia para a mesma conta retorna 422")
    void transferenciaMesmaContaRetorna422() throws Exception {
        TransferenciaRequest req = new TransferenciaRequest(contaOrigemId, contaOrigemId, new BigDecimal("100.00"));

        MvcResult resp = transferenciaApiClient.criar(req, null);

        int statusCode = resp.getResponse().getStatus();

        assertThat(statusCode).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
        assertThat(transacaoRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("Valor nao positivo e barrado pela Bean Validation com 400")
    void transferenciaValorInvalidoRetorna400() throws Exception {
        TransferenciaRequest req = new TransferenciaRequest(contaOrigemId, contaDestinoId, new BigDecimal("-1.00"));

        MvcResult resp = transferenciaApiClient.criar(req, null);

        int statusCode = resp.getResponse().getStatus();

        assertThat(statusCode).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(transacaoRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("Mesma Idempotency-Key nao reprocessa: debita uma vez e devolve a resposta original")
    void transferenciaIdempotenteNaoReprocessa() throws Exception {
        TransferenciaRequest req = new TransferenciaRequest(contaOrigemId, contaDestinoId, new BigDecimal("100.00"));

        String idempotencyKey = "chave-fixa-123";
        MvcResult primeira = transferenciaApiClient.criar(req, idempotencyKey);
        int primeiraStatusCode = primeira.getResponse().getStatus();
        MvcResult segunda = transferenciaApiClient.criar(req, idempotencyKey);
        int segundaStatusCode = segunda.getResponse().getStatus();

        TransferenciaResponse primeirabody = mapper.readValue(
                primeira.getResponse().getContentAsString(),
                TransferenciaResponse.class
        );
        TransferenciaResponse segundabody = mapper.readValue(
                primeira.getResponse().getContentAsString(),
                TransferenciaResponse.class
        );

        assertThat(primeiraStatusCode).isEqualTo(HttpStatus.CREATED.value());
        assertThat(segundaStatusCode).isEqualTo(HttpStatus.CREATED.value());
        assertThat(segundabody.transacaoId()).isEqualTo(primeirabody.transacaoId());

        assertThat(contaRepository.findById(contaOrigemId).orElseThrow().getSaldo())
                .isEqualByComparingTo("900.00");
        assertThat(transacaoRepository.findAll()).hasSize(1);
    }
}
