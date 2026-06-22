package com.example.banco_digital.config;

import com.example.banco_digital.messaging.TransferenciaProducer;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic transferenciaRealizadaTopic() {
        return TopicBuilder.name(TransferenciaProducer.TOPICO)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
