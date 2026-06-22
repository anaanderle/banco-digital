package com.example.banco_digital.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "conta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_conta", nullable = false, unique = true)
    private String numeroConta;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldo;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private OffsetDateTime dataCriacao;

    @Version
    @Column(nullable = false)
    private Long versao;
}
