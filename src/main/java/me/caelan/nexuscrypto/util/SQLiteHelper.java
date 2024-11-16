package me.caelan.nexuscrypto.util;

import me.caelan.nexuscrypto.Investment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SQLiteHelper {

    private static final String DATABASE_URL = "jdbc:sqlite:plugins/NexusCrypto/nexuscrypto.db";

    public SQLiteHelper() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try (Connection conn = this.connect(); Statement stmt = conn.createStatement()) {
            String createNexCryptoTable = "CREATE TABLE IF NOT EXISTS nexcrypto (" +
                    "player_uuid TEXT PRIMARY KEY," +
                    "nexcrypto_balance REAL DEFAULT 0.0," +
                    "nexcrypto_value REAL DEFAULT 1.0)";
            stmt.execute(createNexCryptoTable);

            String createInvestmentsTable = "CREATE TABLE IF NOT EXISTS investments (" +
                    "player_uuid TEXT," +
                    "amount REAL," +
                    "start_time INTEGER," +
                    "end_time INTEGER," +
                    "currency_type TEXT," +
                    "profit_loss REAL," +
                    "is_profit BOOLEAN," +
                    "message TEXT)";
            stmt.execute(createInvestmentsTable);

            String createTotalNexCryptoInvestedTable = "CREATE TABLE IF NOT EXISTS total_nexcrypto_invested (" +
                    "player_uuid TEXT PRIMARY KEY," +
                    "total_invested REAL DEFAULT 0.0)";
            stmt.execute(createTotalNexCryptoInvestedTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DATABASE_URL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    public List<UUID> getAllPlayerUUIDs() {
        List<UUID> playerUUIDs = new ArrayList<>();
        String sql = "SELECT player_uuid FROM nexcrypto";
        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                playerUUIDs.add(UUID.fromString(rs.getString("player_uuid")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return playerUUIDs;
    }

    public boolean updateNexCryptoBalance(String playerUUID, double balance) {
        String sql = "INSERT OR REPLACE INTO nexcrypto(player_uuid, nexcrypto_balance) VALUES(?, ?)";
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID);
            pstmt.setDouble(2, balance);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public double getNexCryptoBalance(String playerUUID) {
        String sql = "SELECT nexcrypto_balance FROM nexcrypto WHERE player_uuid = ?";
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("nexcrypto_balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public boolean updateNexCryptoValue(String playerUUID, double value) {
        String sql = "INSERT OR REPLACE INTO nexcrypto(player_uuid, nexcrypto_value) VALUES(?, ?)";
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID);
            pstmt.setDouble(2, value);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public double getNexCryptoValue(String playerUUID) {
        String sql = "SELECT nexcrypto_value FROM nexcrypto WHERE player_uuid = ?";
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("nexcrypto_value");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1.0;
    }

    public void addInvestment(String playerUUID, double amount, long startTime, long endTime, String currencyType, String message) {
        String sql = "INSERT INTO investments(player_uuid, amount, start_time, end_time, currency_type, message) VALUES(?, ?, ?, ?, ?, ?)";
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID);
            pstmt.setDouble(2, amount);
            pstmt.setLong(3, startTime);
            pstmt.setLong(4, endTime);
            pstmt.setString(5, currencyType);
            pstmt.setString(6, message);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Investment> getInvestments() {
        List<Investment> investments = new ArrayList<>();
        String sql = "SELECT player_uuid, amount, start_time, end_time, currency_type, message FROM investments";
        try (Connection conn = this.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                investments.add(new Investment(
                        UUID.fromString(rs.getString("player_uuid")),
                        rs.getDouble("amount"),
                        rs.getLong("start_time"),
                        rs.getLong("end_time"),
                        rs.getString("currency_type"),
                        rs.getString("message")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return investments;
    }

    public void removeInvestment(UUID playerUUID) {
        String sql = "DELETE FROM investments WHERE player_uuid = ?";
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateInvestmentResult(UUID playerUUID, double result, boolean isProfit) {
        String sql = "UPDATE investments SET profit_loss = ?, is_profit = ? WHERE player_uuid = ?";
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, result);
            pstmt.setBoolean(2, isProfit);
            pstmt.setString(3, playerUUID.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean updateTotalNexCryptoInvested(String playerUUID, double amount) {
        String selectSql = "SELECT total_invested FROM total_nexcrypto_invested WHERE player_uuid = ?";
        String updateSql = "UPDATE total_nexcrypto_invested SET total_invested = ? WHERE player_uuid = ?";
        String insertSql = "INSERT INTO total_nexcrypto_invested (player_uuid, total_invested) VALUES (?, ?)";

        try (Connection conn = this.connect()) {
            PreparedStatement selectPstmt = conn.prepareStatement(selectSql);
            selectPstmt.setString(1, playerUUID);
            ResultSet rs = selectPstmt.executeQuery();

            if (rs.next()) {
                double currentTotal = rs.getDouble("total_invested");
                double newTotal = currentTotal + amount;

                PreparedStatement updatePstmt = conn.prepareStatement(updateSql);
                updatePstmt.setDouble(1, newTotal);
                updatePstmt.setString(2, playerUUID);
                updatePstmt.executeUpdate();
            } else {
                PreparedStatement insertPstmt = conn.prepareStatement(insertSql);
                insertPstmt.setString(1, playerUUID);
                insertPstmt.setDouble(2, amount);
                insertPstmt.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public double getTotalNexCryptoInvested(String playerUUID) {
        String sql = "SELECT total_invested FROM total_nexcrypto_invested WHERE player_uuid = ?";
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total_invested");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
}
