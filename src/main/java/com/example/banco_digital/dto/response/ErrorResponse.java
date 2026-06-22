package com.example.banco_digital.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String message,
        String correlationId,
        List<CampoInvalido> errors) {

    public record CampoInvalido(String campo, String motivo) {
    }
}
