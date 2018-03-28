package com.shing.cognitivesystem;

import Authentication.AuthenticationController;
import Authentication.UserSyncController;
import CognitiveServices.DiagnosisController;
import CognitiveServices.TextAnalyticsController;
import Database.FireBaseDB;
import InformationExtractor.FacebookController;
import InformationExtractor.SocialStaticData;
import InformationExtractor.TwitterController;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import org.springframework.social.facebook.api.PagedList;
import org.springframework.social.facebook.api.Post;
import org.springframework.social.facebook.api.User;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.TwitterProfile;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.http.HttpHeaders.USER_AGENT;

@RestController
public class WebAppInterface {
    private static ILexicalDatabase db = new NictWordNet();

    private static double compute(String word1, String word2) {
        WS4JConfiguration.getInstance().setMFS(true);
        double s = new WuPalmer(db).calcRelatednessOfWords(word1, word2);
        return s;
    }

    @RequestMapping("/sr")
    public String sr() {
        String[] words = {"uneasy", "anxious", "worried"};
        String result = "";
        for (int i = 0; i < words.length - 1; i++) {
            for (int j = i + 1; j < words.length; j++) {
                double distance = compute(words[i], words[j]);
                System.out.println(words[i] + " -  " + words[j] + " = " + distance);
                result += result + words[i] + " -  " + words[j] + " = " + distance + "\n";
            }
        }

        return result;
    }

    @RequestMapping("/sr_2")
    public String sr_2() {
        String[] words = {"tired", "exhausted", "fatigued", "headache", "migraine", "insomnia", "wakefulness", "dog", "cat"};
        String result = "";

        for (int i = 0; i < words.length - 1; i++) {
            for (int j = i + 1; j < words.length; j++) {
                try {
                    URL url = new URL("http://maraca.d.umn.edu/cgi-bin/similarity/similarity.cgi?word1=" + words[i] + "&senses1=all&word2=" + words[j] + "&senses2=all&measure=wup&rootnode=yes");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setRequestProperty("User-Agent", USER_AGENT);
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    String extraction = extractContentInsideTag("p class=\"results\"", response.toString());
                    if (extraction == null)
                        extraction = "The relatedness of " + words[i] + " and " + words[j] + " using wup is 0.";
                    result += "<p>" + extraction + "</p>";
                } catch (Exception e) {

                }
            }
        }

        return result;
    }

    private String extractContentInsideTag(String tag, String source) {
        Pattern TAG_REGEX = Pattern.compile("<" + tag + ">(.+?)</p>");
        final Matcher matcher = TAG_REGEX.matcher(source);
        while (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private ArrayList<String> extractContentInsideTag2(String source) {
        ArrayList<String> result = new ArrayList<>();
        Pattern TAG_REGEX = Pattern.compile("<(.+?)>");
        final Matcher matcher = TAG_REGEX.matcher(source);
        while (matcher.find()) {
            result.add(matcher.group(1));
        }
        return result;
    }

    @RequestMapping("ta")
    public void ta() throws Exception {
        TextAnalyticsController.getInstance().TextAnalyticsService("I really enjoy the new XBox One S. It has a clean look, it has 4K/HDR resolution and it is affordable." + "The Grand Hotel is a new hotel in the center of Seattle. It earned 5 stars in my review, and has the classiest decor I've ever seen.");
    }

    @RequestMapping("di")
    public void test5() throws Exception {
        AuthenticationController.getInstance().loginWithEmailPassword("sukm2004@gmail.com", "sukm2004");
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

        return obj.toString();
    }

    @PostMapping("SyncFacebook")
    public String SyncFacebook() throws ParseException {
        AuthenticationController authController = AuthenticationController.getInstance();
        if (!authController.isLogin())
            return null;

        User currentFacebookUser = SocialStaticData.facebook.fetchObject("me", User.class, "id", "about", "age_range", "birthday", "context", "cover", "currency", "devices", "education", "email", "favorite_athletes", "favorite_teams", "first_name", "gender", "hometown", "inspirational_people", "installed", "install_type", "is_verified", "languages", "last_name", "link", "locale", "location", "meeting_for", "middle_name", "name", "name_format", "political", "quotes", "payment_pricepoints", "relationship_status", "religion", "security_settings", "significant_other", "sports", "test_group", "timezone", "third_party_id", "updated_time", "verified", "video_upload_limits", "viewer_can_send_gift", "website", "work");
        PagedList<Post> feed = SocialStaticData.facebook.feedOperations().getFeed();
        ArrayList<JsonObject> facebookDetail = FacebookController.getInstance().getFacebookDetail(feed);

        boolean success = UserSyncController.getInstance().syncData_facebook(Long.parseLong(currentFacebookUser.getId()), facebookDetail);

        JsonObject obj = new JsonObject();
        obj.addProperty("successSync", success);

        return obj.toString();
    }

    @PostMapping("SyncTwitter")
    public String SyncTwitter() throws ParseException {
        AuthenticationController authController = AuthenticationController.getInstance();
        if (!authController.isLogin())
            return null;

        TwitterProfile twitterProfile = SocialStaticData.twitter.userOperations().getUserProfile();
        List<Tweet> list = SocialStaticData.twitter.timelineOperations().getUserTimeline();
        ArrayList<JsonObject> tweets = TwitterController.getInstance().getTweetDetail(list);

        boolean success = UserSyncController.getInstance().syncData_twitter(twitterProfile.getId(), tweets);

        JsonObject obj = new JsonObject();
        obj.addProperty("successSync", success);

        return obj.toString();
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
}
