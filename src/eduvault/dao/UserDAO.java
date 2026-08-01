package eduvault.dao;

import eduvault.db.DatabaseConnection;
import eduvault.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

    public void insertUser(User user) {
        String sql = "INSERT INTO users (username, email, phone_number, password_hash, salt, master_wrap_salt, vault_key_master_encrypted, vault_key_master_iv, vault_key_recovery_encrypted, vault_key_recovery_iv, recovery_words_count, recovery_version) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPhoneNumber());
            stmt.setString(4, user.getPasswordHash());
            stmt.setString(5, user.getSalt());
            stmt.setString(6, user.getMasterWrapSalt());
            stmt.setString(7, user.getVaultKeyMasterEncrypted());
            stmt.setString(8, user.getVaultKeyMasterIv());
            stmt.setString(9, user.getVaultKeyRecoveryEncrypted());
            stmt.setString(10, user.getVaultKeyRecoveryIv());
            stmt.setInt(11, user.getRecoveryWordsCount());
            stmt.setString(12, user.getRecoveryVersion());

            stmt.executeUpdate();
            System.out.println("Kullanıcı eklendi!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setPhoneNumber(rs.getString("phone_number"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setSalt(rs.getString("salt"));

                user.setMasterWrapSalt(rs.getString("master_wrap_salt"));
                user.setVaultKeyMasterEncrypted(rs.getString("vault_key_master_encrypted"));
                user.setVaultKeyMasterIv(rs.getString("vault_key_master_iv"));
                user.setVaultKeyRecoveryEncrypted(rs.getString("vault_key_recovery_encrypted"));
                user.setVaultKeyRecoveryIv(rs.getString("vault_key_recovery_iv"));
                user.setRecoveryWordsCount(rs.getInt("recovery_words_count"));
                user.setRecoveryVersion(rs.getString("recovery_version"));

                user.setCreatedAt(rs.getTimestamp("created_at"));
                return user;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    public boolean updateUserCredentialsAndMasterWrap(User user) {
    String sql = "UPDATE users SET password_hash = ?, salt = ?, master_wrap_salt = ?, vault_key_master_encrypted = ?, vault_key_master_iv = ? WHERE username = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, user.getPasswordHash());
        stmt.setString(2, user.getSalt());
        stmt.setString(3, user.getMasterWrapSalt());
        stmt.setString(4, user.getVaultKeyMasterEncrypted());
        stmt.setString(5, user.getVaultKeyMasterIv());
        stmt.setString(6, user.getUsername());

        int affectedRows = stmt.executeUpdate();
        return affectedRows > 0;

    } catch (Exception e) {
        e.printStackTrace();
    }

    return false;
}
}