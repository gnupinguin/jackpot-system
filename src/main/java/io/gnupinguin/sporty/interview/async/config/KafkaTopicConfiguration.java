package io.gnupinguin.sporty.interview.async.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfiguration {

    @Bean
    public NewTopic jackpotBetTopic(KafkaProperties properties) { //TODO only for test purposes, remove in production
        return new NewTopic(properties.jackpotBetsTopic(), 1, (short) 1);
    }

}
