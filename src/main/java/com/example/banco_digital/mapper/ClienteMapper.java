package com.example.banco_digital.mapper;

import com.example.banco_digital.dto.request.ClienteRequest;
import com.example.banco_digital.dto.response.ClienteResponse;
import com.example.banco_digital.entity.Cliente;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {

    public Cliente toEntity(ClienteRequest request) {
        return Cliente.builder()
                .nome(request.nome())
                .cpf(request.cpf())
                .build();
    }

    public ClienteResponse toResponse(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCpf(),
                cliente.getDataCriacao());
    }
}
