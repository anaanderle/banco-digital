package com.example.banco_digital.repository;

import com.example.banco_digital.entity.Conta;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContaRepository extends JpaRepository<Conta, Long> {

    boolean existsByNumeroConta(String numeroConta);

    boolean existsByClienteId(Long clienteId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Conta c where c.id in :ids order by c.id asc")
    List<Conta> findByIdsForUpdate(@Param("ids") List<Long> ids);

    Optional<Conta> findByNumeroConta(String numeroConta);
}
