package com.learn.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class CreateClientTest {

    private static final String TOPIC = "client.create";

    private KafkaContainer kafkaContainer;

    @BeforeEach
    public void setUp() {
        kafkaContainer = new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"));
        kafkaContainer.start();


//        String bootstrapServers = kafkaContainer.getBootstrapServers();
    }

    @AfterEach
    public void tearDown() {
        kafkaContainer.stop();
    }

    @Test
    public void saveNewClient() {
        Assertions.assertTrue(true);
    }
}
