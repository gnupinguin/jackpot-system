package io.gnupinguin.sporty.interview.rest.resource;

import java.math.BigDecimal;

public record RewardResource(RewardStatus status, BigDecimal amount) {

    enum RewardStatus {
        Won, Loose, NotProcessed
    }

    public static RewardResource won(BigDecimal amount) {
        return new RewardResource(RewardStatus.Won, amount);
    }

    public static RewardResource loose() {
        return new RewardResource(RewardStatus.Loose, BigDecimal.ZERO);
    }

    public static RewardResource notProcessed() {
        return new RewardResource(RewardStatus.NotProcessed, BigDecimal.ZERO);
    }
}
