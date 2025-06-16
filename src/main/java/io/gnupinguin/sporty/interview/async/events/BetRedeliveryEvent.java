package io.gnupinguin.sporty.interview.async.events;

public record BetRedeliveryEvent(String eventId, long betId, String reason) {
}
