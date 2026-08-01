package eduvault.service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PasswordGeneratorService {

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()-_=+[]{};:,.<>?";

    private static final SecureRandom random = new SecureRandom();

    public String generatePassword(int length, boolean useUppercase, boolean useLowercase,
                                   boolean useDigits, boolean useSpecial) {

        if (length < 8) {
            throw new IllegalArgumentException("Parola uzunluğu en az 8 olmalı.");
        }

        StringBuilder allowedChars = new StringBuilder();
        List<Character> passwordChars = new ArrayList<>();

        if (useUppercase) {
            allowedChars.append(UPPERCASE);
            passwordChars.add(getRandomChar(UPPERCASE));
        }

        if (useLowercase) {
            allowedChars.append(LOWERCASE);
            passwordChars.add(getRandomChar(LOWERCASE));
        }

        if (useDigits) {
            allowedChars.append(DIGITS);
            passwordChars.add(getRandomChar(DIGITS));
        }

        if (useSpecial) {
            allowedChars.append(SPECIAL);
            passwordChars.add(getRandomChar(SPECIAL));
        }

        if (allowedChars.length() == 0) {
            throw new IllegalArgumentException("En az bir karakter grubu seçilmelidir.");
        }

        while (passwordChars.size() < length) {
            passwordChars.add(getRandomChar(allowedChars.toString()));
        }

        Collections.shuffle(passwordChars);

        StringBuilder password = new StringBuilder();
        for (char c : passwordChars) {
            password.append(c);
        }

        return password.toString();
    }

    private char getRandomChar(String chars) {
        return chars.charAt(random.nextInt(chars.length()));
    }
}