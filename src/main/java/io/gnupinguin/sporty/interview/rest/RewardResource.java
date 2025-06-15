package io.gnupinguin.sporty.interview.rest;

import java.math.BigDecimal;

public record RewardResource(boolean won, BigDecimal amount) {
}
