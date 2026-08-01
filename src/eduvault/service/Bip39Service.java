package eduvault.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Bip39Service {

    private static final int WORD_COUNT_24 = 24;
    private static final int ENTROPY_BITS_24 = 256;
    private static final int CHECKSUM_BITS_24 = 8;

    public List<String> loadEnglishWordList() throws Exception {
        List<String> words = new ArrayList<>();

        InputStream is = getClass().getResourceAsStream("/eduvault/resources/bip39_english.txt");
        if (is == null) {
            throw new RuntimeException("bip39_english.txt bulunamadı.");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    words.add(trimmed);
                }
            }
        }

        if (words.size() != 2048) {
            throw new RuntimeException("Kelime listesi 2048 kelime içermelidir. Şu an: " + words.size());
        }

        return words;
    }
    
    public String normalizeMnemonicInput(String mnemonic) {
        if (mnemonic == null) {
            return "";
        }

        String normalized = mnemonic
                .trim()
                .toLowerCase()
                .replace("\r", " ")
                .replace("\n", " ");

        normalized = normalized.replaceAll("\\s+", " ").trim();

        return normalized;
    }

    public String generateMnemonic24() throws Exception {
        List<String> words = loadEnglishWordList();

        byte[] entropy = new byte[32]; // 256 bit
        new SecureRandom().nextBytes(entropy);

        byte[] hash = MessageDigest.getInstance("SHA-256").digest(entropy);

        StringBuilder bits = new StringBuilder();

        for (byte b : entropy) {
            bits.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }

        String checksumBits = String.format("%8s", Integer.toBinaryString(hash[0] & 0xFF)).replace(' ', '0')
                .substring(0, CHECKSUM_BITS_24);

        bits.append(checksumBits);

        List<String> mnemonicWords = new ArrayList<>();

        for (int i = 0; i < WORD_COUNT_24; i++) {
            String chunk = bits.substring(i * 11, (i + 1) * 11);
            int index = Integer.parseInt(chunk, 2);
            mnemonicWords.add(words.get(index));
        }

        return String.join(" ", mnemonicWords);
    }

    public boolean isValidMnemonic(String mnemonic) throws Exception {
        List<String> words = loadEnglishWordList();

        String cleanedMnemonic = normalizeMnemonicInput(mnemonic);
        String normalized = Normalizer.normalize(cleanedMnemonic, Normalizer.Form.NFKD);
        String[] parts = normalized.split("\\s+");

        if (parts.length != 24) {
            return false;
        }

        StringBuilder bits = new StringBuilder();

        for (String word : parts) {
            int index = words.indexOf(word);
            if (index < 0) {
                return false;
            }

            bits.append(String.format("%11s", Integer.toBinaryString(index)).replace(' ', '0'));
        }

        String entropyBits = bits.substring(0, ENTROPY_BITS_24);
        String checksumBits = bits.substring(ENTROPY_BITS_24);

        byte[] entropy = new byte[32];
        for (int i = 0; i < entropy.length; i++) {
            String byteString = entropyBits.substring(i * 8, (i + 1) * 8);
            entropy[i] = (byte) Integer.parseInt(byteString, 2);
        }

        byte[] hash = MessageDigest.getInstance("SHA-256").digest(entropy);
        String expectedChecksum = String.format("%8s", Integer.toBinaryString(hash[0] & 0xFF)).replace(' ', '0')
                .substring(0, CHECKSUM_BITS_24);

        return checksumBits.equals(expectedChecksum);
    }

    public byte[] mnemonicToSeed(String mnemonic, String passphrase) throws Exception {
        String cleanedMnemonic = normalizeMnemonicInput(mnemonic);
        String normalizedMnemonic = Normalizer.normalize(cleanedMnemonic, Normalizer.Form.NFKD);
        String normalizedSalt = Normalizer.normalize("mnemonic" + passphrase, Normalizer.Form.NFKD);

        PBEKeySpec spec = new PBEKeySpec(
                normalizedMnemonic.toCharArray(),
                normalizedSalt.getBytes(StandardCharsets.UTF_8),
                2048,
                512
        );

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        return factory.generateSecret(spec).getEncoded();
    }
}
