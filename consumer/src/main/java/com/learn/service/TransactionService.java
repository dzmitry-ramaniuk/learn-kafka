package com.learn.service;

import com.learn.dto.TransactionDto;
import com.learn.exception.ClientNotFoundException;
import com.learn.model.Client;
import com.learn.model.Transaction;
import com.learn.repository.ClientRepository;
import com.learn.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final ClientRepository clientRepository;

    public void save(TransactionDto transactionDto) {
        Optional<Client> client = clientRepository.findById(transactionDto.getClientId());

        if (client.isEmpty()) {
            log.error("Client not found, id: {} ", transactionDto.getClientId());
            throw new ClientNotFoundException("Client not found, id: " + transactionDto.getClientId());
        }


        Transaction transaction = Transaction.builder()
                .bank(transactionDto.getBank())
                .client(client.get())
                .transactionType(transactionDto.getTransactionType())
                .quantity(transactionDto.getQuantity())
                .price(transactionDto.getPrice())
                .cost(transactionDto.getQuantity() * transactionDto.getPrice())
                .createdAt(transactionDto.getCreatedAt())
                .build();

        transactionRepository.save(transaction);
    }
}
