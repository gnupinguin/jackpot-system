package io.gnupinguin.sporty.interview.async.task;

import io.gnupinguin.sporty.interview.async.BetPublisher;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RequiredArgsConstructor
public class JackpotBetTaskQueue implements BetTaskQueue {

    private final BetPublisher betPublisher;

    //TODO restrict count of workers to prevent resource exhaustion
    private final Map<Long, BlockingQueue<BetTask>> taskQueues = new ConcurrentHashMap<>();
    private final Map<Long, AtomicBoolean> activeWorkers = new ConcurrentHashMap<>();
    private final ExecutorService executor;

    @Override
    public void submit(@Nonnull BetTask task) {
        taskQueues.computeIfAbsent(task.event().jackpotId(), jackpotId -> new LinkedBlockingQueue<>(50)).offer(task);
        startWorkerIfAbsent(task.event().jackpotId());
    }

    private void startWorkerIfAbsent(@Nonnull Long jackpotId) {
        activeWorkers.computeIfAbsent(jackpotId, id -> new AtomicBoolean(false));

        if (activeWorkers.get(jackpotId).compareAndSet(false, true)) {
            executor.submit(() -> {
                try {
                    var queue = taskQueues.get(jackpotId);
                    while (true) {
                        BetTask task = queue.poll(30, TimeUnit.SECONDS); // Idle timeout
                        if (task == null) {
                            break; // No task for 30s — shutdown worker
                        }
                        for (; task.retries() < 3; task = task.nextAttempt()) { // Retry up to 3 times
                            try {
                                task.processor().run();
                                break; // Exit retry loop on success
                            } catch (Exception e) {
                                log.error("Error processing task for jackpot ID {}: {}", jackpotId, task.event(), e);
                            }
                        }
                        if (task.retries() >= 3) {
                            log.warn("Task for jackpot ID {} failed after 3 attempts: {}", jackpotId, task.event());
                            betPublisher.redelivery(task.event().betId(), "Failed after 3 retries");
                        }
                        task.ack().acknowledge(); // Acknowledge the task
                    }
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt(); // Restore interrupted status
                    log.info("Worker for jackpot ID {} was interrupted", jackpotId);
                } catch (Exception e) {
                    log.error("Unexpected error in worker for jackpot ID {}", jackpotId, e);
                } finally {
                    activeWorkers.remove(jackpotId);
                    taskQueues.remove(jackpotId); // Optional: clean up empty queue
                }
            });
        }
    }
}
