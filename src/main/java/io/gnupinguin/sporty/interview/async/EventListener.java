package io.gnupinguin.sporty.interview.async;

import io.gnupinguin.sporty.interview.async.events.BetEvent;
import io.gnupinguin.sporty.interview.async.task.BetTask;
import io.gnupinguin.sporty.interview.async.task.BetTaskQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventListener {

    private final BetEventProcessor processor;
    private final BetTaskQueue queues;

    @KafkaListener(topics = "${kafka.jackpotBetsTopic}", containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void listenEvent(BetEvent event, Acknowledgment acknowledgment) {
        queues.submit(new BetTask(event, acknowledgment, () -> processor.process(event)));
    }

}
