package io.gnupinguin.sporty.interview.processor;

import io.gnupinguin.sporty.interview.persistence.model.rule.RuleStrategy;
import io.gnupinguin.sporty.interview.processor.contribution.JackpotRuleContributor;
import io.gnupinguin.sporty.interview.processor.reward.JackpotRuleRewarder;
import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JackpotRuleProcessorProvider {

    private final Map<RuleStrategy, JackpotRuleContributor> contributors;
    private final Map<RuleStrategy, JackpotRuleRewarder> rewarders;

    public JackpotRuleProcessorProvider(List<JackpotRuleContributor> contributors,
                                        List<JackpotRuleRewarder> rewarders) {
        this.contributors = contributors.stream().collect(Collectors.toMap(JackpotRuleContributor::getStrategy, Function.identity()));
        this.rewarders = rewarders.stream().collect(Collectors.toMap(JackpotRuleRewarder::getStrategy, Function.identity()));
    }

    @Nonnull
    public JackpotRuleContributor getContributor(@Nonnull RuleStrategy strategy) {
        var contributor = contributors.get(strategy);
        if (contributor == null) {
            throw new IllegalArgumentException("No contributor found for strategy: " + strategy); //TODO handle this more gracefully
        }
        return contributor;
    }

    @Nonnull
    public JackpotRuleRewarder getRewarder(@Nonnull RuleStrategy strategy) {
        var processor = rewarders.get(strategy);
        if (processor == null) {
            throw new IllegalArgumentException("No rewarder found for strategy: " + strategy); //TODO handle this more gracefully
        }
        return processor;
    }

}
