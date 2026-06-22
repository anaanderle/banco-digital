package com.example.banco_digital.helper;

import com.example.banco_digital.dto.request.TransferenciaRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Component
public class TransferenciaApiClient {

    @Autowired
    private MockMvc rest;

    @Autowired
    private JsonMapper mapper;

    public MvcResult criar(
            TransferenciaRequest request,
            String idempotencyKey
    ) throws Exception {
        return rest.perform(
                post("/transferencias")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
        ).andReturn();
    }
}