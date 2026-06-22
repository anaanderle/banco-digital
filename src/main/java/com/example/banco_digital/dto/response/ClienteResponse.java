package com.example.banco_digital.dto.response;

import java.time.OffsetDateTime;

public record ClienteResponse(
        Long id,
        String nome,
        String cpf,
        OffsetDateTime dataCriacao) {
}
