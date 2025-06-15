package io.gnupinguin.sporty.interview.async.processor.reward;

import io.gnupinguin.sporty.interview.persistence.model.rule.RuleStrategy;
import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JackpotRuleRewarderProvider {

    private final Map<RuleStrategy, JackpotRuleRewarder> rewarders;

    public JackpotRuleRewarderProvider(List<JackpotRuleRewarder> processors) {
        this.rewarders = processors.stream().collect(Collectors.toMap(JackpotRuleRewarder::getStrategy, Function.identity()));
    }

    @Nonnull
    public JackpotRuleRewarder getRewarder(@Nonnull RuleStrategy strategy) {
        var processor = rewarders.get(strategy);
        if (processor == null) {
            throw new IllegalArgumentException("No contributor found for strategy: " + strategy); //TODO handle this more gracefully
        }
        return processor;
    }

}
