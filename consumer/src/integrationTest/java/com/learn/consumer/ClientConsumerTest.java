package com.learn.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.model.Client;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@Testcontainers
public class ClientConsumerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Container
    static final KafkaContainer kafkaContainer = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))
            .waitingFor(Wait.forListeningPort())
            .withKraft();

    @Container
    static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:latest"))
            .withDatabaseName("transaction")
            .withUsername("postgres")
            .withPassword("postgres")

            .withInitScript("initTest.sql");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("kafka.host", kafkaContainer::getBootstrapServers);
    }

    @Test
    void testSaveClient() throws JsonProcessingException {
        Client client = Client.builder()
                .clientId(1)
                .email("test@test.com")
                .build();

        ProducerRecord<String, String> clientRecord = new ProducerRecord<>(
                "client.create",
                "1",
                mapper.writeValueAsString(client));


        KafkaProducer<String, String> producer = createProducer();
        producer.send(clientRecord);

        await()
                .pollInterval(Duration.ofSeconds(3))
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    RowMapper<Client> rowMapper = (rs, rowNum) -> Client.builder()
                            .clientId(rs.getInt("id"))
                            .email(rs.getString("email"))
                            .build();

                    Client dbClient = jdbcTemplate.queryForObject(
                            "SELECT * from client where id = ?",
                            rowMapper,
                            client.getClientId());

                    assertEquals(client, dbClient);
                });

    }

    private KafkaProducer<String, String> createProducer() {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", kafkaContainer.getBootstrapServers());
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        return new KafkaProducer<>(properties);
    }

}
