package com.learn.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.learn.client.KafkaClient;
import com.learn.dto.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TransactionController {

    private final KafkaClient kafkaClient;

    @PostMapping("/transaction")
    public Transaction createTransaction(@RequestBody Transaction transaction) throws JsonProcessingException {
        kafkaClient.sendTransaction(transaction);

        return transaction;
    }
}
