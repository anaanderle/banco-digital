package com.example.banco_digital.dto.response;

import java.util.List;

public record ImportacaoResponse(
        int totalLinhas,
        int importados,
        int falhas,
        List<ErroLinha> erros) {

    public record ErroLinha(int linha, String motivo) {
    }
}
