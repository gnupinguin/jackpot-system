
# Jackpot System 

## Local run

If you want to run the project locally, you need to have a running instance of Kafka.
If you don't have it, you can run it with Docker (the topics are created automatically via main application):

```bash
docker compose -up -d
```

Then you can run the project with:

```bash
./gradlew bootRun
```
By default, the application will run on port `8080`. 

## Database
Application runs a local H2 database for persistence.
The database schema and initial data are stored in `src/main/resources/db` folder.
If you want to get a db access, you can use h2-console: `http://localhost:8080/h2-console`.
The db settings are in `src/main/resources/application.yml` file.

Initially, two jackpots with different rules and rewards are created. There are two users.

## API
There are two endpoints available:
- **POST /bet**: Place a bet and contribute to the jackpot. It's partially synchronous, as it processes the bet immediately but contributes to the jackpot and issues a reward asynchronously via Kafka.
- **GET /bet/{betId}/reward**: Check if the bet wan a jackpot reward.

To test the jackpot system, you can use swagger UI: `http://localhost:8080/test/ui`.

## Flow overview
The bet processing flow is as follows:
1. **Place Bet**: User places a bet via the `/bet` endpoint.
    - The bet is persisted in the database.
    - A Kafka message is sent to the `jackpot-bets` topic with the bet details.
2. **Contribute to Jackpot and issue reward**: The bet is processed asynchronously by a Kafka consumer that:
   - Fetches the jackpot contribution rule. It locks the jackpot to ensure atomicity.
   - Calculates the contribution based on the bet amount and the rule strategy (fixed or variable).
   - Updates the jackpot pool and persists a record of the contribution.
   - Checks if a reward can be issued based on the jackpot's reward rule.
   - If a reward can be issued, it persists a record of the reward and resets the jackpot pool.
   - After all, a bet is marked as processed.
3. **Check Reward**: User can check if the bet won a reward via `/bet/check-reward` endpoint.
   - If the bet hadn't been processed yet, it returns `NotProcessed` status.

### Jackpot Contribution Logic

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

### Jackpot Reward Logic

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

## Code Highlights
- **BetEndpoint**: Handles incoming bets and checks rewards.
- **EventListener**: Kafka consumer that processes bets asynchronously.

## Architecture Notes

- place bet is a partially synchronous operation, as it processes the bet immediately but contributes to the jackpot and issues a reward asynchronously via Kafka.
- Thanks to asynchronous processing via Kafka, the system can handle high loads without blocking the main thread.
- Contribution and reward rules are fetched from the database to allow dynamic configuration.
- Optimistic lock is used to ensure atomicity when updating the jackpot pool.

## Future Improvements
It's a simplified version of the jackpot system, and it can be extended with more complex rules, user management, and error handling.

- Error handling: Currently, the system does not handle errors in a robust way. It can be improved by adding proper error handling and logging.
- Redelivery: The system does not handle message redelivery in case of failures. It can be improved by adding a dead-letter queue or retry mechanism.
- Rule parameters: they are always numeric, but in general it's better to store it as a string and parse on extraction.
- Rule cache: Currently, the rules are fetched from the database every time. It can be improved by caching the rules in memory to reduce database load.
- Better testing: The current tests are basic and can be improved by adding more test cases and scenarios.