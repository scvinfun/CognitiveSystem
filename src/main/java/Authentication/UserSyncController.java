package Authentication;

import Database.FireBaseDB;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class UserSyncController {
    public enum POST_TYPE {
        TWITTER,
        FACEBOOK
    }

    private static UserSyncController instance = null;
    private String syncPath = null;

    public static UserSyncController getInstance() {
        if (instance == null) {
            instance = new UserSyncController();
        }
        return instance;
    }

    private UserSyncController() {
        syncPath = "UserSyncData";
    }

    public boolean syncData_twitter(long twitterId, ArrayList<JsonObject> tweets) {
        JsonObject userSyncData = FireBaseDB.getInstance().getData(syncPath);
        if (userSyncData == null || !isCurrentSyncDataExisted(userSyncData)) {
            initSyncData_twitter(twitterId, tweets);
            return true;
        } else {
            JsonObject independentUserSyncData = extractIndependentUserSyncData(userSyncData);
            if (isSameTwitterAccount(twitterId, independentUserSyncData)) {
                modifySyncData_twitter(twitterId, tweets, independentUserSyncData);
                return true;
            } else {
                return false;
            }
        }
    }

    private void initSyncData_twitter(long twitterId, ArrayList<JsonObject> tweets) {
        AuthenticationController ac = AuthenticationController.getInstance();
        if (!ac.isLogin())
            return;

        JsonObject obj = new JsonObject();
        obj.addProperty("uid", ac.getCurrentCSUser().getLocalId());
        obj.addProperty("twitterId", twitterId);
        obj.addProperty("twitterSyncTime", getLatestDateTime(POST_TYPE.TWITTER, tweets));
        FireBaseDB.getInstance().writeData(syncPath, obj);
    }

    private void modifySyncData_twitter(long twitterId, ArrayList<JsonObject> tweets, JsonObject independentUserSyncData) {
        if (!AuthenticationController.getInstance().isLogin())
            return;

        JsonObject obj = new JsonObject();
        obj.addProperty("twitterId", twitterId);
        obj.addProperty("twitterSyncTime", getLatestDateTime(POST_TYPE.TWITTER, tweets, independentUserSyncData));
        FireBaseDB.getInstance().modifyData(syncPath + "/" + independentUserSyncData.get("key").getAsString(), obj);
    }

    private boolean isSameTwitterAccount(long twitterId, JsonObject independentUserSyncData) {
        if (independentUserSyncData.has("twitterId")) {
            long userSyncData_twitterId = independentUserSyncData.get("twitterId").getAsLong();
            return twitterId == userSyncData_twitterId;
        } else {
            return true;
        }
    }

    public boolean syncData_facebook(long facebookId, ArrayList<JsonObject> posts) {
        JsonObject userSyncData = FireBaseDB.getInstance().getData(syncPath);
        if (userSyncData == null || !isCurrentSyncDataExisted(userSyncData)) {
            initSyncData_facebook(facebookId, posts);
            return true;
        } else {
            JsonObject independentUserSyncData = extractIndependentUserSyncData(userSyncData);
            if (isSameFacebookAccount(facebookId, independentUserSyncData)) {
                modifySyncData_facebook(facebookId, posts, independentUserSyncData);
                return true;
            } else {
                return false;
            }
        }
    }

    private void initSyncData_facebook(long facebookId, ArrayList<JsonObject> posts) {
        AuthenticationController ac = AuthenticationController.getInstance();
        if (!ac.isLogin())
            return;

        JsonObject obj = new JsonObject();
        obj.addProperty("uid", ac.getCurrentCSUser().getLocalId());
        obj.addProperty("facebookId", facebookId);
        obj.addProperty("facebookSyncTime", getLatestDateTime(POST_TYPE.FACEBOOK, posts));
        FireBaseDB.getInstance().writeData(syncPath, obj);
    }

    private void modifySyncData_facebook(long facebookId, ArrayList<JsonObject> posts, JsonObject independentUserSyncData) {
        if (!AuthenticationController.getInstance().isLogin())
            return;

        JsonObject obj = new JsonObject();
        obj.addProperty("facebookId", facebookId);
        obj.addProperty("facebookSyncTime", getLatestDateTime(POST_TYPE.FACEBOOK, posts, independentUserSyncData));
        FireBaseDB.getInstance().modifyData(syncPath + "/" + independentUserSyncData.get("key").getAsString(), obj);
    }

    private boolean isSameFacebookAccount(long facebookId, JsonObject independentUserSyncData) {
        if (independentUserSyncData.has("facebookId")) {
            long userSyncData_facebookId = independentUserSyncData.get("facebookId").getAsLong();
            return facebookId == userSyncData_facebookId;
        } else {
            return true;
        }
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

    private String getLatestDateTime(POST_TYPE postType, ArrayList<JsonObject> tweets) {
        return getLatestDateTime(postType, tweets, null);
    }

    private String getLatestDateTime(POST_TYPE postType, ArrayList<JsonObject> posts, JsonObject independentUserSyncData) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
        Date latestDateTime = null;

        try {
            if (independentUserSyncData != null)
                if (postType == POST_TYPE.TWITTER && independentUserSyncData.has("twitter"))
                    latestDateTime = sdf.parse(independentUserSyncData.get("twitterSyncTime").getAsString());
                else if (postType == POST_TYPE.FACEBOOK && independentUserSyncData.has("facebook"))
                    latestDateTime = sdf.parse(independentUserSyncData.get("facebookSyncTime").getAsString());

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
                obj.addProperty("key", entry.getKey());
                if (obj.has("twitterId"))
                    obj.addProperty("twitterId", obj.get("twitterId").getAsLong());
                if (obj.has("facebookId"))
                    obj.addProperty("facebookId", obj.get("facebookId").getAsLong());
                return obj;
            }
        }

        return null;
    }

    public JsonObject getUserSyncData() {
        JsonObject result = new JsonObject();

        JsonObject userSyncData = FireBaseDB.getInstance().getData(syncPath);
        if (userSyncData == null || !isCurrentSyncDataExisted(userSyncData)) {
            result.addProperty("syncDataIsExisted", false);
        } else {
            result = extractIndependentUserSyncData(userSyncData);
            result.addProperty("syncDataIsExisted", true);
        }

        return result;
    }
}
