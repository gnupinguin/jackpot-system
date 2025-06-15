package io.gnupinguin.sporty.interview.async.processor.contribution;

import io.gnupinguin.sporty.interview.persistence.model.rule.RuleStrategy;
import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JackpotRuleContributorProvider {

    private final Map<RuleStrategy, JackpotRuleContributor> processors;

    public JackpotRuleContributorProvider(List<JackpotRuleContributor> processors) {
        this.processors = processors.stream().collect(Collectors.toMap(JackpotRuleContributor::getStrategy, Function.identity()));
    }

    @Nonnull
    public JackpotRuleContributor getContributor(RuleStrategy strategy) {
        var processor = processors.get(strategy);
        if (processor == null) {
            throw new IllegalArgumentException("No contributor found for strategy: " + strategy); //TODO handle this more gracefully
        }
        return processor;
    }

}
