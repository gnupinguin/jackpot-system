package io.gnupinguin.sporty.interview.async.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfiguration {

    @Bean
    public NewTopic jackpotBetTopic() { //TODO only for test purposes, remove in production
        return new NewTopic("jackpot-bets", 1, (short) 1);
    }

}
