package eduvault.service;

import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class RecoveryService {

    private final Bip39Service bip39Service = new Bip39Service();
    private final VaultKeyService vaultKeyService = new VaultKeyService();

    public SecretKey deriveRecoveryWrapKey(String mnemonic) throws Exception {
        byte[] bip39Seed = bip39Service.mnemonicToSeed(mnemonic, "");

        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec hmacKey = new SecretKeySpec("EduVault-Recovery-Wrap".getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(hmacKey);

        byte[] output = mac.doFinal(bip39Seed);

        return new SecretKeySpec(output, "AES");
    }

    public String encryptVaultKeyForRecovery(SecretKey vaultKey, SecretKey recoveryWrapKey, String base64IV) throws Exception {
        return vaultKeyService.encryptVaultKey(vaultKey, recoveryWrapKey, base64IV);
    }

    public SecretKey decryptVaultKeyFromRecovery(String encryptedVaultKey, SecretKey recoveryWrapKey, String base64IV) throws Exception {
        return vaultKeyService.decryptVaultKey(encryptedVaultKey, recoveryWrapKey, base64IV);
    }
}