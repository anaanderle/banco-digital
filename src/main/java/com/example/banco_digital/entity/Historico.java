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
@Table(name = "historico")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Historico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conta_id", nullable = false)
    private Long contaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimento", nullable = false, length = 10)
    private TipoMovimento tipoMovimento;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @Column(name = "saldo_anterior", nullable = false, precision = 19, scale = 2)
    private BigDecimal saldoAnterior;

    @Column(name = "saldo_posterior", nullable = false, precision = 19, scale = 2)
    private BigDecimal saldoPosterior;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private OffsetDateTime dataCriacao;
}
