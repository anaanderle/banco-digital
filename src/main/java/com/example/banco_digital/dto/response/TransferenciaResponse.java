package com.example.banco_digital.dto.response;

import com.example.banco_digital.entity.StatusTransacao;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TransferenciaResponse(
        Long transacaoId,
        Long contaOrigemId,
        Long contaDestinoId,
        BigDecimal valor,
        StatusTransacao status,
        String correlationId,
        OffsetDateTime dataCriacao) {
}
