package com.example.banco_digital.mapper;

import com.example.banco_digital.dto.response.TransferenciaResponse;
import com.example.banco_digital.entity.Transacao;
import org.springframework.stereotype.Component;

@Component
public class TransacaoMapper {

    public TransferenciaResponse toResponse(Transacao transacao) {
        return new TransferenciaResponse(
                transacao.getId(),
                transacao.getContaOrigemId(),
                transacao.getContaDestinoId(),
                transacao.getValor(),
                transacao.getStatus(),
                transacao.getCorrelationId(),
                transacao.getDataCriacao());
    }
}
