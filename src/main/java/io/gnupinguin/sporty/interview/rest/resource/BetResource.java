package io.gnupinguin.sporty.interview.rest.resource;

import java.math.BigDecimal;

public record BetResource(long id, long userId, long jackpotId, BigDecimal amount) {
}
