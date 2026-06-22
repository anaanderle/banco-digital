package com.example.banco_digital.service;

import com.example.banco_digital.dto.request.TransferenciaRequest;
import com.example.banco_digital.dto.response.TransferenciaResponse;
import com.example.banco_digital.entity.*;
import com.example.banco_digital.exception.BusinessException;
import com.example.banco_digital.exception.ResourceNotFoundException;
import com.example.banco_digital.helper.CorrelationIdHolder;
import com.example.banco_digital.mapper.TransacaoMapper;
import com.example.banco_digital.messaging.TransferenciaConcluidaApplicationEvent;
import com.example.banco_digital.messaging.TransferenciaRealizadaEvent;
import com.example.banco_digital.repository.ContaRepository;
import com.example.banco_digital.repository.HistoricoRepository;
import com.example.banco_digital.repository.TransacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TransferenciaService {

    private static final Logger log = LoggerFactory.getLogger(TransferenciaService.class);

    private final ContaRepository contaRepository;
    private final TransacaoRepository transacaoRepository;
    private final HistoricoRepository historicoRepository;
    private final TransacaoMapper transacaoMapper;
    private final IdempotencyService idempotencyService;
    private final ApplicationEventPublisher eventPublisher;

    public TransferenciaService(ContaRepository contaRepository,
                                TransacaoRepository transacaoRepository,
                                HistoricoRepository historicoRepository,
                                TransacaoMapper transacaoMapper,
                                IdempotencyService idempotencyService,
                                ApplicationEventPublisher eventPublisher
    ) {
        this.contaRepository = contaRepository;
        this.transacaoRepository = transacaoRepository;
        this.historicoRepository = historicoRepository;
        this.transacaoMapper = transacaoMapper;
        this.idempotencyService = idempotencyService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public TransferenciaResponse transferir(TransferenciaRequest request, String idempotencyKey) {
        validarRequisicao(request);

        String requestHash = null;
        IdempotencyRecord claim = null;

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            requestHash = idempotencyService.hash(request);
            var concluida = idempotencyService.buscarConcluida(idempotencyKey, requestHash);

            if (concluida.isPresent()) {
                log.info("Idempotency-Key {} ja processada; devolvendo resposta original", idempotencyKey);
                return idempotencyService.desserializar(
                        concluida.get().getResponseBody(), TransferenciaResponse.class);
            }

            claim = idempotencyService.reivindicar(idempotencyKey, requestHash);
        }

        TransferenciaResponse response = executarTransferencia(request);

        if (claim != null) {
            idempotencyService.concluir(claim, 201, idempotencyService.serializar(response));
        }

        return response;
    }

    private TransferenciaResponse executarTransferencia(TransferenciaRequest request) {
        Long origemId = request.contaOrigemId();
        Long destinoId = request.contaDestinoId();
        BigDecimal valor = request.valor();

        List<Long> idsOrdenados = origemId < destinoId ? List.of(origemId, destinoId) : List.of(destinoId, origemId);

        Map<Long, Conta> contas = contaRepository.findByIdInOrderByIdAsc(idsOrdenados).stream()
                .collect(Collectors.toMap(Conta::getId, Function.identity()));

        Conta origem = contas.get(origemId);
        Conta destino = contas.get(destinoId);

        if (origem == null) {
            throw new ResourceNotFoundException("Conta de origem não encontrada: id " + origemId);
        }

        if (destino == null) {
            throw new ResourceNotFoundException("Conta de destino não encontrada: id " + destinoId);
        }

        if (origem.getSaldo().compareTo(valor) < 0) {
            throw new BusinessException("Saldo insuficiente na conta de origem");
        }

        BigDecimal saldoOrigemAnterior = origem.getSaldo();
        BigDecimal saldoDestinoAnterior = destino.getSaldo();
        origem.setSaldo(saldoOrigemAnterior.subtract(valor));
        destino.setSaldo(saldoDestinoAnterior.add(valor));

        OffsetDateTime agora = OffsetDateTime.now();
        String correlationId = CorrelationIdHolder.get();

        Transacao transacao = transacaoRepository.save(Transacao.builder()
                .contaOrigemId(origemId)
                .contaDestinoId(destinoId)
                .valor(valor)
                .status(StatusTransacao.CONCLUIDA)
                .correlationId(correlationId)
                .dataCriacao(agora)
                .build());

        historicoRepository.save(novoHistorico(
                origemId, TipoMovimento.DEBITO, valor, saldoOrigemAnterior, origem.getSaldo(), agora));
        historicoRepository.save(novoHistorico(
                destinoId, TipoMovimento.CREDITO, valor, saldoDestinoAnterior, destino.getSaldo(), agora));

        eventPublisher.publishEvent(new TransferenciaConcluidaApplicationEvent(
                new TransferenciaRealizadaEvent(
                        transacao.getId(), origemId, destinoId, valor, correlationId)));

        log.info("Transferencia {} concluida: {} -> {} valor {}",
                transacao.getId(), origemId, destinoId, valor);
        return transacaoMapper.toResponse(transacao);
    }

    private void validarRequisicao(TransferenciaRequest request) {
        if (request.contaOrigemId().equals(request.contaDestinoId())) {
            throw new BusinessException("Conta de origem e destino não podem ser a mesma");
        }

        if (request.valor() == null || request.valor().signum() <= 0) {
            throw new BusinessException("Valor da transferência deve ser maior que zero");
        }
    }

    private Historico novoHistorico(Long contaId, TipoMovimento tipo, BigDecimal valor,
                                    BigDecimal saldoAnterior, BigDecimal saldoPosterior,
                                    OffsetDateTime dataCriacao
    ) {
        return Historico.builder()
                .contaId(contaId)
                .tipoMovimento(tipo)
                .valor(valor)
                .saldoAnterior(saldoAnterior)
                .saldoPosterior(saldoPosterior)
                .dataCriacao(dataCriacao)
                .build();
    }
}
