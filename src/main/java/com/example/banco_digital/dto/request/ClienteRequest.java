package com.example.banco_digital.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados para cadastro de um cliente")
public record ClienteRequest(

        @NotBlank(message = "nome é obrigatório")
        String nome,

        @NotBlank(message = "cpf é obrigatório")
        String cpf
) {
}
