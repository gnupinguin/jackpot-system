package io.gnupinguin.sporty.interview.async.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

@Configuration
public class KafkaProducerConfiguration {

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> pf) {
        return new KafkaTemplate<>(pf);
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory(KafkaProperties properties) {
        Map<String, Object> config = Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.bootstrapServers(),
                ProducerConfig.RETRIES_CONFIG, properties.producer().retries(),
                ProducerConfig.RETRY_BACKOFF_MS_CONFIG, properties.producer().retryBackoffMs(),
                ProducerConfig.BATCH_SIZE_CONFIG, properties.producer().batchSize(),
                ProducerConfig.MAX_BLOCK_MS_CONFIG, properties.producer().maxBlockMs(),
                ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip",
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class
        );
        return new DefaultKafkaProducerFactory<>(config);
    }

}
