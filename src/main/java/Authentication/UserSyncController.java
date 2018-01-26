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
    //TODO:TYPE + Test sync twitter then sync facebook
    private final String POSTTYPE_TWITTER = "TWITTER";
    private final String POSTTYPE_FACEBOOK = "FACEBOOK";

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
        obj.addProperty("twitter", getLatestDateTime(POSTTYPE_TWITTER, tweets));
        FireBaseDB.getInstance().writeData(syncPath, obj);
    }

    private void modifySyncData_twitter(ArrayList<JsonObject> tweets, JsonObject independentUserSyncData) {
        if (!AuthenticationController.getInstance().isLogin())
            return;

        JsonObject obj = new JsonObject();
        obj.addProperty("twitter", getLatestDateTime(POSTTYPE_TWITTER, tweets, independentUserSyncData));
        FireBaseDB.getInstance().modifyData(syncPath + "/" + independentUserSyncData.get("path").getAsString(), obj);
    }

    public void syncData_facebook(ArrayList<JsonObject> posts) {
        if (!AuthenticationController.getInstance().isLogin())
            return;

        JsonObject userSyncData = FireBaseDB.getInstance().getData(syncPath);
        if (userSyncData == null || !isCurrentSyncDataExisted(userSyncData)) {
            initSyncData_facebook(posts);
        } else {
            JsonObject independentUserSyncData = extractIndependentUserSyncData(userSyncData);
            modifySyncData_facebook(posts, independentUserSyncData);
        }
    }

    private void initSyncData_facebook(ArrayList<JsonObject> posts) {
        AuthenticationController ac = AuthenticationController.getInstance();
        if (!ac.isLogin())
            return;

        JsonObject obj = new JsonObject();
        obj.addProperty("uid", ac.getCurrentCSUser().getLocalId());
        obj.addProperty("facebook", getLatestDateTime(POSTTYPE_FACEBOOK, posts));
        FireBaseDB.getInstance().writeData(syncPath, obj);
    }

    private void modifySyncData_facebook(ArrayList<JsonObject> posts, JsonObject independentUserSyncData) {
        if (!AuthenticationController.getInstance().isLogin())
            return;

        JsonObject obj = new JsonObject();
        obj.addProperty("facebook", getLatestDateTime(POSTTYPE_FACEBOOK, posts, independentUserSyncData));
        FireBaseDB.getInstance().modifyData(syncPath + "/" + independentUserSyncData.get("path").getAsString(), obj);
    }

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

    private String getLatestDateTime(String postType, ArrayList<JsonObject> tweets) {
        return getLatestDateTime(postType, tweets, null);
    }

    private String getLatestDateTime(String postType, ArrayList<JsonObject> posts, JsonObject independentUserSyncData) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
        Date latestDateTime = null;

        try {
            if (independentUserSyncData != null)
                if (postType.equals(POSTTYPE_TWITTER) && independentUserSyncData.has("twitter"))
                    latestDateTime = sdf.parse(independentUserSyncData.get("twitter").getAsString());
                else if (postType.equals(POSTTYPE_FACEBOOK) && independentUserSyncData.has("facebook"))
                    latestDateTime = sdf.parse(independentUserSyncData.get("facebook").getAsString());

            for (JsonObject t : posts) {
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
