package com.example.banco_digital.service;

import com.example.banco_digital.entity.IdempotencyRecord;
import com.example.banco_digital.exception.ConflictException;
import com.example.banco_digital.repository.IdempotencyRepository;

import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Service
public class IdempotencyService {

    private final IdempotencyRepository repository;
    private final JsonMapper mapper;

    public IdempotencyService(IdempotencyRepository repository, JsonMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public Optional<IdempotencyRecord> buscarConcluida(String key, String requestHash) {
        return repository.findById(key).map(record -> {
            if (!record.getRequestHash().equals(requestHash)) {
                throw new ConflictException(
                        "Idempotency-Key já utilizada com um corpo de requisição diferente");
            }
            return record;
        });
    }

    public IdempotencyRecord reivindicar(String key, String requestHash) {
        try {
            IdempotencyRecord placeholder = IdempotencyRecord.builder()
                    .idempotencyKey(key)
                    .requestHash(requestHash)
                    .responseStatus(0)
                    .responseBody(null)
                    .dataCriacao(OffsetDateTime.now())
                    .build();
            return repository.saveAndFlush(placeholder);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(
                    "Requisição com esta Idempotency-Key já está em processamento");
        }
    }

    public void concluir(IdempotencyRecord record, int status, String responseBody) {
        record.setResponseStatus(status);
        record.setResponseBody(responseBody);
        repository.save(record);
    }

    public String hash(Object payload) {
        try {
            byte[] json = mapper.writeValueAsBytes(payload);
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(json);
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao calcular hash da requisição", e);
        }
    }

    public String serializar(Object response) {
        try {
            return mapper.writeValueAsString(response);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao serializar resposta para idempotência", e);
        }
    }

    public <T> T desserializar(String json, Class<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao desserializar resposta idempotente", e);
        }
    }
}
