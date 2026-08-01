package eduvault.model;

import java.sql.Timestamp;

public class User {
    private int id;
    private String username;
    private String email;
    private String phoneNumber;
    private String passwordHash;
    private String salt;
    private Timestamp createdAt;
    private String masterWrapSalt;
    private String vaultKeyMasterEncrypted;
    private String vaultKeyMasterIv;
    private String vaultKeyRecoveryEncrypted;
    private String vaultKeyRecoveryIv;
    private int recoveryWordsCount;
    private String recoveryVersion;

    public User() 
    {
    }

    public User(int id, String username, String email, String phoneNumber, String passwordHash, String salt, Timestamp createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    public String getMasterWrapSalt() {
    return masterWrapSalt;
    }

    public void setMasterWrapSalt(String masterWrapSalt) {
        this.masterWrapSalt = masterWrapSalt;
    }

    public String getVaultKeyMasterEncrypted() {
        return vaultKeyMasterEncrypted;
    }

    public void setVaultKeyMasterEncrypted(String vaultKeyMasterEncrypted) {
        this.vaultKeyMasterEncrypted = vaultKeyMasterEncrypted;
    }

    public String getVaultKeyMasterIv() {
        return vaultKeyMasterIv;
    }

    public void setVaultKeyMasterIv(String vaultKeyMasterIv) {
        this.vaultKeyMasterIv = vaultKeyMasterIv;
    }

    public String getVaultKeyRecoveryEncrypted() {
        return vaultKeyRecoveryEncrypted;
    }

    public void setVaultKeyRecoveryEncrypted(String vaultKeyRecoveryEncrypted) {
        this.vaultKeyRecoveryEncrypted = vaultKeyRecoveryEncrypted;
    }

    public String getVaultKeyRecoveryIv() {
        return vaultKeyRecoveryIv;
    }

    public void setVaultKeyRecoveryIv(String vaultKeyRecoveryIv) {
        this.vaultKeyRecoveryIv = vaultKeyRecoveryIv;
    }

    public int getRecoveryWordsCount() {
        return recoveryWordsCount;
    }

    public void setRecoveryWordsCount(int recoveryWordsCount) {
        this.recoveryWordsCount = recoveryWordsCount;
    }

    public String getRecoveryVersion() {
        return recoveryVersion;
    }

    public void setRecoveryVersion(String recoveryVersion) {
        this.recoveryVersion = recoveryVersion;
    }
}