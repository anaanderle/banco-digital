package com.example.banco_digital.helper;

import com.example.banco_digital.dto.request.TransferenciaRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@TestComponent
public class TransferenciaApiClient {

    @Autowired
    private MockMvc rest;

    @Autowired
    private JsonMapper mapper;

    public MvcResult criar(
            TransferenciaRequest request,
            String idempotencyKey
    ) throws Exception {
        var requestBuilder = post("/transferencias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request));

        if (idempotencyKey != null) {
            requestBuilder.header("Idempotency-Key", idempotencyKey);
        }

        return rest.perform(requestBuilder).andReturn();
    }
}