package com.example.banco_digital.service;

import com.example.banco_digital.dto.request.ContaRequest;
import com.example.banco_digital.dto.response.ContaResponse;
import com.example.banco_digital.dto.response.SaldoResponse;
import com.example.banco_digital.entity.Conta;
import com.example.banco_digital.exception.ConflictException;
import com.example.banco_digital.exception.ResourceNotFoundException;
import com.example.banco_digital.mapper.ContaMapper;
import com.example.banco_digital.repository.ClienteRepository;
import com.example.banco_digital.repository.ContaRepository;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContaService {

    private final ContaRepository contaRepository;
    private final ClienteRepository clienteRepository;
    private final ContaMapper contaMapper;

    public ContaService(ContaRepository contaRepository,
                        ClienteRepository clienteRepository,
                        ContaMapper contaMapper) {
        this.contaRepository = contaRepository;
        this.clienteRepository = clienteRepository;
        this.contaMapper = contaMapper;
    }

    @Transactional
    public ContaResponse criar(ContaRequest request) {
        if (!clienteRepository.existsById(request.clienteId())) {
            throw new ResourceNotFoundException("Cliente não encontrado: id " + request.clienteId());
        }
        if (contaRepository.existsByNumeroConta(request.numeroConta())) {
            throw new ConflictException("Já existe conta com o número informado");
        }
        Conta conta = contaMapper.toEntity(request);
        conta.setDataCriacao(OffsetDateTime.now());
        return contaMapper.toResponse(contaRepository.save(conta));
    }

    @Transactional(readOnly = true)
    public ContaResponse buscarPorId(Long id) {
        return contaMapper.toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public SaldoResponse consultarSaldo(Long id) {
        Conta conta = buscarEntidade(id);
        return contaMapper.toSaldoResponse(conta, OffsetDateTime.now());
    }

    public Conta buscarEntidade(Long id) {
        return contaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: id " + id));
    }
}
