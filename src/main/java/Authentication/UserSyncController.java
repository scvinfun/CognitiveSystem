package Authentication;

import CognitiveServices.DiagnosisController;
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
    private SimpleDateFormat sdf = null;
    private SimpleDateFormat sdf_display = null;

    public static UserSyncController getInstance() {
        if (instance == null) {
            instance = new UserSyncController();
        }
        return instance;
    }

    private UserSyncController() {
        syncPath = "UserSyncData";
        sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
        sdf_display = new SimpleDateFormat("dd/MMM/yyyy - HH:mm", Locale.US);
    }

    private int diagnoseAllData(POST_TYPE postType, ArrayList<JsonObject> posts) throws Exception {
        return diagnoseData(postType, posts, null);
    }

    private int diagnoseData(POST_TYPE postType, ArrayList<JsonObject> posts, String syncTime_str) throws Exception {
        if (syncTime_str != null) {
            Date syncTime = sdf.parse(syncTime_str);
            ArrayList<JsonObject> posts_copy = new ArrayList<>(posts);
            for (JsonObject obj : posts) {
                Date createAt = sdf.parse(obj.get("createAt").getAsString());
                if (createAt.before(syncTime) || createAt.equals(syncTime))
                    posts_copy.remove(obj);
            }

            posts = posts_copy;
        }

        // handling diagnostic function
        return DiagnosisController.getInstance().diagnose(postType, posts);
    }

    public JsonObject syncData_twitter(long twitterId, ArrayList<JsonObject> tweets) throws Exception {
        JsonObject result = new JsonObject();

        JsonObject userSyncData = FireBaseDB.getInstance().getData(syncPath);
        if (userSyncData == null || !isCurrentSyncDataExisted(userSyncData)) {
            initSyncData_twitter(twitterId, tweets);
            result.addProperty("detectedNum", diagnoseAllData(POST_TYPE.TWITTER, tweets));
            result.addProperty("successSync", true);
        } else {
            JsonObject independentUserSyncData = extractIndependentUserSyncData(userSyncData);
            boolean isSameTwitterAccount = isSameTwitterAccount(twitterId, independentUserSyncData);
            boolean isSameSyncTime = isSameSyncTime(POST_TYPE.TWITTER, tweets, independentUserSyncData);
            if (isSameTwitterAccount && !isSameSyncTime) {
                modifySyncData_twitter(twitterId, tweets, independentUserSyncData);
                if (independentUserSyncData.has("twitterSyncTime"))
                    result.addProperty("detectedNum", diagnoseData(POST_TYPE.TWITTER, tweets, independentUserSyncData.get("twitterSyncTime").getAsString()));
                else
                    result.addProperty("detectedNum", diagnoseAllData(POST_TYPE.TWITTER, tweets));
                result.addProperty("successSync", true);
            } else if (isSameTwitterAccount && isSameSyncTime) {
                result.addProperty("successSync", true);
            } else {
                result.addProperty("successSync", false);
            }
        }

        return result;
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

    public JsonObject syncData_facebook(long facebookId, ArrayList<JsonObject> posts) throws Exception {
        JsonObject result = new JsonObject();

        JsonObject userSyncData = FireBaseDB.getInstance().getData(syncPath);
        if (userSyncData == null || !isCurrentSyncDataExisted(userSyncData)) {
            initSyncData_facebook(facebookId, posts);
            result.addProperty("detectedNum", diagnoseAllData(POST_TYPE.FACEBOOK, posts));
            result.addProperty("successSync", true);
        } else {
            JsonObject independentUserSyncData = extractIndependentUserSyncData(userSyncData);
            boolean isSameFacebookAccount = isSameFacebookAccount(facebookId, independentUserSyncData);
            boolean isSameSyncTime = isSameSyncTime(POST_TYPE.FACEBOOK, posts, independentUserSyncData);
            if (isSameFacebookAccount && !isSameSyncTime) {
                modifySyncData_facebook(facebookId, posts, independentUserSyncData);
                if (independentUserSyncData.has("facebookSyncTime"))
                    result.addProperty("detectedNum", diagnoseData(POST_TYPE.FACEBOOK, posts, independentUserSyncData.get("facebookSyncTime").getAsString()));
                else
                    result.addProperty("detectedNum", diagnoseAllData(POST_TYPE.FACEBOOK, posts));
                result.addProperty("successSync", true);
            } else if (isSameFacebookAccount && isSameSyncTime) {
                result.addProperty("successSync", true);
            } else {
                result.addProperty("successSync", false);
            }
        }
        return result;
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
        Date latestDateTime = null;

        try {
            if (independentUserSyncData != null)
                if (postType == POST_TYPE.TWITTER && independentUserSyncData.has("twitterId"))
                    latestDateTime = sdf.parse(independentUserSyncData.get("twitterSyncTime").getAsString());
                else if (postType == POST_TYPE.FACEBOOK && independentUserSyncData.has("facebookId"))
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

    private boolean isSameSyncTime(POST_TYPE postType, ArrayList<JsonObject> posts, JsonObject independentUserSyncData) throws ParseException {
        Date current_SynTime = sdf.parse(getLatestDateTime(postType, posts));

        Date syncData_SynTime = null;
        if (postType == POST_TYPE.TWITTER && independentUserSyncData.has("twitterId"))
            syncData_SynTime = sdf.parse(independentUserSyncData.get("twitterSyncTime").getAsString());
        else if (postType == POST_TYPE.FACEBOOK && independentUserSyncData.has("facebookId"))
            syncData_SynTime = sdf.parse(independentUserSyncData.get("facebookSyncTime").getAsString());

        if (syncData_SynTime == null)
            return false;
        else
            return syncData_SynTime.equals(current_SynTime);
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

    public JsonObject getUserSyncData() throws ParseException {
        JsonObject result = new JsonObject();

        JsonObject userSyncData = FireBaseDB.getInstance().getData(syncPath);
        if (userSyncData == null || !isCurrentSyncDataExisted(userSyncData)) {
            result.addProperty("syncDataIsExisted", false);
        } else {
            result = extractIndependentUserSyncData(userSyncData);
            if (result.has("twitterSyncTime")) {
                String twitterSyncTime = result.get("twitterSyncTime").getAsString();
                result.remove("twitterSyncTime");
                result.addProperty("twitterSyncTime", sdf_display.format(sdf.parse(twitterSyncTime)));
            }
            if (result.has("facebookSyncTime")) {
                String facebookSyncTime = result.get("facebookSyncTime").getAsString();
                result.remove("facebookSyncTime");
                result.addProperty("facebookSyncTime", sdf_display.format(sdf.parse(facebookSyncTime)));
            }

            result.addProperty("syncDataIsExisted", true);
        }

        return result;
    }
}
