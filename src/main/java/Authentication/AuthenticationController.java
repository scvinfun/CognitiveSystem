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

    public void loginWithEmailPassword(String email, String password) throws Exception {
        JsonObject response_login = null;
        JsonObject response_getUser = null;

        JsonObject request_login = new JsonObject();
        request_login.addProperty("email", email);
        request_login.addProperty("password", password);
        request_login.addProperty("returnSecureToken", true);
        FireBaseAuth auth = FireBaseAuth.getInstance();
        response_login = auth.authenticationCall(auth.OPERATION_SIGNIN, request_login);

        String idToken = response_login.get("idToken").getAsString();
        if (response_login != null) {
            JsonObject request_getUser = new JsonObject();
            request_getUser.addProperty("idToken", idToken);
            response_getUser = auth.authenticationCall(auth.OPERATION_GETUSER, request_getUser);
        }

        if (response_getUser != null) {
            JsonObject user_info = response_getUser.getAsJsonArray("users").get(0).getAsJsonObject();
            this.currentCSUser = new CSUser(user_info.get("localId").getAsString(), user_info.get("email").getAsString(), user_info.get("passwordHash").getAsString(), user_info.get("validSince")
                    .getAsString(), user_info.get("lastLoginAt").getAsString(), user_info.get("createdAt").getAsString(), user_info.get("emailVerified").getAsBoolean(), user_info.get("passwordUpdatedAt").getAsDouble());
            this.idToken = idToken;
        }
    }

    public CSUser getCurrentCSUser() {
        return currentCSUser;
    }

    public boolean isLogin() {
        return currentCSUser != null && idToken != null;
    }

    private void updateIdToken(String idToken) {
        this.idToken = idToken;
    }

    public void logout() {
        this.idToken = null;
        this.currentCSUser = null;
    }
}
