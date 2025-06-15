
# 🎰 Jackpot System – Architecture & Logic

## 📦 Overview

This project implements a modular, event-driven **jackpot system** that supports:

- Variable and fixed **contribution rules**
- Variable and fixed **reward strategies**
- Clean separation of concerns between persistence, business logic, and asynchronous processing

## ⚙️ Architectural Highlights

- **Spring Boot + Spring JDBC** for lightweight, testable infrastructure
- **Kafka-based async processing** of bets for contribution handling
- **Transactional reward check logic**, ensuring consistency and isolation
- **Pluggable rule system** via database-driven configuration:
    - `jackpot_rule`, `jackpot_rule_param` define dynamic behaviors

## 🎰 Jackpot Contribution Logic

### 🔄 Trigger

Handled **asynchronously** via Kafka consumer when a bet is placed.

### 🧠 Flow

1. Fetch the **jackpot contribution rule** for the bet's jackpot
2. Depending on the rule strategy (`FIXED` or `VARIABLE`):
    - **FIXED**: contribute a fixed percentage of the bet amount
      ```
      contribution = bet.amount × rate
      ```
    - **VARIABLE**: contribution rate decreases as pool grows
      ```
      effective_rate = initial_rate - steps × decrease_rate
      contribution = bet.amount × max(effective_rate, min_rate)
      ```
3. Increment the jackpot pool
4. Persist a `jackpot_contribution` record for auditability

## 🏆 Jackpot Reward Logic

### ⚡ Trigger

Checked **synchronously** (e.g., HTTP request or async scheduler).

### 🧠 Flow

1. Fetch the **reward rule** for the jackpot
2. Check if a reward has already been issued (`jackpot_reward` exists or `last_rewarded_at` set)
3. Depending on strategy:
    - **FIXED**: use a static win probability
      ```
      if chanceGenerator.won(chance): issue reward
      ```
    - **VARIABLE**: chance scales with pool size
      ```
      effective_chance = min(pool × increase_rate, max_chance)
      if chanceGenerator.won(effective_chance): issue reward
      ```
4. If reward is issued:
    - Persist a `jackpot_reward` record
    - Optionally reset the jackpot pool
    - Update `last_rewarded_at` for concurrency control

## 🔒 Concurrency & Safety

- `SELECT ... FOR UPDATE` ensures **atomic jackpot locking**
- Uniqueness on `jackpot_reward(jackpot_id)` prevents double payout
- Optional `last_rewarded_at` timestamp used to reject stale bet rewards
