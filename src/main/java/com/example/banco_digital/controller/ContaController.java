package com.example.banco_digital.controller;

import com.example.banco_digital.dto.request.ContaRequest;
import com.example.banco_digital.dto.response.ContaResponse;
import com.example.banco_digital.dto.response.ErrorResponse;
import com.example.banco_digital.dto.response.SaldoResponse;
import com.example.banco_digital.service.ContaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/contas")
@Tag(name = "Contas", description = "Abertura, consulta e saldo de contas")
public class ContaController {

    private final ContaService contaService;

    public ContaController(ContaService contaService) {
        this.contaService = contaService;
    }

    @PostMapping
    @Operation(summary = "Abre uma conta para um cliente existente")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Conta criada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Número de conta já existe",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ContaResponse> criar(@Valid @RequestBody ContaRequest request) {
        ContaResponse response = contaService.criar(request);
        return ResponseEntity.created(URI.create("/contas/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta uma conta por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conta encontrada"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ContaResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(contaService.buscarPorId(id));
    }

    @GetMapping("/{id}/saldo")
    @Operation(summary = "Consulta o saldo atual de uma conta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Saldo retornado"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SaldoResponse> consultarSaldo(@PathVariable Long id) {
        return ResponseEntity.ok(contaService.consultarSaldo(id));
    }
}
