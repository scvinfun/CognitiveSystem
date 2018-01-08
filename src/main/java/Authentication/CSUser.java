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
}