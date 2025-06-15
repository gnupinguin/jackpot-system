package io.gnupinguin.sporty.interview.async.events;

public record BetEvent(String eventId, long userId, long jackpotId, long amount) {

}
