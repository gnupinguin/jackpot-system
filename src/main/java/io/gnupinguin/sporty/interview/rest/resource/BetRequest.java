package io.gnupinguin.sporty.interview.rest.resource;

import java.math.BigDecimal;

public record BetRequest(
    long userId,
    long jackpotId,
    BigDecimal amount) { }
