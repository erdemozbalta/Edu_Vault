package eduvault.model;

public class RegisterResult {
    private boolean success;
    private String message;
    private String recoveryPhrase;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRecoveryPhrase() {
        return recoveryPhrase;
    }

    public void setRecoveryPhrase(String recoveryPhrase) {
        this.recoveryPhrase = recoveryPhrase;
    }
}