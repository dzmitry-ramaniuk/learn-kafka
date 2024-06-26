package com.learn.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.client.KafkaClient;
import com.learn.dto.TransactionDto;
import com.learn.exception.ClientNotFoundException;
import com.learn.service.TransactionService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionConsumer {
    @Value("${kafka.transaction.topic}")
    private String topic;

    private final KafkaConsumer<String, String> kafkaConsumerTransaction;
    private final TransactionService transactionService;
    private final KafkaClient kafkaClient;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void start() {
        kafkaConsumerTransaction.subscribe(Collections.singletonList(topic));
        new Thread(() -> {
            while (true) {
                kafkaConsumerTransaction.poll(Duration.ofMillis(1000)).forEach(transactionRecord -> {
                    log.info("Received message: " + transactionRecord.value());

                    TransactionDto transactionDto = null;
                    try {
                        transactionDto = objectMapper.readValue(transactionRecord.value(), TransactionDto.class);
                        transactionService.save(transactionDto);
                    } catch (JsonProcessingException e) {
                        log.error("Error parsing message", e);
                    } catch (ClientNotFoundException e) {
                        log.error("Client not found", e);
                        kafkaClient.sendTransaction(transactionDto);
                    }
                });
            }
        }).start();
    }
}
