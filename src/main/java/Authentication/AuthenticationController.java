package Authentication;

import com.google.gson.JsonObject;

public class AuthenticationController {
    private static AuthenticationController instance = null;
    private String idToken = null;
    private CSUser currentCSUser = null;

    public static AuthenticationController getInstance() {
        if (instance == null) {
            instance = new AuthenticationController();
        }
        return instance;
    }

    public boolean registerAccountWithEmailPassword(String email, String password) {
        JsonObject response_register = null;
        JsonObject request_register = new JsonObject();
        request_register.addProperty("email", email);
        request_register.addProperty("password", Encryptor.getInstance().encrypt(password));
        request_register.addProperty("returnSecureToken", true);
        FireBaseAuth auth = FireBaseAuth.getInstance();
        response_register = auth.authenticationServiceCall(auth.OPERATION_SIGNUP, request_register);

        if (response_register != null) {
            loginWithEmailPassword(email, password);
            return true;
        } else {
            return false;
        }
    }

    public void fake_login() {
        loginWithEmailPassword("dummy@dummy.com", "dummy");
    }

    public boolean loginWithEmailPassword(String email, String password) {
        JsonObject response_login = null;
        JsonObject response_getUser = null;
        String idToken = null;

        JsonObject request_login = new JsonObject();
        request_login.addProperty("email", email);
        request_login.addProperty("password", Encryptor.getInstance().encrypt(password));
        request_login.addProperty("returnSecureToken", true);
        FireBaseAuth auth = FireBaseAuth.getInstance();
        response_login = auth.authenticationServiceCall(auth.OPERATION_SIGNIN, request_login);

        if (response_login != null) {
            idToken = response_login.get("idToken").getAsString();
            JsonObject request_getUser = new JsonObject();
            request_getUser.addProperty("idToken", idToken);
            response_getUser = auth.authenticationServiceCall(auth.OPERATION_GETUSER, request_getUser);
        }

        if (response_getUser != null) {
            JsonObject user_info = response_getUser.getAsJsonArray("users").get(0).getAsJsonObject();
            this.currentCSUser = new CSUser(user_info.get("localId").getAsString(), user_info.get("email").getAsString(), user_info.get("passwordHash").getAsString(), user_info.get("validSince")
                    .getAsString(), user_info.get("lastLoginAt").getAsString(), user_info.get("createdAt").getAsString(), user_info.get("emailVerified").getAsBoolean(), user_info.get("passwordUpdatedAt").getAsDouble());
            updateIdToken(idToken);

            return true;
        }

        return false;
    }

    public CSUser getCurrentCSUser() {
        return currentCSUser;
    }

    public String getIdToken() {
        return this.idToken;
    }

    public boolean isLogin() {
        return currentCSUser != null && idToken != null;
    }

    private void updateIdToken(String idToken) {
        if (this.idToken == null)
            this.idToken = idToken;
    }

    public void logout() {
        this.idToken = null;
        this.currentCSUser = null;
    }
}
