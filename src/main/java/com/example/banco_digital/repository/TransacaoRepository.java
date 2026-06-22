package com.example.banco_digital.repository;

import com.example.banco_digital.entity.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {
}
