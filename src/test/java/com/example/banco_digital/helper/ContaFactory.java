package com.example.banco_digital.helper;

import com.example.banco_digital.dto.request.ContaRequest;

import java.math.BigDecimal;

public final class ContaFactory {

    public static ContaRequest gerarContaRequest(Long clienteId, BigDecimal saldoInicial) {
        return new ContaRequest(
                "codigoBanco-" + System.nanoTime(),
                "numeroConta-" + System.nanoTime(),
                "digitoConta-" + System.nanoTime(),
                "numeroAgencia-" + System.nanoTime(),
                "digitoAgencia-" + System.nanoTime(),
                clienteId,
                saldoInicial
        );
    }
}