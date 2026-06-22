package com.example.banco_digital.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.banco_digital.dto.request.TransferenciaRequest;
import com.example.banco_digital.dto.response.TransferenciaResponse;
import com.example.banco_digital.entity.Conta;
import com.example.banco_digital.entity.Historico;
import com.example.banco_digital.entity.StatusTransacao;
import com.example.banco_digital.entity.TipoMovimento;
import com.example.banco_digital.entity.Transacao;
import com.example.banco_digital.exception.BusinessException;
import com.example.banco_digital.exception.ResourceNotFoundException;
import com.example.banco_digital.mapper.TransacaoMapper;
import com.example.banco_digital.messaging.TransferenciaConcluidaApplicationEvent;
import com.example.banco_digital.repository.ContaRepository;
import com.example.banco_digital.repository.HistoricoRepository;
import com.example.banco_digital.repository.TransacaoRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class TransferenciaServiceTest {

    @Mock private ContaRepository contaRepository;
    @Mock private TransacaoRepository transacaoRepository;
    @Mock private HistoricoRepository historicoRepository;
    @Mock private TransacaoMapper transacaoMapper;
    @Mock private IdempotencyService idempotencyService;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private TransferenciaService transferenciaService;

    private Conta conta(Long id, String saldo) {
        return Conta.builder()
                .id(id)
                .numeroConta("ACC-" + id)
                .saldo(new BigDecimal(saldo))
                .clienteId(1L)
                .dataCriacao(OffsetDateTime.now())
                .versao(0L)
                .build();
    }

    @Test
    @DisplayName("Transferencia valida debita a origem, credita o destino e persiste tudo")
    void transferir_comSucesso_deveDebitarCreditarEPersistir() {
        Conta origem = conta(1L, "1000.00");
        Conta destino = conta(2L, "500.00");
        TransferenciaRequest request = new TransferenciaRequest(1L, 2L, new BigDecimal("100.00"));

        when(contaRepository.findByIdsForUpdate(anyList())).thenReturn(List.of(origem, destino));
        when(transacaoRepository.save(any(Transacao.class))).thenAnswer(inv -> {
            Transacao t = inv.getArgument(0);
            t.setId(99L);
            return t;
        });
        TransferenciaResponse esperado = new TransferenciaResponse(
                99L, 1L, 2L, new BigDecimal("100.00"),
                StatusTransacao.CONCLUIDA, "corr-1", OffsetDateTime.now());
        when(transacaoMapper.toResponse(any(Transacao.class))).thenReturn(esperado);

        TransferenciaResponse resultado = transferenciaService.transferir(request, null);

        assertThat(origem.getSaldo()).isEqualByComparingTo("900.00");
        assertThat(destino.getSaldo()).isEqualByComparingTo("600.00");
        assertThat(resultado).isEqualTo(esperado);

        verify(transacaoRepository, times(1)).save(any(Transacao.class));
        ArgumentCaptor<Historico> histCaptor = ArgumentCaptor.forClass(Historico.class);
        verify(historicoRepository, times(2)).save(histCaptor.capture());
        List<Historico> historicos = histCaptor.getAllValues();
        assertThat(historicos).extracting(Historico::getTipoMovimento)
                .containsExactly(TipoMovimento.DEBITO, TipoMovimento.CREDITO);
        assertThat(historicos.get(0).getSaldoAnterior()).isEqualByComparingTo("1000.00");
        assertThat(historicos.get(0).getSaldoPosterior()).isEqualByComparingTo("900.00");
        assertThat(historicos.get(1).getSaldoAnterior()).isEqualByComparingTo("500.00");
        assertThat(historicos.get(1).getSaldoPosterior()).isEqualByComparingTo("600.00");

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOf(TransferenciaConcluidaApplicationEvent.class);
    }

    @Test
    @DisplayName("Saldo insuficiente lanca BusinessException e nao persiste nada")
    void transferir_saldoInsuficiente_deveLancarBusiness() {
        Conta origem = conta(1L, "50.00");
        Conta destino = conta(2L, "0.00");
        TransferenciaRequest request = new TransferenciaRequest(1L, 2L, new BigDecimal("100.00"));

        when(contaRepository.findByIdsForUpdate(anyList())).thenReturn(List.of(origem, destino));

        assertThatThrownBy(() -> transferenciaService.transferir(request, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Saldo insuficiente");

        verify(transacaoRepository, never()).save(any());
        verify(historicoRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Transferencia para a mesma conta e barrada antes de qualquer acesso ao banco")
    void transferir_mesmaConta_deveLancarBusiness() {
        TransferenciaRequest request = new TransferenciaRequest(1L, 1L, new BigDecimal("100.00"));

        assertThatThrownBy(() -> transferenciaService.transferir(request, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("mesma");

        verify(contaRepository, never()).findByIdsForUpdate(anyList());
        verify(transacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Conta de origem inexistente lanca ResourceNotFoundException")
    void transferir_origemInexistente_deveLancarNotFound() {
        Conta destino = conta(2L, "500.00");
        TransferenciaRequest request = new TransferenciaRequest(1L, 2L, new BigDecimal("100.00"));

        when(contaRepository.findByIdsForUpdate(anyList())).thenReturn(List.of(destino));

        assertThatThrownBy(() -> transferenciaService.transferir(request, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("origem");

        verify(transacaoRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Valor menor ou igual a zero e barrado pela checagem defensiva")
    void transferir_valorNaoPositivo_deveLancarBusiness() {
        TransferenciaRequest request = new TransferenciaRequest(1L, 2L, BigDecimal.ZERO);

        assertThatThrownBy(() -> transferenciaService.transferir(request, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("maior que zero");

        verify(contaRepository, never()).findByIdsForUpdate(anyList());
    }
}
