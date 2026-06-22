package com.example.banco_digital.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastro de um cliente")
public record ClienteRequest(

        @Schema(example = "Maria Silva")
        @NotBlank(message = "nome é obrigatório")
        @Size(min = 2, max = 150, message = "nome deve ter entre 2 e 150 caracteres")
        String nome,

        @Schema(example = "52998224725", description = "CPF com 11 dígitos, somente números")
        @NotBlank(message = "cpf é obrigatório")
        @Pattern(regexp = "\\d{11}", message = "cpf deve conter exatamente 11 dígitos numéricos")
        String cpf) {
}
