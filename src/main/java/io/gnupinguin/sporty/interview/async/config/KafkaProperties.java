package io.gnupinguin.sporty.interview.async.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("kafka")
public record KafkaProperties(
        String bootstrapServers,
        String jackpotBetsTopic,
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
            int maxBlockMs) {
    }
}
