package com.learn.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.dto.TransactionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaClient {
    @Value("${kafka.transaction.topic}")
    private String transactionTopic;

    private final KafkaProducer<String, String> producer;
    private final ObjectMapper objectMapper;

    public void sendTransaction(TransactionDto transactionDto) {
        ProducerRecord<String, String> transactionRecord = null;
        try {
            transactionRecord = new ProducerRecord<>(transactionTopic,
                    transactionDto.getClientId().toString(),
                    objectMapper.writeValueAsString(transactionDto));
        } catch (JsonProcessingException e) {
            log.error("Error parsing message", e);
        }

        producer.send(transactionRecord);
    }
}
