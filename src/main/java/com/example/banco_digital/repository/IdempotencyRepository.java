package com.example.banco_digital.repository;

import com.example.banco_digital.entity.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyRepository extends JpaRepository<IdempotencyRecord, String> {
}
