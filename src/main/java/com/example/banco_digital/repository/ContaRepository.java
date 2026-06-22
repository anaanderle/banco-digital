package com.example.banco_digital.repository;

import com.example.banco_digital.entity.Conta;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContaRepository extends JpaRepository<Conta, Long> {

    boolean existsByNumeroConta(String numeroConta);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Conta> findByIdInOrderByIdAsc(@Param("ids") List<Long> ids);
}
