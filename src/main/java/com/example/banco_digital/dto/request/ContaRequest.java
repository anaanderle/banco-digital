package com.example.banco_digital.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

@Schema(description = "Dados para abertura de uma conta")
public record ContaRequest(

        @NotBlank(message = "numeroConta é obrigatório")
        String numeroConta,

        @NotNull(message = "clienteId é obrigatório")
        Long clienteId,

        @Schema(description = "Saldo inicial, se omitido assume 0")
        @PositiveOrZero(message = "saldoInicial não pode ser negativo")
        BigDecimal saldoInicial
) {
    public BigDecimal saldoInicialOuZero() {
        return saldoInicial == null ? BigDecimal.ZERO : saldoInicial;
    }
}
