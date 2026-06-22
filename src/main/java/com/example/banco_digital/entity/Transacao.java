package com.example.banco_digital.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "transacao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conta_origem_id", nullable = false)
    private Long contaOrigemId;

    @Column(name = "conta_destino_id", nullable = false)
    private Long contaDestinoId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusTransacao status;

    @Column(name = "correlation_id", nullable = false)
    private String correlationId;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private OffsetDateTime dataCriacao;
}
