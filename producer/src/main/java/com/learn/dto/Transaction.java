package com.learn.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Transaction {
    private String bank;
    private Long clientId;
    private TransactionType transactionType;
    private Integer quantity;
    private Double price;
    private LocalDateTime createdAt;
}
