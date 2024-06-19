package com.learn.config;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

@Configuration
public class KafkaConfig {
    @Value("${kafka.host}")
    private String kafkaHost;

    @Bean
    public KafkaProducer kafkaProducer() throws UnknownHostException {
        Properties properties = new Properties();
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, InetAddress.getLocalHost().getHostName());
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost);
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new KafkaProducer(properties);
    }
}
