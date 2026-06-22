package com.example.banco_digital.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SaldoResponse(
        Long contaId,
        BigDecimal saldo,
        OffsetDateTime dataConsulta) {
}
