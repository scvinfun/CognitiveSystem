package Authentication;

import Database.FireBaseDB;
import com.google.gson.JsonObject;

public class UserSyncController {

    private static UserSyncController instance = null;

    public static UserSyncController getInstance() {
        if (instance == null) {
            instance = new UserSyncController();
        }
        return instance;
    }

    public void initSyncData() {

    }

    // TODO: sync user data
    public void syncData() {
        if (!AuthenticationController.getInstance().isLogin())
            return;

        JsonObject obj = new JsonObject();
        obj.addProperty("uid", AuthenticationController.getInstance().getCurrentCSUser().getLocalId());
        obj.addProperty("facebook", "2011");
        obj.addProperty("twitter", "2009");
        FireBaseDB.getInstance().writeData("UserSync", obj);
    }
}
