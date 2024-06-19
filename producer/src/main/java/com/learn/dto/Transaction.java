package com.learn.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    private String bank;
    private Long clientId;
    private TransactionType transactionType;
    private Integer quantity;
    private Double price;
    private LocalDateTime createdAt;
}
