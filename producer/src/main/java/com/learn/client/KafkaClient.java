package com.learn.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.dto.Client;
import com.learn.dto.Transaction;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaClient {
    @Value("${kafka.client.topic}")
    private String clientTopic;
    @Value("${kafka.transaction.topic}")
    private String transactionTopic;

    private final KafkaProducer<String, String> producer;
    private final ObjectMapper objectMapper;

    public void sendClient(Client client) throws JsonProcessingException {
        ProducerRecord<String, String> clientRecord = new ProducerRecord<>(clientTopic,
                client.getClientId().toString(),
                objectMapper.writeValueAsString(client));

        producer.send(clientRecord);
    }

    public void sendTransaction(Transaction transaction) throws JsonProcessingException {
        ProducerRecord<String, String> transactionRecord = new ProducerRecord<>(transactionTopic,
                transaction.getClientId().toString(),
                objectMapper.writeValueAsString(transaction));

        producer.send(transactionRecord);
    }
}
