package io.gnupinguin.sporty.interview.rest;

import java.math.BigDecimal;

public record BetResource (
    long userId,
    long jackpotId,
    BigDecimal amount) { }
