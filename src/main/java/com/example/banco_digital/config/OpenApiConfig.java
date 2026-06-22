package com.example.banco_digital.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bancoDigitalOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Banco Digital API")
                .description("""
                        API REST de um banco digital.

                        Cabeçalhos nos endpoints de escrita:
                        - X-Correlation-Id: rastreabilidade ponta a ponta (gerado se ausente).
                        - Idempotency-Key: garante que requisições repetidas não sejam reprocessadas.
                        """)
                .version("v1")
                .contact(new Contact().name("Ana Clara Anderle")));
    }
}
