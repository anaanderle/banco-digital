package com.example.banco_digital.controller;

import com.example.banco_digital.dto.request.TransferenciaRequest;
import com.example.banco_digital.dto.response.ErrorResponse;
import com.example.banco_digital.dto.response.TransferenciaResponse;
import com.example.banco_digital.service.TransferenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transferencias")
@Tag(name = "Transferencias", description = "Transferência bancária")
public class TransferenciaController {

    private final TransferenciaService transferenciaService;

    public TransferenciaController(TransferenciaService transferenciaService) {
        this.transferenciaService = transferenciaService;
    }

    @PostMapping
    @Operation(summary = "Realiza uma transferência entre duas contas")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transferência concluída"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Conta de origem ou destino inexistente",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de idempotência",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TransferenciaResponse> transferir(
            @Valid @RequestBody TransferenciaRequest request,
            @Parameter(description = "Chave de idempotência para retentativas seguras")
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        TransferenciaResponse response = transferenciaService.transferir(request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
