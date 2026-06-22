package com.example.banco_digital.messaging;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TransferenciaEventListener {

    private final TransferenciaProducer producer;

    public TransferenciaEventListener(TransferenciaProducer producer) {
        this.producer = producer;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTransferenciaConcluida(TransferenciaConcluidaApplicationEvent event) {
        producer.publicar(event.payload());
    }
}
