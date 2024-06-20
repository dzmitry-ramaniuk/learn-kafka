package com.learn.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.learn.dto.TransactionDto;
import com.learn.model.Client;
import com.learn.model.Transaction;
import com.learn.model.TransactionType;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.assertj.core.api.Assertions;
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
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@Testcontainers
public class TransactionTest {

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
    void testSaveTransaction() throws JsonProcessingException, InterruptedException {
        Client client = Client.builder()
                .clientId(1)
                .email("test@test.com")
                .build();
        TransactionDto transactionDto = TransactionDto.builder()
                .bank("test")
                .clientId(1)
                .transactionType(TransactionType.INCOME)
                .quantity(2)
                .price(10.0)
                .createdAt(LocalDateTime.now())
                .build();
        Transaction expectedTransaction = Transaction.builder()
                .bank(transactionDto.getBank())
                .client(client)
                .transactionType(TransactionType.INCOME)
                .quantity(transactionDto.getQuantity())
                .price(transactionDto.getPrice())
                .cost(transactionDto.getQuantity() * transactionDto.getPrice())
                .createdAt(transactionDto.getCreatedAt())
                .build();

        mapper.registerModule(new JavaTimeModule());

        ProducerRecord<String, String> clientRecord = new ProducerRecord<>(
                "client.create",
                "1",
                mapper.writeValueAsString(client));
        ProducerRecord<String, String> transactionRecord = new ProducerRecord<>(
                "transaction.create",
                "1",
                mapper.writeValueAsString(transactionDto));

        KafkaProducer<String, String> producer = createProducer();

        producer.send(clientRecord);
        Thread.sleep(1000);
        producer.send(transactionRecord);

        await()
                .pollInterval(Duration.ofSeconds(3))
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    RowMapper<Transaction> rowMapper = (rs, rowNum) -> Transaction.builder()
                            .bank(rs.getString("bank"))
                            .client(Client.builder()
                                    .clientId(rs.getInt("client_id"))
                                    .email(rs.getString("email"))
                                    .build())
                            .transactionType(TransactionType.valueOf(rs.getString("transaction_type")))
                            .quantity(rs.getInt("quantity"))
                            .price(rs.getDouble("price"))
                            .cost(rs.getDouble("cost"))
                            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                            .build();

                    Transaction dbTransaction = jdbcTemplate.queryForObject(
                            """
                                    SELECT t.*, c.*
                                    FROM transaction t JOIN client c ON t.client_id = c.id
                                    WHERE t.client_id = ?""",
                            rowMapper,
                            client.getClientId());

                    Assertions.assertThat(dbTransaction)
                            .usingRecursiveComparison()
                            .ignoringFields("id", "createdAt")
                            .isEqualTo(expectedTransaction);
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
