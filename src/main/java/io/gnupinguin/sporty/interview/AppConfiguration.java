package io.gnupinguin.sporty.interview;

import io.gnupinguin.sporty.interview.common.ChanceGenerator;
import io.gnupinguin.sporty.interview.common.RandomChanceGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.security.SecureRandom;
import java.time.Clock;

@Configuration
public class AppConfiguration {

    @Bean
    public NamedParameterJdbcOperations namedParameterJdbcOperations(DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public ChanceGenerator chanceGenerator() {
        return new RandomChanceGenerator(new SecureRandom());
    }

}
