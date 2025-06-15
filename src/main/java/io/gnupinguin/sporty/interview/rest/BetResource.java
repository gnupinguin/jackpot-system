package io.gnupinguin.sporty.interview.rest;

public record BetResource (
    long userId,
    long jackpotId,
    long amount) { }
