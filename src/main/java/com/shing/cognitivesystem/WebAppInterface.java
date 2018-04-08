package com.shing.cognitivesystem;

import Authentication.AuthenticationController;
import Authentication.UserSymptomController;
import Authentication.UserSyncController;
import CognitiveServices.DiagnosisController;
import Database.FireBaseDB;
import InformationExtractor.FacebookController;
import InformationExtractor.SocialStaticData;
import InformationExtractor.TwitterController;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.social.facebook.api.PagedList;
import org.springframework.social.facebook.api.Post;
import org.springframework.social.facebook.api.User;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.TwitterProfile;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
public class WebAppInterface {
    @RequestMapping("di")
    public void di() throws Exception {
        DiagnosisController.getInstance().diagnose(UserSyncController.POST_TYPE.TWITTER, null);
    }

    /* Production Functions */
    @PostMapping("SignUp")
    public String SignUp(@RequestParam("email") String email, @RequestParam("pwd") String pwd) {
        AuthenticationController authController = AuthenticationController.getInstance();
        boolean successSignUp = authController.registerAccountWithEmailPassword(email, pwd);
        JsonObject obj = new JsonObject();
        obj.addProperty("successSignUp", successSignUp);

        return obj.toString();
    }

    @PostMapping("Login")
    public String Login(@RequestParam("email") String email, @RequestParam("pwd") String pwd) {
        AuthenticationController authController = AuthenticationController.getInstance();
        boolean successLogin = authController.loginWithEmailPassword(email, pwd);

        JsonObject obj = new JsonObject();
        obj.addProperty("successLogin", successLogin);

        return obj.toString();
    }

    @PostMapping("Logout")
    public void Logout() {
        AuthenticationController.getInstance().logout();
    }

    @GetMapping("IsLogin")
    public boolean IsLogin() {
        AuthenticationController authController = AuthenticationController.getInstance();
        return authController.isLogin();
    }

    @GetMapping("GetUserInfo")
    public String GetUserInfo() throws ParseException {
        AuthenticationController authController = AuthenticationController.getInstance();
        if (!authController.isLogin())
            return null;

        JsonObject obj = new JsonObject();
        obj.addProperty("email", authController.getCurrentCSUser().getEmail());
        obj.add("userSyncData", UserSyncController.getInstance().getUserSyncData());
        obj.addProperty("hasDetectedRecord", UserSymptomController.getInstance().hasDetectedRecord(authController.getCurrentCSUser().getLocalId()));

        return obj.toString();
    }

    @PostMapping("SyncFacebook")
    public String SyncFacebook() {
        AuthenticationController authController = AuthenticationController.getInstance();
        if (!authController.isLogin())
            return null;

        try {
            User currentFacebookUser = SocialStaticData.facebook.fetchObject("me", User.class, "id", "about", "age_range", "birthday", "context", "cover", "currency", "devices", "education", "email", "favorite_athletes", "favorite_teams", "first_name", "gender", "hometown", "inspirational_people", "installed", "install_type", "is_verified", "languages", "last_name", "link", "locale", "location", "meeting_for", "middle_name", "name", "name_format", "political", "quotes", "payment_pricepoints", "relationship_status", "religion", "security_settings", "significant_other", "sports", "test_group", "timezone", "third_party_id", "updated_time", "verified", "video_upload_limits", "viewer_can_send_gift", "website", "work");
            PagedList<Post> feed = SocialStaticData.facebook.feedOperations().getFeed();
            ArrayList<JsonObject> facebookDetail = FacebookController.getInstance().getFacebookDetail(feed);

            JsonObject obj = UserSyncController.getInstance().syncData_facebook(Long.parseLong(currentFacebookUser.getId()), facebookDetail);
            obj.addProperty("connected", true);

            return obj.toString();
        } catch (Exception e) {
            JsonObject obj = new JsonObject();
            obj.addProperty("connected", false);

            return obj.toString();
        }
    }

    @PostMapping("SyncTwitter")
    public String SyncTwitter() {
        AuthenticationController authController = AuthenticationController.getInstance();
        if (!authController.isLogin())
            return null;

        try {
            TwitterProfile twitterProfile = SocialStaticData.twitter.userOperations().getUserProfile();
            List<Tweet> list = SocialStaticData.twitter.timelineOperations().getUserTimeline();
            ArrayList<JsonObject> tweets = TwitterController.getInstance().getTweetDetail(list);

            JsonObject obj = UserSyncController.getInstance().syncData_twitter(twitterProfile.getId(), tweets);
            obj.addProperty("connected", true);

            return obj.toString();
        } catch (Exception e) {
            JsonObject obj = new JsonObject();
            obj.addProperty("connected", false);

            return obj.toString();
        }
    }

    @GetMapping("GetSymptomCount")
    public String GetSymptomCount() {
        AuthenticationController authController = AuthenticationController.getInstance();
        if (!authController.isLogin())
            return null;

        return UserSymptomController.getInstance().getSymptomCount();
    }

    @GetMapping("GetUserSymptom")
    public String GetUserSymptom() throws ParseException {
        AuthenticationController authController = AuthenticationController.getInstance();
        if (!authController.isLogin())
            return null;

        return UserSymptomController.getInstance().getUserSymptomData(authController.getCurrentCSUser().getLocalId());
    }

    @GetMapping("GetDisorders")
    public String GetDisorders() {
        AuthenticationController authController = AuthenticationController.getInstance();
        if (!authController.isLogin())
            return null;

        return FireBaseDB.getInstance().getData("MentalDisorder").toString();
    }

    @PostMapping("GetSymptom")
    public String GetSymptom(@RequestParam("disorder") String disorder) {
        ArrayList<JsonObject> response = new ArrayList<>();
        Set<Map.Entry<String, JsonElement>> entrySet = FireBaseDB.getInstance().getData("DiagnosticRule").entrySet();
        for (Map.Entry<String, JsonElement> entry : entrySet) {
            JsonObject obj = entry.getValue().getAsJsonObject();
            if (obj.get("disorder").getAsString().equals(disorder)) {
                JsonObject foundObj = new JsonObject();
                foundObj.add(entry.getKey(), obj);
                response.add(foundObj);
            }
        }
        return new Gson().toJson(response);
    }

    @PostMapping("deleteSyncData")
    public void deleteSyncData(@RequestParam("syncType") String syncType) {
        AuthenticationController authController = AuthenticationController.getInstance();
        if (!authController.isLogin())
            return;

        if (syncType.equalsIgnoreCase("twitter"))
            UserSyncController.getInstance().deleteUserSyncData(UserSyncController.POST_TYPE.TWITTER);
        else if (syncType.equalsIgnoreCase("facebook"))
            UserSyncController.getInstance().deleteUserSyncData(UserSyncController.POST_TYPE.FACEBOOK);
    }
}
