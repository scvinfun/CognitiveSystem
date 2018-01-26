package Logging;

import Authentication.AuthenticationController;
import Authentication.CSUser;
import Database.FireBaseDB;
import com.google.gson.JsonObject;

import java.sql.Timestamp;

public class LoggingController {
    private static LoggingController instance = null;

    public static LoggingController getInstance() {
        if (instance == null) {
            instance = new LoggingController();
        }
        return instance;
    }

    public void logging(String category, String description) {
        AuthenticationController auth = AuthenticationController.getInstance();
        if (!auth.isLogin()) {
            return;
        }

        JsonObject obj = new JsonObject();
        CSUser currentUser = auth.getCurrentCSUser();
        obj.addProperty("uid", currentUser.getLocalId());
        obj.addProperty("email", currentUser.getEmail());
        obj.addProperty("category", category);
        obj.addProperty("description", description);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        obj.addProperty("createAt", timestamp.toString());
        FireBaseDB.getInstance().writeData("Log", obj);
    }
}
