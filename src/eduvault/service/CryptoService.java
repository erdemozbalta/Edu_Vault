package eduvault.service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoService {

    private static final int AES_KEY_SIZE = 256;
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;
    private static final int ITERATIONS = 65536;

    public static SecretKey deriveKey(String masterPassword, String salt) throws Exception {
        byte[] saltBytes = Base64.getDecoder().decode(salt);

        PBEKeySpec spec = new PBEKeySpec(masterPassword.toCharArray(), saltBytes, ITERATIONS, AES_KEY_SIZE);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();

        return new SecretKeySpec(keyBytes, "AES");
    }

    public static String generateIV() {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return Base64.getEncoder().encodeToString(iv);
    }

    public static String encrypt(String plainText, SecretKey key, String base64IV) throws Exception {
        byte[] ivBytes = Base64.getDecoder().decode(base64IV);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH, ivBytes);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decrypt(String encryptedText, SecretKey key, String base64IV) throws Exception {
        byte[] ivBytes = Base64.getDecoder().decode(base64IV);
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH, ivBytes);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}