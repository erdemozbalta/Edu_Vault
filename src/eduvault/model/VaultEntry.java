package eduvault.model;

import java.sql.Timestamp;

public class VaultEntry {
    private int id;
    private int userId;
    private String siteName;
    private String siteUrl;
    private String accountUsername;
    private String encryptedPassword;
    private String iv;
    private String note;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String decryptedPassword;

    public VaultEntry() {
    }

    public VaultEntry(int id, int userId, String siteName, String siteUrl, String accountUsername,
                      String encryptedPassword, String iv, String note,
                      Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.userId = userId;
        this.siteName = siteName;
        this.siteUrl = siteUrl;
        this.accountUsername = accountUsername;
        this.encryptedPassword = encryptedPassword;
        this.iv = iv;
        this.note = note;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }    

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getSiteUrl() {
        return siteUrl;
    }

    public void setSiteUrl(String siteUrl) {
        this.siteUrl = siteUrl;
    }

    public String getAccountUsername() {
        return accountUsername;
    }

    public void setAccountUsername(String accountUsername) {
        this.accountUsername = accountUsername;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
    public String getDecryptedPassword() {
        return decryptedPassword;
    }

    public void setDecryptedPassword(String decryptedPassword) {
        this.decryptedPassword = decryptedPassword;
    }
}