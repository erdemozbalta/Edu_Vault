package eduvault.dao;

import eduvault.db.DatabaseConnection;
import eduvault.model.VaultEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class VaultEntryDAO {

    public void insertVaultEntry(VaultEntry entry) {
        String sql = "INSERT INTO vault_entries (user_id, site_name, site_url, account_username, encrypted_password, iv, note) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, entry.getUserId());
            stmt.setString(2, entry.getSiteName());
            stmt.setString(3, entry.getSiteUrl());
            stmt.setString(4, entry.getAccountUsername());
            stmt.setString(5, entry.getEncryptedPassword());
            stmt.setString(6, entry.getIv());
            stmt.setString(7, entry.getNote());

            stmt.executeUpdate();
            System.out.println("Vault kaydı eklendi!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<VaultEntry> getEntriesByUserId(int userId) {
        List<VaultEntry> entries = new ArrayList<>();
        String sql = "SELECT * FROM vault_entries WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                VaultEntry entry = new VaultEntry();
                entry.setId(rs.getInt("id"));
                entry.setUserId(rs.getInt("user_id"));
                entry.setSiteName(rs.getString("site_name"));
                entry.setSiteUrl(rs.getString("site_url"));
                entry.setAccountUsername(rs.getString("account_username"));
                entry.setEncryptedPassword(rs.getString("encrypted_password"));
                entry.setIv(rs.getString("iv"));
                entry.setNote(rs.getString("note"));
                entry.setCreatedAt(rs.getTimestamp("created_at"));
                entry.setUpdatedAt(rs.getTimestamp("updated_at"));

                entries.add(entry);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return entries;
    }
    public boolean updateVaultEntry(VaultEntry entry) {
    String sql = "UPDATE vault_entries SET site_name = ?, site_url = ?, account_username = ?, encrypted_password = ?, iv = ?, note = ? WHERE id = ? AND user_id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, entry.getSiteName());
        stmt.setString(2, entry.getSiteUrl());
        stmt.setString(3, entry.getAccountUsername());
        stmt.setString(4, entry.getEncryptedPassword());
        stmt.setString(5, entry.getIv());
        stmt.setString(6, entry.getNote());
        stmt.setInt(7, entry.getId());
        stmt.setInt(8, entry.getUserId());

        int affectedRows = stmt.executeUpdate();
        return affectedRows > 0;

    } catch (Exception e) {
        e.printStackTrace();
    }

    return false;
}

    public boolean deleteVaultEntry(int entryId, int userId) {
    String sql = "DELETE FROM vault_entries WHERE id = ? AND user_id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, entryId);
        stmt.setInt(2, userId);

        int affectedRows = stmt.executeUpdate();
        return affectedRows > 0;

    } catch (Exception e) {
        e.printStackTrace();
    }

    return false;
}
    public VaultEntry getEntryById(int entryId, int userId) {
    String sql = "SELECT * FROM vault_entries WHERE id = ? AND user_id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, entryId);
        stmt.setInt(2, userId);

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            VaultEntry entry = new VaultEntry();
            entry.setId(rs.getInt("id"));
            entry.setUserId(rs.getInt("user_id"));
            entry.setSiteName(rs.getString("site_name"));
            entry.setSiteUrl(rs.getString("site_url"));
            entry.setAccountUsername(rs.getString("account_username"));
            entry.setEncryptedPassword(rs.getString("encrypted_password"));
            entry.setIv(rs.getString("iv"));
            entry.setNote(rs.getString("note"));
            entry.setCreatedAt(rs.getTimestamp("created_at"));
            entry.setUpdatedAt(rs.getTimestamp("updated_at"));
            return entry;
        }

    } catch (Exception e) {
        e.printStackTrace();
    }

    return null;
}
}