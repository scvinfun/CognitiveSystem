package Authentication;

import Database.FireBaseDB;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class UserSyncController {

    private static UserSyncController instance = null;
    private String syncPath = null;

    public static UserSyncController getInstance() {
        if (instance == null) {
            instance = new UserSyncController();
        }
        return instance;
    }

    private UserSyncController() {
        syncPath = "UserSync";
    }

    public void syncData_twitter(ArrayList<JsonObject> tweets) {
        if (!AuthenticationController.getInstance().isLogin())
            return;

        JsonObject userSyncData = FireBaseDB.getInstance().getData(syncPath);
        if (userSyncData == null || !isCurrentSyncDataExisted(userSyncData)) {
            initSyncData_twitter(tweets);
        } else {
            JsonObject independentUserSyncData = extractIndependentUserSyncData(userSyncData);
            modifySyncData_twitter(tweets, independentUserSyncData);
        }
    }

    private void initSyncData_twitter(ArrayList<JsonObject> tweets) {
        AuthenticationController ac = AuthenticationController.getInstance();
        if (!ac.isLogin())
            return;

        JsonObject obj = new JsonObject();
        obj.addProperty("uid", ac.getCurrentCSUser().getLocalId());
        obj.addProperty("twitter", getLatestDateTime(tweets));
        FireBaseDB.getInstance().writeData(syncPath, obj);
    }

    private void modifySyncData_twitter(ArrayList<JsonObject> tweets, JsonObject independentUserSyncData) {
        if (!AuthenticationController.getInstance().isLogin())
            return;

        JsonObject obj = new JsonObject();
        obj.addProperty("twitter", getLatestDateTime(tweets, independentUserSyncData));
        FireBaseDB.getInstance().modifyData(syncPath + "/" + independentUserSyncData.get("path").getAsString(), obj);
    }

    private void modifySyncData_facebook(String facebookSyncDate) {
        if (!AuthenticationController.getInstance().isLogin())
            return;

        JsonObject obj = new JsonObject();
        obj.addProperty("facebook", facebookSyncDate);
        FireBaseDB.getInstance().modifyData(syncPath, obj);
    }

    // TODO:try may not a good choice,somehow userSyncData still have data(!=null)
    private boolean isCurrentSyncDataExisted(JsonObject userSyncData) {
        boolean isExisted = false;

        Set<Map.Entry<String, JsonElement>> entrySet = userSyncData.entrySet();
        try {
            for (Map.Entry<String, JsonElement> entry : entrySet) {
                JsonObject obj = entry.getValue().getAsJsonObject();
                if (obj.get("uid").getAsString().equals(AuthenticationController.getInstance().getCurrentCSUser().getLocalId())) {
                    isExisted = true;
                    break;
                }
            }
        } catch (Exception e) {
            return false;
        }

        return isExisted;
    }

    private String getLatestDateTime(ArrayList<JsonObject> tweets) {
        return getLatestDateTime(tweets, null);
    }

    private String getLatestDateTime(ArrayList<JsonObject> tweets, JsonObject independentUserSyncData) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
        Date latestDateTime = null;

        try {
            if (independentUserSyncData != null)
                latestDateTime = sdf.parse(independentUserSyncData.get("twitter").getAsString());

            for (JsonObject t : tweets) {
                Date dateTime = sdf.parse(t.get("createAt").getAsString());
                if (latestDateTime == null || dateTime.after(latestDateTime))
                    latestDateTime = dateTime;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return latestDateTime.toString();
    }

    private JsonObject extractIndependentUserSyncData(JsonObject userSyncData) {
        Set<Map.Entry<String, JsonElement>> entrySet = userSyncData.entrySet();
        for (Map.Entry<String, JsonElement> entry : entrySet) {
            JsonObject obj = entry.getValue().getAsJsonObject();
            if (obj.get("uid").getAsString().equals(AuthenticationController.getInstance().getCurrentCSUser().getLocalId())) {
                obj.addProperty("path", entry.getKey());
                return obj;
            }
        }

        return null;
    }
}
