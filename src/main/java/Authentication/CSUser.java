package Authentication;

public class CSUser {
    private String localId, email, passwordHash, validSince, lastLoginAt, createdAt;
    private boolean emailVerified;
    private double passwordUpdatedAt;

    public CSUser(String localId, String email, String passwordHash, String validSince, String lastLoginAt, String createdAt, boolean emailVerified, double passwordUpdatedAt) {
        this.localId = localId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.validSince = validSince;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
        this.emailVerified = emailVerified;
        this.passwordUpdatedAt = passwordUpdatedAt;
    }

    public String getLocalId() {
        return localId;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getValidSince() {
        return validSince;
    }

    public String getLastLoginAt() {
        return lastLoginAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public double getPasswordUpdatedAt() {
        return passwordUpdatedAt;
    }
}