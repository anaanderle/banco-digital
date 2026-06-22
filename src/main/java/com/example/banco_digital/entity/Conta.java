package com.example.banco_digital.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

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

    @Column(name = "codigo_banco", nullable = false)
    private String codigoBanco;

    @Column(name = "numero_conta", nullable = false)
    private String numeroConta;

    @Column(name = "digito_conta", nullable = false)
    private String digitoConta;

    @Column(name = "numero_agencia", nullable = false)
    private String numeroAgencia;

    @Column(name = "digito_agencia")
    private String digitoAgencia;

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
