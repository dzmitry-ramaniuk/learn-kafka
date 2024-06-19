package com.learn.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.learn.client.KafkaClient;
import com.learn.dto.Client;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ClientController {

    private final KafkaClient kafkaClient;

    @PostMapping("/client")
    public Client createClient(@RequestBody Client client) throws JsonProcessingException {
        kafkaClient.sendClient(client);

        return client;
    }
}
