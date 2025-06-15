package io.gnupinguin.sporty.interview.async.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("kafka")
public record KafkaProperties(
        String bootstrapServers,
        String jackpotBetsTopic,
        String jackpotBetsRedeliveryTopic,
        KafkaConsumerProperties consumer,
        KafkaProducerProperties producer) {
    public record KafkaConsumerProperties(
            int processingThreads,
            String groupId) {
    }

    public record KafkaProducerProperties(
            int retries,
            int retryBackoffMs,
            int batchSize,
            int lingerMs,
            int maxBlockMs) {
    }
}
