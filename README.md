## NexCrypto

**NexCrypto** is a powerful, customizable economy plugin that integrates cryptocurrency-like features into the game. Players can use two types of currencies: **NexCoin** (similar to USD) and **NexCrypto** (similar to Bitcoin). This plugin allows players to earn currency, invest in the economy, and experience market volatility, just like in real-world crypto markets.

With a fully customizable economy system, players can adjust rates, make investments, and track their progress in real-time. The plugin also supports **PlaceholderAPI** integration, making it easy to display essential economy data in-game.

### Features:
- **Dual Currency System**:
  - **NexCoin**: The stable in-game currency.
  - **NexCrypto**: A more volatile currency, similar to cryptocurrency.
  
- **Customizable Economy Settings**:
  - `baseRate`: The initial exchange rate for currency conversion (default: 1.05).
  - `volatilityFactor`: Controls the volatility of **NexCrypto** (default: 1.0).
  - `profitChance`: The chance for players to profit from investments (default: 0.5).
  - `lossFactor`: The potential for loss during investments (default: 0.5).
  - `NexCryptoToUSDRate`: The conversion rate from **NexCrypto** to USD (default: 40).

- **Investment System**: Players can invest their **NexCoin** or **NexCrypto** and experience profits or losses based on market trends.

- **Placeholders API Support**:
  - **`%nexus_nexcoin_balance%`**: Displays the **NexCoin** balance of a player.
  - **`%nexus_nexcrypto_balance%`**: Displays the **NexCrypto** balance of a player.
  - **`%nexus_nexcrypto_invested%`**: Shows the total amount of **NexCrypto** a player has invested.
  - **`%nexus_nexcrypto_worth%`**: Displays the current worth of **NexCrypto** in USD.

- **SQLite Integration**: The plugin stores all player data in an SQLite database, making it lightweight and efficient.

- **Command List**:
  - `/nexcoin balance nexcoin [player]`: View the **NexCoin** balance for a specific player.
  - `/nexcoin balance nexcrypto [player]`: View the **NexCrypto** balance for a specific player.
  - `/nexcoin pay [player] [amount] nexcoin`: Pay a player in **NexCoin**.
  - `/nexcoin pay [player] [amount] nexcrypto`: Pay a player in **NexCrypto**.
  - `/nexcoin add [player] [amount] nexcoin`: Add **NexCoin** to a player’s balance.
  - `/nexcoin add [player] [amount] nexcrypto`: Add **NexCrypto** to a player’s balance.
  - `/nexcoin sell [amount] nexcoin`: Sell items for **NexCoin**.
  - `/nexcoin sell [amount] nexcrypto`: Sell items for **NexCrypto**.
  - `/nexcoin worth nexcoin`: Check the current worth of **NexCoin**.
  - `/nexcoin worth nexcrypto`: Check the current worth of **NexCrypto**.
  - `/nexcoin invest [amount] nexcoin [duration|percentage]`: Make an investment with **NexCoin**.
  - `/nexcoin invest [amount] nexcrypto [duration|percentage]`: Make an investment with **NexCrypto**.
  - `/nexcoin adjustvalue nexcoin [value]`: Adjust the value of **NexCoin**.
  - `/nexcoin adjustvalue nexcrypto [value]`: Adjust the value of **NexCrypto**.

## Requirements
- Must have **Vault** plugin running on the server.

## How to Setup

1. Download the plugin and place the JAR file in the `plugins` folder of your Spigot server.
2. Start your server. This will generate a `config.yml` file.
3. Stop the server and configure `config.yml` according to your preferences.
4. Restart your server.

## Building

To compile **NexCrypto** plugin, you’ll need:

- **JDK 17** (or above)
- **Maven**: run `mvn clean install` to build.
