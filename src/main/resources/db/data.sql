-- Alice
INSERT INTO "user" (name) VALUES ('Alice'); -- suppose generated id = 1

-- Bob
INSERT INTO "user" (name) VALUES ('Bob'); -- suppose generated id = 2


-- Contribution Rule 1: FIXED
INSERT INTO jackpot_rule (type, strategy, name, created_at)
VALUES ('CONTRIBUTION', 'FIXED', 'Fixed Contribution Rule', NOW()); -- id = 1

-- Contribution Rule 2: VARIABLE
INSERT INTO jackpot_rule (type, strategy, name, created_at)
VALUES ('CONTRIBUTION', 'VARIABLE', 'Variable Contribution Rule', NOW()); -- id = 2

-- Reward Rule
INSERT INTO jackpot_rule (type, strategy, name, created_at)
VALUES ('REWARD', 'FIXED', 'Reward Rule', NOW()); -- id = 3

-- For Contribution Rule 1 (id = 1)
INSERT INTO jackpot_rule_param (rule_id, "key", "value")
VALUES (1, 'fixed_amount', 10.00);

-- For Contribution Rule 2 (id = 2)
INSERT INTO jackpot_rule_param (rule_id, "key", "value")
VALUES (2, 'percentage', 5.00),
       (2, 'min_amount', 2.00);


-- Starter Jackpot with Contribution Rule 1 and Reward Rule 3
INSERT INTO jackpot (
    name, initial_pool_amount, current_pool_amount,
    contribution_rule_id, reward_rule_id,
    created_at, updated_at
) VALUES (
    'Starter Jackpot', 1000.00, 1000.00,
    1, 3, NOW(), NOW()
); -- id = 1

-- Mega Jackpot with Contribution Rule 2 and Reward Rule 3
INSERT INTO jackpot (
    name, initial_pool_amount, current_pool_amount,
    contribution_rule_id, reward_rule_id,
    created_at, updated_at
) VALUES (
    'Mega Jackpot', 5000.00, 5000.00,
    2, 3, NOW(), NOW()
); -- id = 2
