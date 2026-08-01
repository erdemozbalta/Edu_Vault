package eduvault.service;

import eduvault.dao.VaultEntryDAO;
import eduvault.model.User;
import eduvault.model.VaultEntry;
import java.util.List;
import javax.crypto.SecretKey;

public class VaultService {

    private VaultEntryDAO vaultEntryDAO = new VaultEntryDAO();

    public boolean addVaultEntry(User user, String siteName, String siteUrl,
                                 String accountUsername, String plainPassword, String note) {
        try {
            if (user == null) {
                System.out.println("Kullanıcı bilgisi boş!");
                return false;
            }

            if (siteName == null || siteName.trim().isEmpty()) {
                System.out.println("Site adı boş olamaz!");
                return false;
            }

            if (accountUsername == null || accountUsername.trim().isEmpty()) {
                System.out.println("Hesap kullanıcı adı boş olamaz!");
                return false;
            }

            if (plainPassword == null || plainPassword.trim().isEmpty()) {
                System.out.println("Kaydedilecek parola boş olamaz!");
                return false;
            }

            javax.crypto.SecretKey key = SessionManager.getCurrentVaultKey();
            if (key == null) {
                System.out.println("Oturum anahtarı bulunamadı!");
                return false;
            }

            String iv = CryptoService.generateIV();
            String encryptedPassword = CryptoService.encrypt(plainPassword, key, iv);

            VaultEntry entry = new VaultEntry();
            entry.setUserId(user.getId());
            entry.setSiteName(siteName);
            entry.setSiteUrl(siteUrl);
            entry.setAccountUsername(accountUsername);
            entry.setEncryptedPassword(encryptedPassword);
            entry.setIv(iv);
            entry.setNote(note);

            vaultEntryDAO.insertVaultEntry(entry);
            System.out.println("Vault kaydı başarıyla eklendi!");
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

        public java.util.List<VaultEntry> getDecryptedEntries(User user) {
        try {
            javax.crypto.SecretKey key = SessionManager.getCurrentVaultKey();
            if (key == null) {
                return java.util.List.of();
            }

            java.util.List<VaultEntry> entries = vaultEntryDAO.getEntriesByUserId(user.getId());

            for (VaultEntry entry : entries) {
                String decryptedPassword = CryptoService.decrypt(
                        entry.getEncryptedPassword(),
                        key,
                        entry.getIv()
                );
                entry.setDecryptedPassword(decryptedPassword);
            }

            return entries;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return java.util.List.of();
    }
    public boolean updateVaultEntry(User user, int entryId,
                                    String siteName, String siteUrl, String accountUsername,
                                    String plainPassword, String note) {
        try {
            VaultEntry existingEntry = vaultEntryDAO.getEntryById(entryId, user.getId());

            if (existingEntry == null) {
                System.out.println("Güncellenecek kayıt bulunamadı!");
                return false;
            }

            javax.crypto.SecretKey key = SessionManager.getCurrentVaultKey();
            if (key == null) {
                System.out.println("Oturum anahtarı bulunamadı!");
                return false;
            }

            String newIv = CryptoService.generateIV();
            String encryptedPassword = CryptoService.encrypt(plainPassword, key, newIv);

            existingEntry.setSiteName(siteName);
            existingEntry.setSiteUrl(siteUrl);
            existingEntry.setAccountUsername(accountUsername);
            existingEntry.setEncryptedPassword(encryptedPassword);
            existingEntry.setIv(newIv);
            existingEntry.setNote(note);

            boolean updated = vaultEntryDAO.updateVaultEntry(existingEntry);

            if (updated) {
                System.out.println("Vault kaydı güncellendi!");
            } else {
                System.out.println("Vault kaydı güncellenemedi!");
            }

            return updated;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean deleteVaultEntry(User user, int entryId) {
    boolean deleted = vaultEntryDAO.deleteVaultEntry(entryId, user.getId());

    if (deleted) {
        System.out.println("Vault kaydı silindi!");
    } else {
        System.out.println("Vault kaydı silinemedi!");
    }

    return deleted;
}
}