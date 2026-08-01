package eduvault.service;

import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class VaultKeyService {

    private static final int AES_KEY_SIZE = 256;
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;
    private static final int ITERATIONS = 65536;

    public SecretKey generateRandomVaultKey() {
        byte[] keyBytes = new byte[32];
        new SecureRandom().nextBytes(keyBytes);
        return new SecretKeySpec(keyBytes, "AES");
    }

    public String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public SecretKey deriveMasterWrapKey(String masterPassword, String base64Salt) throws Exception {
        byte[] saltBytes = Base64.getDecoder().decode(base64Salt);

        PBEKeySpec spec = new PBEKeySpec(masterPassword.toCharArray(), saltBytes, ITERATIONS, AES_KEY_SIZE);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();

        return new SecretKeySpec(keyBytes, "AES");
    }

    public String generateIV() {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return Base64.getEncoder().encodeToString(iv);
    }

    public String encryptVaultKey(SecretKey vaultKey, SecretKey wrapKey, String base64IV) throws Exception {
        byte[] ivBytes = Base64.getDecoder().decode(base64IV);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, ivBytes);
        cipher.init(Cipher.ENCRYPT_MODE, wrapKey, spec);

        byte[] encrypted = cipher.doFinal(vaultKey.getEncoded());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public SecretKey decryptVaultKey(String encryptedVaultKey, SecretKey wrapKey, String base64IV) throws Exception {
        byte[] ivBytes = Base64.getDecoder().decode(base64IV);
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedVaultKey);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, ivBytes);
        cipher.init(Cipher.DECRYPT_MODE, wrapKey, spec);

        byte[] decrypted = cipher.doFinal(encryptedBytes);
        return new SecretKeySpec(decrypted, "AES");
    }
}