package com.example.banco_digital.controller;

import com.example.banco_digital.dto.request.ClienteRequest;
import com.example.banco_digital.dto.response.ClienteResponse;
import com.example.banco_digital.dto.response.ErrorResponse;
import com.example.banco_digital.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clientes")
@Tag(name = "Clientes", description = "Cadastro e consulta de clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    @Operation(summary = "Cadastra um cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "CPF já cadastrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ClienteResponse> criar(@Valid @RequestBody ClienteRequest request) {
        ClienteResponse response = clienteService.criar(request);
        return ResponseEntity.created(URI.create("/clientes/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta um cliente por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ClienteResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Lista todos os clientes")
    public ResponseEntity<List<ClienteResponse>> listar() {
        return ResponseEntity.ok(clienteService.listar());
    }
}
