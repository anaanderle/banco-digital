package com.example.banco_digital.service;

import com.example.banco_digital.dto.request.ClienteRequest;
import com.example.banco_digital.dto.response.ClienteResponse;
import com.example.banco_digital.entity.Cliente;
import com.example.banco_digital.exception.ConflictException;
import com.example.banco_digital.exception.ResourceNotFoundException;
import com.example.banco_digital.mapper.ClienteMapper;
import com.example.banco_digital.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    public ClienteService(ClienteRepository clienteRepository, ClienteMapper clienteMapper) {
        this.clienteRepository = clienteRepository;
        this.clienteMapper = clienteMapper;
    }

    @Transactional
    public ClienteResponse criar(ClienteRequest request) {
        if (clienteRepository.existsByCpf(request.cpf())) {
            throw new ConflictException("Já existe um cliente com o CPF informado");
        }

        Cliente cliente = clienteMapper.toEntity(request);
        cliente.setDataCriacao(OffsetDateTime.now());

        return clienteMapper.toResponse(clienteRepository.save(cliente));
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(Long id) {
        return clienteMapper.toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return clienteRepository.findAll().stream()
                .map(clienteMapper::toResponse)
                .toList();
    }

    public Cliente buscarEntidade(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado: id " + id));
    }
}
