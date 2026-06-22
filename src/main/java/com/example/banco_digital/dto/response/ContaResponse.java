package com.example.banco_digital.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ContaResponse(
        Long id,
        String codigoBanco,
        String numeroConta,
        String digitoConta,
        String numeroAgencia,
        String digitoAgencia,
        BigDecimal saldo,
        Long clienteId,
        OffsetDateTime dataCriacao) {
}
