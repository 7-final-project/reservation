package com.qring.reservation.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConfig {
//    @Value("#{${kafka.topic}}")
//    private Map<String, String> topics;
//
//    @Bean
//    public List<NewTopic> kafkaTopicList() {
//        List<NewTopic> topicList = new ArrayList<>();
//        topics.forEach((key, topicName) -> {
//            topicList.add(new NewTopic(topicName, 1, (short) 1));
//        });
//        return topicList;
//    }
    @Value("${spring.kafka.topic.reservation-create-event}")
    private String reservationCreateEventTopic;

    @Bean
    public NewTopic reservationCreateEventTopic() {
        return new NewTopic(reservationCreateEventTopic, 1, (short) 1);
    }
}
