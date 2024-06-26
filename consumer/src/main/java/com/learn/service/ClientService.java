package com.learn.service;

import com.learn.model.Client;
import com.learn.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;

    public void save(Client client) {
        clientRepository.save(client);
    }
}
