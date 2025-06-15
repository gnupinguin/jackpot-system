package io.gnupinguin.sporty.interview.async.events;

import io.gnupinguin.sporty.interview.persistence.model.Bet;

public record BetEvent(String eventId, Bet bet) {

}
