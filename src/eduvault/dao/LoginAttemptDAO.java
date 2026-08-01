package eduvault.dao;

import eduvault.db.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class LoginAttemptDAO {

    public void logAttempt(String username, boolean success) {
        String sql = "INSERT INTO login_attempts (username, success) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setBoolean(2, success);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int countRecentFailedAttempts(String username, int minutes) {
        String sql = """
                SELECT COUNT(*) 
                FROM login_attempts
                WHERE username = ?
                AND success = false
                AND attempt_time >= (NOW() - INTERVAL ? MINUTE)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setInt(2, minutes);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public Timestamp getLastFailedAttemptTime(String username) {
        String sql = """
                SELECT attempt_time
                FROM login_attempts
                WHERE username = ?
                AND success = false
                ORDER BY attempt_time DESC
                LIMIT 1
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getTimestamp("attempt_time");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public int countTotalLockCycles(String username) {
        String sql = """
                SELECT COUNT(*) / 5
                FROM login_attempts
                WHERE username = ?
                AND success = false
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public void clearAttempts(String username) {
        String sql = "DELETE FROM login_attempts WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
