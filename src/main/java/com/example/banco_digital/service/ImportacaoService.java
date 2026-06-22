package com.example.banco_digital.service;

import com.example.banco_digital.dto.request.ClienteRequest;
import com.example.banco_digital.dto.request.ContaRequest;
import com.example.banco_digital.dto.response.ImportacaoResponse;
import com.example.banco_digital.dto.response.ImportacaoResponse.ErroLinha;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImportacaoService {

    private static final Logger log = LoggerFactory.getLogger(ImportacaoService.class);
    private static final String SEPARADOR = "[;\\t]";

    private final ClienteService clienteService;
    private final ContaService contaService;

    public ImportacaoService(ClienteService clienteService, ContaService contaService) {
        this.clienteService = clienteService;
        this.contaService = contaService;
    }

    public ImportacaoResponse importarClientes(MultipartFile arquivo) {
        return importar(arquivo, "nome", this::processarLinhaCliente);
    }

    public ImportacaoResponse importarContas(MultipartFile arquivo) {
        return importar(arquivo, "numero", this::processarLinhaConta);
    }

    private ImportacaoResponse importar(MultipartFile arquivo, String headerHint, LinhaProcessor processor) {
        validarArquivo(arquivo);
        List<ErroLinha> erros = new ArrayList<>();
        int total = 0;
        int importados = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(arquivo.getInputStream(), StandardCharsets.UTF_8))) {

            String linha;
            int numeroLinha = 0;
            boolean primeira = true;

            while ((linha = reader.readLine()) != null) {
                numeroLinha++;

                if (linha.isBlank()) {
                    continue;
                }

                if (primeira && pareceCabecalho(linha, headerHint)) {
                    primeira = false;
                    continue;
                }

                primeira = false;
                total++;

                try {
                    processor.processar(linha);
                    importados++;
                } catch (Exception e) {
                    erros.add(new ErroLinha(numeroLinha, e.getMessage()));
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao ler o arquivo: " + e.getMessage(), e);
        }

        log.info("Importação concluída: {} linhas, {} importadas, {} falhas", total, importados, erros.size());
        return new ImportacaoResponse(total, importados, erros.size(), erros);
    }

    private void processarLinhaCliente(String linha) {
        String[] campos = linha.split(SEPARADOR, -1);

        if (campos.length < 2) {
            throw new IllegalArgumentException("Esperado 'nome;cpf'");
        }

        String nome = campos[0].trim();
        String cpf = campos[1].trim();

        clienteService.criar(new ClienteRequest(nome, cpf));
    }

    private void processarLinhaConta(String linha) {
        String[] campos = linha.split(SEPARADOR, -1);

        if (campos.length < 6) {
            throw new IllegalArgumentException("Esperado 'clienteId;saldoInicial;codigoBanco;numeroConta;digitoConta;numeroAgencia;digitoAgencia'");
        }

        Long clienteId = parseLong(campos[0].trim(), "clienteId");
        BigDecimal saldoInicial = getValor(campos[1].trim());
        String codigoBanco = campos[2].trim();
        String numeroConta = campos[3].trim();
        String digitoConta = campos[4].trim();
        String numeroAgencia = campos[5].trim();
        String digitoAgencia = campos[6].trim();

        contaService.criar(new ContaRequest(codigoBanco, numeroConta, digitoConta, numeroAgencia, digitoAgencia, clienteId, saldoInicial));
    }

    private void validarArquivo(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Arquivo vazio ou ausente");
        }

        String nome = arquivo.getOriginalFilename();

        if (nome != null) {
            String lower = nome.toLowerCase();
            if (!lower.endsWith(".csv") && !lower.endsWith(".txt")) {
                throw new IllegalArgumentException("Formato não suportado; use .csv ou .txt");
            }
        }
    }

    private boolean pareceCabecalho(String linha, String hint) {
        String lower = linha.toLowerCase();
        return lower.contains(hint) || lower.contains("cpf") || lower.contains("cliente");
    }

    private Long parseLong(String valor, String campo) {
        try {
            return Long.parseLong(valor);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(campo + " inválido: '" + valor + "'");
        }
    }

    private BigDecimal getValor(String valor) {
        if (valor.isBlank()) return BigDecimal.ZERO;
        BigDecimal parsedValor = parseValor(valor);
        return parsedValor.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : parsedValor;
    }

    private BigDecimal parseValor(String valor) {
        try {
            return new BigDecimal(valor.replace(".", "").replace(",", "."));
        } catch (NumberFormatException e) {
            try {
                return new BigDecimal(valor);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("saldoInicial inválido: '" + valor + "'");
            }
        }
    }

    @FunctionalInterface
    private interface LinhaProcessor {
        void processar(String linha) throws Exception;
    }
}
