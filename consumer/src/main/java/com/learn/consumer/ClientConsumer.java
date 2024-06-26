package com.learn.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.model.Client;
import com.learn.service.ClientService;
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
public class ClientConsumer {
    @Value("${kafka.client.topic}")
    private String topic;

    private final KafkaConsumer<String, String> kafkaConsumerClient;
    private final ClientService clientService;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void start() {
        kafkaConsumerClient.subscribe(Collections.singletonList(topic));
        new Thread(() -> {
            while (true) {
                kafkaConsumerClient.poll(Duration.ofMillis(1000)).forEach(clientRecord -> {
                    log.info("Received message: {}", clientRecord.value());
                    try {
                        clientService.save(objectMapper.readValue(clientRecord.value(), Client.class));
                    } catch (JsonProcessingException e) {
                        log.error("Error parsing message", e);
                    } catch (Exception e) {
                        log.error("Error saving client", e);
                    }
                });
            }
        }).start();
    }
}
