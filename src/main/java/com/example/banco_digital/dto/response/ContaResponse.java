package com.example.banco_digital.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ContaResponse(
        Long id,
        String numeroConta,
        BigDecimal saldo,
        Long clienteId,
        OffsetDateTime dataCriacao) {
}
