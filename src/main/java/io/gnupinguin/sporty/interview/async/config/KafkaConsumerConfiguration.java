package io.gnupinguin.sporty.interview.async.config;

import io.gnupinguin.sporty.interview.async.BetPublisher;
import io.gnupinguin.sporty.interview.async.events.BetEvent;
import io.gnupinguin.sporty.interview.async.task.JackpotBetTaskQueue;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Map;
import java.util.concurrent.Executors;

@Configuration
public class KafkaConsumerConfiguration {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(ConsumerFactory<String, Object> cf, KafkaProperties properties) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, Object>();
        factory.setConsumerFactory(cf);
        factory.setConcurrency(properties.consumer().processingThreads());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory(KafkaProperties properties) {
        JsonDeserializer<Object> deserializer = new JsonDeserializer<>();
        deserializer.addTrustedPackages(BetEvent.class.getPackageName());

        Map<String, Object> props = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.bootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, properties.consumer().groupId(),
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer
        );

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public JackpotBetTaskQueue jackpotBetTaskQueue(KafkaProperties properties, BetPublisher betPublisher) {
        return new JackpotBetTaskQueue(betPublisher, Executors.newVirtualThreadPerTaskExecutor());
    }

}
