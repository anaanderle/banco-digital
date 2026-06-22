package com.example.banco_digital.mapper;

import com.example.banco_digital.dto.request.ContaRequest;
import com.example.banco_digital.dto.response.ContaResponse;
import com.example.banco_digital.dto.response.SaldoResponse;
import com.example.banco_digital.entity.Conta;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class ContaMapper {

    public Conta toEntity(ContaRequest request) {
        return Conta.builder()
                .codigoBanco(request.codigoBanco())
                .numeroConta(request.numeroConta())
                .digitoConta(request.digitoConta())
                .numeroAgencia(request.numeroAgencia())
                .digitoAgencia(request.digitoAgencia())
                .clienteId(request.clienteId())
                .saldo(request.saldoInicialOuZero())
                .build();
    }

    public ContaResponse toResponse(Conta conta) {
        return new ContaResponse(
                conta.getId(),
                conta.getCodigoBanco(),
                conta.getNumeroConta(),
                conta.getDigitoConta(),
                conta.getNumeroAgencia(),
                conta.getDigitoAgencia(),
                conta.getSaldo(),
                conta.getClienteId(),
                conta.getDataCriacao());
    }

    public SaldoResponse toSaldoResponse(Conta conta, OffsetDateTime dataConsulta) {
        return new SaldoResponse(conta.getId(), conta.getSaldo(), dataConsulta);
    }
}
