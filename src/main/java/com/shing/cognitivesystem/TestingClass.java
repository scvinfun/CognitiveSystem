package com.shing.cognitivesystem;

import Authentication.AuthenticationController;
import Authentication.Encryptor;
import Authentication.UserSyncController;
import CognitiveServices.ComputerVisionController;
import CognitiveServices.TextAnalyticsController;
import Database.FireBaseDB;
import com.google.gson.JsonObject;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.http.HttpHeaders.USER_AGENT;

@RestController
public class TestingClass {
    @RequestMapping("/ta")
    public String test_ta() {
        return TextAnalyticsController.getInstance().TextAnalyticsService("这是一个用中文写的文件", "Este ha sido un dia terrible, llegué tarde al trabajo debido a un accidente automobilistico.", "I really enjoy the new XBox One S. It has a clean look, it has 4K/HDR resolution and it is affordable.", "The Grand Hotel is a new hotel in the center of Seattle. It earned 5 stars in my review, and has the classiest decor I've ever seen.");
    }

    @RequestMapping("/cv")
    public String test_cv() {
        return ComputerVisionController.getInstance().ComputerVisionService("https://upload.wikimedia.org/wikipedia/commons/1/12/Broadway_and_Times_Square_by_night.jpg");
    }

    private static ILexicalDatabase db = new NictWordNet();

    private static double compute(String word1, String word2) {
        WS4JConfiguration.getInstance().setMFS(true);
        double s = new WuPalmer(db).calcRelatednessOfWords(word1, word2);
        return s;
    }

    @RequestMapping("/sr")
    public String test2() {
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
    public String test2_2() {
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

    @RequestMapping("/db")
    public void test6() throws Exception {
        AuthenticationController.getInstance().loginWithEmailPassword("scvinfun@gmail.com", "vinfun2004");
        System.out.println(FireBaseDB.getInstance().getData("fad"));
        JsonObject obj = new JsonObject();
        obj.addProperty("test", 1);
        obj.addProperty("tse", true);
        obj.addProperty("rr", "test");
        FireBaseDB.getInstance().writeData("path", obj);
    }

    @RequestMapping("/en")
    public void test5() {
        Encryptor encryptor = Encryptor.getInstance();
        String s = encryptor.encrypt("vinfun2004");
        System.out.println("E:" + s + " D:" + encryptor.decrypt(s));
    }

    @RequestMapping("/sync")
    public void test7() {
        AuthenticationController.getInstance().loginWithEmailPassword("scvinfun@gmail.com", "vinfun2004");
        UserSyncController usc = UserSyncController.getInstance();
    }

    @RequestMapping("/patch")
    public void test8() {
        AuthenticationController.getInstance().loginWithEmailPassword("scvinfun@gmail.com", "vinfun2004");
        JsonObject r = new JsonObject();
        r.addProperty("rr", "fdfa");
        FireBaseDB.getInstance().modifyData("path/-L2j1JIjs0s4XxoxkZSI", r);
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
    public String GetUserInfo() {
        JsonObject obj = new JsonObject();
        AuthenticationController authController = AuthenticationController.getInstance();
        obj.addProperty("email", authController.getCurrentCSUser().getEmail());

        return obj.toString();
    }
}
