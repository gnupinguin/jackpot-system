package io.gnupinguin.sporty.interview.persistence.repository;

import io.gnupinguin.sporty.interview.persistence.model.rule.JackpotRule;
import io.gnupinguin.sporty.interview.persistence.model.rule.JackpotRuleType;
import io.gnupinguin.sporty.interview.persistence.model.rule.JackpotRules;
import jakarta.annotation.Nonnull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Repository
public interface JackpotRuleRepository extends CrudRepository<JackpotRule, Long> {

    @Nonnull
    default JackpotRules findAllGroupedByType(List<Long> ids) {
        var rules = StreamSupport.stream(findAllById(ids).spliterator(), false)
                .collect(Collectors.toMap(JackpotRule::type, Function.identity())); //TODO handle missing rules more gracefully
        return new JackpotRules(rules.get(JackpotRuleType.CONTRIBUTION), rules.get(JackpotRuleType.REWARD));
    }

}
