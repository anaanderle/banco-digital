package com.example.banco_digital.repository;

import com.example.banco_digital.entity.Historico;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoricoRepository extends JpaRepository<Historico, Long> {

    List<Historico> findByContaIdOrderByDataCriacaoDesc(Long contaId);
}
