package eduvault.service;

import eduvault.dao.UserDAO;
import eduvault.model.User;
import eduvault.dao.LoginAttemptDAO;
import java.sql.Timestamp;
import eduvault.model.RegisterResult;
import javax.crypto.SecretKey;
import eduvault.service.Bip39Service;
import eduvault.service.VaultKeyService;
import eduvault.service.RecoveryService;


public class AuthService {

    private UserDAO userDAO = new UserDAO();
    private LoginAttemptDAO loginAttemptDAO = new LoginAttemptDAO();
    private Bip39Service bip39Service = new Bip39Service();
    private VaultKeyService vaultKeyService = new VaultKeyService();
    private RecoveryService recoveryService = new RecoveryService();
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int[] LOCKOUT_MINUTES = {5, 15, 40, 100};
    private static final int MAX_LOCK_CYCLES_BEFORE_RESET = 5;


    public RegisterResult register(String username, String email, String phoneNumber, String password) {
        RegisterResult result = new RegisterResult();

        try {
            if (username == null || username.trim().isEmpty()) {
                result.setSuccess(false);
                result.setMessage("Kullanıcı adı boş olamaz!");
                return result;
            }

            if (!isValidUsername(username)) {
                result.setSuccess(false);
                result.setMessage("Kullanıcı adı geçersiz:\n" + getUsernameValidationMessage(username));
                return result;
            }

            if (email == null || email.trim().isEmpty()) {
                result.setSuccess(false);
                result.setMessage("E-posta boş olamaz!");
                return result;
            }

            if (!isValidEmail(email)) {
                result.setSuccess(false);
                result.setMessage("E-posta geçersiz:\n" + getEmailValidationMessage(email));
                return result;
            }

            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                result.setSuccess(false);
                result.setMessage("Telefon numarası boş olamaz!");
                return result;
            }

            if (!isValidPhone(phoneNumber)) {
                result.setSuccess(false);
                result.setMessage("Telefon numarası geçersiz:\n" + getPhoneValidationMessage(phoneNumber));
                return result;
            }

            if (password == null || password.trim().isEmpty()) {
                result.setSuccess(false);
                result.setMessage("Parola boş olamaz!");
                return result;
            }

            if (!isPasswordStrong(password)) {
                result.setSuccess(false);
                result.setMessage("Parola yeterince güçlü değil:\n" + getPasswordValidationMessage(password));
                return result;
            }

            if (userDAO.existsByUsername(username)) {
                result.setSuccess(false);
                result.setMessage("Bu kullanıcı adı zaten kullanılıyor!");
                return result;
            }

            if (userDAO.existsByEmail(email)) {
                result.setSuccess(false);
                result.setMessage("Bu e-posta zaten kayıtlı!");
                return result;
            }

            
            String loginSalt = HashService.generateSalt();
            String passwordHash = HashService.hashPassword(password, loginSalt);

            
            SecretKey vaultKey = vaultKeyService.generateRandomVaultKey();

            
            String masterWrapSalt = vaultKeyService.generateSalt();
            SecretKey masterWrapKey = vaultKeyService.deriveMasterWrapKey(password, masterWrapSalt);
            String vaultKeyMasterIv = vaultKeyService.generateIV();
            String vaultKeyMasterEncrypted = vaultKeyService.encryptVaultKey(vaultKey, masterWrapKey, vaultKeyMasterIv);

           
            String recoveryPhrase = bip39Service.generateMnemonic24();

            
            SecretKey recoveryWrapKey = recoveryService.deriveRecoveryWrapKey(recoveryPhrase);
            String vaultKeyRecoveryIv = vaultKeyService.generateIV();
            String vaultKeyRecoveryEncrypted = recoveryService.encryptVaultKeyForRecovery(vaultKey, recoveryWrapKey, vaultKeyRecoveryIv);

           
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPhoneNumber(phoneNumber);
            user.setPasswordHash(passwordHash);
            user.setSalt(loginSalt);

            user.setMasterWrapSalt(masterWrapSalt);
            user.setVaultKeyMasterEncrypted(vaultKeyMasterEncrypted);
            user.setVaultKeyMasterIv(vaultKeyMasterIv);
            user.setVaultKeyRecoveryEncrypted(vaultKeyRecoveryEncrypted);
            user.setVaultKeyRecoveryIv(vaultKeyRecoveryIv);
            user.setRecoveryWordsCount(24);
            user.setRecoveryVersion("BIP39_EN_V1");

            userDAO.insertUser(user);

            result.setSuccess(true);
            result.setMessage("Kayıt başarılı!");
            result.setRecoveryPhrase(recoveryPhrase);
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            result.setSuccess(false);
            result.setMessage("Kayıt sırasında hata oluştu: " + e.getMessage());
            return result;
        }
    }

    public boolean login(String username, String password) {
        try {
            User user = userDAO.getUserByUsername(username);

            if (user == null) {
                System.out.println("Kullanıcı bulunamadı!");
                return false;
            }

            boolean isValid = HashService.verifyPassword(
                    password,
                    user.getPasswordHash(),
                    user.getSalt()
            );

            if (isValid) {
                System.out.println("Giriş başarılı!");
            } else {
                System.out.println("Şifre yanlış!");
            }

            return isValid;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean isPasswordStrong(String password) {
        return getPasswordValidationMessage(password).isEmpty();
    }
    
    public String getPasswordValidationMessage(String password) {
        StringBuilder message = new StringBuilder();

        if (password == null || password.isEmpty()) {
            return "Parola boş olamaz.";
        }

        if (password.length() < 10) {
            message.append("- Parola en az 10 karakter olmalıdır.\n");
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char ch : password.toCharArray()) {
            if (Character.isUpperCase(ch)) {
                hasUpper = true;
            } else if (Character.isLowerCase(ch)) {
                hasLower = true;
            } else if (Character.isDigit(ch)) {
                hasDigit = true;
            } else {
                hasSpecial = true;
            }
        }

        if (!hasUpper) {
            message.append("- En az 1 büyük harf eklemelisiniz.\n");
        }

        if (!hasLower) {
            message.append("- En az 1 küçük harf eklemelisiniz.\n");
        }

        if (!hasDigit) {
            message.append("- En az 1 rakam eklemelisiniz.\n");
        }

        if (!hasSpecial) {
            message.append("- En az 1 özel karakter eklemelisiniz.\n");
        }

        return message.toString();
    }
    public User loginAndGetUser(String username, String password) {
    try {
        String lockMessage = getLockoutMessage(username);
        if (!lockMessage.isEmpty()) {
            System.out.println(lockMessage);
            return null;
        }

        User user = userDAO.getUserByUsername(username);

        if (user == null) {
            loginAttemptDAO.logAttempt(username, false);
            System.out.println("Kullanıcı bulunamadı!");
            return null;
        }


        if (user.getPasswordHash() == null || user.getSalt() == null) {
            System.out.println("Kullanıcı verileri eksik: password_hash veya salt null.");
            return null;
        }

        boolean isValid = HashService.verifyPassword(
                password,
                user.getPasswordHash(),
                user.getSalt()
        );

        if (isValid) {
            loginAttemptDAO.logAttempt(username, true);
            loginAttemptDAO.clearAttempts(username);
            System.out.println("Giriş başarılı!");
            return user;
        } else {
            loginAttemptDAO.logAttempt(username, false);
            System.out.println("Şifre yanlış!");
        }

    } catch (Exception e) {
        e.printStackTrace();
    }

    return null;
    }
        private boolean isValidUsername(String username) {
            return getUsernameValidationMessage(username).isEmpty();
        }

        private boolean isValidEmail(String email) {
            return getEmailValidationMessage(email).isEmpty();
        }

        private boolean isValidPhone(String phoneNumber) {
            return getPhoneValidationMessage(phoneNumber).isEmpty();
        }
        
        public String getUsernameValidationMessage(String username) {
        StringBuilder message = new StringBuilder();

        if (username == null || username.trim().isEmpty()) {
            return "Kullanıcı adı boş olamaz.";
        }

        String trimmed = username.trim();

        if (trimmed.length() < 7) {
            message.append("- Kullanıcı adı en az 7 karakter olmalıdır.\n");
        }

        if (!trimmed.matches("^[a-zA-Z0-9_]+$")) {
            message.append("- Kullanıcı adı sadece harf, rakam ve alt çizgi (_) içerebilir.\n");
        }

        return message.toString();
        }
        public String getEmailValidationMessage(String email) {
        StringBuilder message = new StringBuilder();

        if (email == null || email.trim().isEmpty()) {
            return "E-posta boş olamaz.";
        }

        String trimmed = email.trim();

        if (!trimmed.contains("@")) {
            message.append("- E-posta '@' içermelidir.\n");
        }

        if (!trimmed.contains(".")) {
            message.append("- E-posta alan adı uzantısı içermelidir.\n");
        }

        if (!trimmed.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            message.append("- Geçerli bir e-posta formatı giriniz. Örnek: kullanici@mail.com\n");
        }

        return message.toString();
        }
        public String getPhoneValidationMessage(String phoneNumber) {
        StringBuilder message = new StringBuilder();

        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return "Telefon numarası boş olamaz.";
        }

        String trimmed = phoneNumber.trim();

        if (!trimmed.matches("^\\d+$")) {
            message.append("- Telefon numarası yalnızca rakamlardan oluşmalıdır.\n");
        }

        if (!(trimmed.length() == 10 || trimmed.length() == 11)) {
            message.append("- Telefon numarası 10 veya 11 haneli olmalıdır.\n");
        }

        return message.toString();
        }
        public String getLockoutMessage(String username) {
           try {
               int failedAttempts = loginAttemptDAO.countRecentFailedAttempts(username, 100);

               if (failedAttempts < MAX_FAILED_ATTEMPTS) {
                   return "";
               }

               int totalCycles = loginAttemptDAO.countTotalLockCycles(username);

               if (totalCycles >= MAX_LOCK_CYCLES_BEFORE_RESET) {
                   return "Hesap çok fazla kez kilitlendi. Devam etmek için kurtarma ekranından ana şifre sıfırlama işlemi yapılmalıdır.";
               }

               int cycleIndex = Math.max(0, totalCycles - 1);
               if (cycleIndex >= LOCKOUT_MINUTES.length) {
                   cycleIndex = LOCKOUT_MINUTES.length - 1;
               }

               int lockMinutes = LOCKOUT_MINUTES[cycleIndex];

               Timestamp lastFailed = loginAttemptDAO.getLastFailedAttemptTime(username);
               if (lastFailed == null) {
                   return "";
               }

               long lockUntil = lastFailed.getTime() + (lockMinutes * 60L * 1000L);
               long remainingMillis = lockUntil - System.currentTimeMillis();

               if (remainingMillis > 0) {
                   long remainingMinutes = (remainingMillis / 1000 / 60) + 1;
                   return "Hesap geçici olarak kilitlendi. Kalan süre: " + remainingMinutes + " dakika.";
               }

           } catch (Exception e) {
               e.printStackTrace();
           }

           return "";
       }
        public RegisterResult resetMasterPasswordWithRecoveryPhrase(String username, String recoveryPhrase, String newPassword) {
        RegisterResult result = new RegisterResult();

        try {
            if (username == null || username.trim().isEmpty()) {
                result.setSuccess(false);
                result.setMessage("Kullanıcı adı boş olamaz!   ");
                return result;
            }

            if (recoveryPhrase == null || recoveryPhrase.trim().isEmpty()) {
                result.setSuccess(false);
                result.setMessage("Kurtarma Kelimeleri boş olamaz!   ");
                return result;
            }

            if (newPassword == null || newPassword.trim().isEmpty()) {
                result.setSuccess(false);
                result.setMessage("Yeni Ana Şifre boş olamaz!   ");
                return result;
            }

            if (!isPasswordStrong(newPassword)) {
                result.setSuccess(false);
                result.setMessage("Yeni parola yeterince güçlü değil:   \n" + getPasswordValidationMessage(newPassword));
                return result;
            }

            User user = userDAO.getUserByUsername(username);

            if (user == null) {
                result.setSuccess(false);
                result.setMessage("Kullanıcı bulunamadı!   ");
                return result;
            }

            boolean validMnemonic = bip39Service.isValidMnemonic(recoveryPhrase);
            if (!validMnemonic) {
                result.setSuccess(false);
                result.setMessage("Kurtarma kelimeleri geçersiz. Kelimeleri doğru sırayla ve eksiksiz girdiğinizden emin olun.   ");
                return result;
            }

            SecretKey recoveryWrapKey = recoveryService.deriveRecoveryWrapKey(recoveryPhrase);

            SecretKey vaultKey = recoveryService.decryptVaultKeyFromRecovery(
                    user.getVaultKeyRecoveryEncrypted(),
                    recoveryWrapKey,
                    user.getVaultKeyRecoveryIv()
            );

            String newLoginSalt = HashService.generateSalt();
            String newPasswordHash = HashService.hashPassword(newPassword, newLoginSalt);

            String newMasterWrapSalt = vaultKeyService.generateSalt();
            SecretKey newMasterWrapKey = vaultKeyService.deriveMasterWrapKey(newPassword, newMasterWrapSalt);
            String newVaultKeyMasterIv = vaultKeyService.generateIV();
            String newVaultKeyMasterEncrypted = vaultKeyService.encryptVaultKey(vaultKey, newMasterWrapKey, newVaultKeyMasterIv);

            user.setPasswordHash(newPasswordHash);
            user.setSalt(newLoginSalt);
            user.setMasterWrapSalt(newMasterWrapSalt);
            user.setVaultKeyMasterIv(newVaultKeyMasterIv);
            user.setVaultKeyMasterEncrypted(newVaultKeyMasterEncrypted);

            boolean updated = userDAO.updateUserCredentialsAndMasterWrap(user);

            if (updated) {
                result.setSuccess(true);
                result.setMessage("Master password başarıyla sıfırlandı!   ");
            } else {
                result.setSuccess(false);
                result.setMessage("Master password sıfırlanamadı!   ");
            }

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            result.setSuccess(false);
            result.setMessage("Şifre sıfırlama sırasında hata oluştu:   " + e.getMessage());
            return result;
        }
    }
        public RegisterResult changeMasterPassword(User user, String currentPassword, String newPassword) {
    RegisterResult result = new RegisterResult();

    try {
        if (user == null) {
            result.setSuccess(false);
            result.setMessage("Kullanıcı oturumu bulunamadı.   ");
            return result;
        }

        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            result.setSuccess(false);
            result.setMessage("Mevcut ana şifre boş olamaz.   ");
            return result;
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            result.setSuccess(false);
            result.setMessage("Yeni ana şifre boş olamaz.   ");
            return result;
        }

        if (!HashService.verifyPassword(currentPassword, user.getPasswordHash(), user.getSalt())) {
            result.setSuccess(false);
            result.setMessage("Mevcut ana şifre yanlış.   ");
            return result;
        }

        if (!isPasswordStrong(newPassword)) {
            result.setSuccess(false);
            result.setMessage("Yeni parola yeterince güçlü değil:   \n" + getPasswordValidationMessage(newPassword));
            return result;
        }

        javax.crypto.SecretKey vaultKey = SessionManager.getCurrentVaultKey();
        if (vaultKey == null) {
            result.setSuccess(false);
            result.setMessage("Oturum anahtarı bulunamadı.   ");
            return result;
        }

        String newLoginSalt = HashService.generateSalt();
        String newPasswordHash = HashService.hashPassword(newPassword, newLoginSalt);

        String newMasterWrapSalt = vaultKeyService.generateSalt();
        javax.crypto.SecretKey newMasterWrapKey = vaultKeyService.deriveMasterWrapKey(newPassword, newMasterWrapSalt);
        String newVaultKeyMasterIv = vaultKeyService.generateIV();
        String newVaultKeyMasterEncrypted = vaultKeyService.encryptVaultKey(vaultKey, newMasterWrapKey, newVaultKeyMasterIv);

        user.setPasswordHash(newPasswordHash);
        user.setSalt(newLoginSalt);
        user.setMasterWrapSalt(newMasterWrapSalt);
        user.setVaultKeyMasterIv(newVaultKeyMasterIv);
        user.setVaultKeyMasterEncrypted(newVaultKeyMasterEncrypted);

        boolean updated = userDAO.updateUserCredentialsAndMasterWrap(user);

        if (updated) {
            result.setSuccess(true);
            result.setMessage("Ana şifre başarıyla değiştirildi.   ");
        } else {
            result.setSuccess(false);
            result.setMessage("Ana şifre değiştirilemedi.   ");
        }

        return result;

    } catch (Exception e) {
        e.printStackTrace();
        result.setSuccess(false);
        result.setMessage("Ana şifre değiştirme sırasında hata oluştu:   " + e.getMessage());
        return result;
    }
}

}