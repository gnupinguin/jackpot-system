package io.gnupinguin.sporty.interview.persistence.model;

import java.math.BigDecimal;
import java.time.Instant;

public record RewardedBet(long betId, boolean processed, long jackpotId, Long rewardId, BigDecimal rewardAmount, Instant createdAt) {
}
