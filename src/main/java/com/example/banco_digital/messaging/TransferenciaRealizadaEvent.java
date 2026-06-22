package com.example.banco_digital.messaging;

import java.math.BigDecimal;

public record TransferenciaRealizadaEvent(
        Long transacaoId,
        Long contaOrigemId,
        Long contaDestinoId,
        BigDecimal valor,
        String correlationId) {
}
