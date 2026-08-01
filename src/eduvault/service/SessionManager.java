package eduvault.service;

import eduvault.model.User;
import java.util.Arrays;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class SessionManager {

    private static User currentUser;
    private static byte[] currentVaultKeyBytes;
    private static long lastActivityTime;
    private static boolean manuallyLoggedOut = false;

    private static final long SESSION_TIMEOUT_MILLIS = 10 * 60 * 1000;

    public static void login(User user, SecretKey vaultKey) {
        currentUser = user;
        currentVaultKeyBytes = vaultKey.getEncoded();
        updateLastActivity();
        manuallyLoggedOut = false;
    }
    
    public static void logout() {
            currentUser = null;

            if (currentVaultKeyBytes != null) {
                Arrays.fill(currentVaultKeyBytes, (byte) 0);
                currentVaultKeyBytes = null;
            }

            lastActivityTime = 0;
            manuallyLoggedOut = true;
        }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static SecretKey getCurrentVaultKey() {
        if (currentVaultKeyBytes == null) {
            return null;
        }
        return new SecretKeySpec(currentVaultKeyBytes, "AES");
    }

    public static boolean isLoggedIn() {
        return currentUser != null && currentVaultKeyBytes != null;
    }

    public static void updateLastActivity() {
        lastActivityTime = System.currentTimeMillis();
    }

    public static boolean isSessionExpired() {
        if (!isLoggedIn()) {
            return true;
        }
        return System.currentTimeMillis() - lastActivityTime > SESSION_TIMEOUT_MILLIS;
    }

    public static long getRemainingSessionMillis() {
        if (!isLoggedIn()) {
            return 0;
        }

        long elapsed = System.currentTimeMillis() - lastActivityTime;
        long remaining = SESSION_TIMEOUT_MILLIS - elapsed;
        return Math.max(0, remaining);
    }
    
    public static boolean isManuallyLoggedOut() {
    return manuallyLoggedOut;
    }
}