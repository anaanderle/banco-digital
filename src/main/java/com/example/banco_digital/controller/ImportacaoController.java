package com.example.banco_digital.controller;

import com.example.banco_digital.dto.response.ImportacaoResponse;
import com.example.banco_digital.service.ImportacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/importacoes")
@Tag(name = "Importacao", description = "Importação em massa via CSV/TXT")
public class ImportacaoController {

    private final ImportacaoService importacaoService;

    public ImportacaoController(ImportacaoService importacaoService) {
        this.importacaoService = importacaoService;
    }

    @PostMapping(value = "/clientes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importa clientes em massa (formato: nome;cpf)")
    public ResponseEntity<ImportacaoResponse> importarClientes(@RequestParam("arquivo") MultipartFile arquivo) {
        return ResponseEntity.ok(importacaoService.importarClientes(arquivo));
    }

    @PostMapping(value = "/contas", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importa contas em massa (formato: clienteId;saldoInicial;codigoBanco;numeroConta;digitoConta;numeroAgencia;digitoAgencia)")
    public ResponseEntity<ImportacaoResponse> importarContas(@RequestParam("arquivo") MultipartFile arquivo) {
        return ResponseEntity.ok(importacaoService.importarContas(arquivo));
    }
}
