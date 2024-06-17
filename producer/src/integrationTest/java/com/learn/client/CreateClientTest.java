package com.learn.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.learn.dto.Client;
import com.learn.dto.Transaction;
import com.learn.dto.TransactionType;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@Testcontainers
@AutoConfigureMockMvc
public class CreateClientTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Container
    static final KafkaContainer kafkaContainer = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))
            .waitingFor(Wait.forListeningPort())
            .withKraft();

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("kafka.host", kafkaContainer::getBootstrapServers);
    }

    @Test
    public void saveNewClient() throws Exception {
        Client client = Client.builder()
                .clientId(2L)
                .email("test@test.com")
                .build();

        mockMvc.perform(MockMvcRequestBuilders
                .post("/client")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(client)));

        KafkaConsumer<String, String> consumer = createConsumer();
        consumer.subscribe(Collections.singletonList("client.create"));

        await()
                .pollInterval(Duration.ofSeconds(3))
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));

                    if (records.isEmpty()) {
                        Assertions.fail("Message not found");
                    }

                    for (var record : records) {
                        Client result = mapper.readValue(record.value(), Client.class);

                        Assertions.assertEquals(client, result);
                    }
                });

        consumer.close();
    }

    @Test
    void saveNewTransaction() throws Exception {
        mapper.registerModule(new JavaTimeModule());

        Transaction transaction = Transaction.builder()
                .clientId(1L)
                .bank("TEST_BANK")
                .transactionType(TransactionType.INCOME)
                .quantity(1)
                .price(100D)
                .createdAt(LocalDateTime.now())
                .build();

        mockMvc.perform(MockMvcRequestBuilders
                .post("/transaction")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(transaction)));


        KafkaConsumer<String, String> consumer = createConsumer();
        consumer.subscribe(Collections.singletonList("transaction.create"));

        await()
                .pollInterval(Duration.ofSeconds(3))
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));

                    if (records.isEmpty()) {
                        Assertions.fail("Message not found");
                    }

                    for (var record : records) {
                        Transaction result = mapper.readValue(record.value(), Transaction.class);

                        Assertions.assertEquals(transaction, result);
                    }
                });

        consumer.close();
    }

    public KafkaConsumer<String, String> createConsumer() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group-id");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return new KafkaConsumer<>(properties);
    }
}
