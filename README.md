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
  - `/balance [nexcoin|nexcrypto] [player]`: View the balance of **NexCoin** or **NexCrypto** for a specific player.
  - `/pay [player] [amount] [nexcoin|nexcrypto]`: Pay a player in **NexCoin** or **NexCrypto**.
  - `/add [player] [amount] [nexcoin|nexcrypto]`: Add currency to a player’s balance.
  - `/sell [amount] [nexcoin|nexcrypto]`: Sell items for **NexCoin** or **NexCrypto**.
  - `/worth [nexcoin|nexcrypto]`: Check the current worth of **NexCoin** or **NexCrypto**.
  - `/invest [amount] [nexcoin|nexcrypto] [duration|percentage]`: Make an investment with **NexCoin** or **NexCrypto**.
  - `/adjustvalue [nexcoin|nexcrypto] [value]`: Adjust the value of **NexCoin** or **NexCrypto**.
 
  ## Requirements
- Must have Vault plugin running on the server.

## How to Setup

1. Download the plugin and place the JAR file in the `plugins` folder of your spigot server.
2. Start your server. This will generate a `config.yml` file.
3. Stop the server and configure `config.yml` according to your preferences.
4. Restart your server

## Building

To compile NexusCrypto plugin, you’ll need:

- JDK 17 (or above)
- Maven: run `mvn clean install` to build.
