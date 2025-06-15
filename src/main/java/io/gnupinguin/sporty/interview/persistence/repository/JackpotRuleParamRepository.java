package io.gnupinguin.sporty.interview.persistence.repository;

import io.gnupinguin.sporty.interview.persistence.model.rule.JackpotRuleParam;
import jakarta.annotation.Nonnull;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JackpotRuleParamRepository extends CrudRepository<JackpotRuleParam, Long> {

    List<JackpotRuleParam> findAllByRuleId(@Nonnull Long ruleId);

}
