package com.example.banco_digital.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class TransferenciaProducer {

    private static final Logger log = LoggerFactory.getLogger(TransferenciaProducer.class);

    public static final String TOPICO = "transferencia-realizada";

    private final KafkaTemplate<String, TransferenciaRealizadaEvent> kafkaTemplate;

    public TransferenciaProducer(KafkaTemplate<String, TransferenciaRealizadaEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publicar(TransferenciaRealizadaEvent event) {
        log.info("Publicando evento no topico {} para transacao {}", TOPICO, event.transacaoId());
        kafkaTemplate.send(TOPICO, event.correlationId(), event);
    }
}
