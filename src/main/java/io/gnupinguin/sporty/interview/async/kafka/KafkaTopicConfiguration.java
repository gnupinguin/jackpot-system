package io.gnupinguin.sporty.interview.async.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfiguration {

    @Bean
    public NewTopic jackpotBetTopic(KafkaProperties properties) { //TODO only for test purposes, remove in production
        return new NewTopic(properties.jackpotBetsTopic(), 1, (short) 1);
    }

    @Bean
    public NewTopic jackpotBetRedeliveryTopic(KafkaProperties properties) {
        return new NewTopic(properties.jackpotBetsRedeliveryTopic(), 1, (short) 1);
    }

}
