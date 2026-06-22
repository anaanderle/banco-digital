package com.example.banco_digital.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Schema(description = "Solicitação de transferência entre duas contas")
public record TransferenciaRequest(

        @Schema(example = "10")
        @NotNull(message = "contaOrigemId é obrigatório")
        Long contaOrigemId,

        @Schema(example = "20")
        @NotNull(message = "contaDestinoId é obrigatório")
        Long contaDestinoId,

        @Schema(example = "100.00")
        @NotNull(message = "valor é obrigatório")
        @Positive(message = "valor deve ser maior que zero")
        BigDecimal valor) {
}
