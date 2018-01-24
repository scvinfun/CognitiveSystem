package com.shing.cognitivesystem;

import Authentication.AuthenticationController;
import Authentication.CSUser;
import Authentication.Encryptor;
import Authentication.UserSyncController;
import CognitiveServices.ComputerVisionController;
import CognitiveServices.TextAnalyticsController;
import Database.FireBaseDB;
import Logging.LoggingController;
import com.google.gson.JsonObject;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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
    public void test2() {
        String[] words = {"add", "get", "filter", "remove", "check", "find", "collect", "create", "dog", "cat"};

        for (int i = 0; i < words.length - 1; i++) {
            for (int j = i + 1; j < words.length; j++) {
                double distance = compute(words[i], words[j]);
                System.out.println(words[i] + " -  " + words[j] + " = " + distance);
            }
        }
    }

    @RequestMapping("/auth")
    public void test3() throws Exception {
        AuthenticationController authController = AuthenticationController.getInstance();
        CSUser user;
        user = authController.getCurrentCSUser();
        System.out.println("Before Login: " + authController.isLogin());

        authController.loginWithEmailPassword("scvinfun@gmail.com", "vinfun2004");
        user = authController.getCurrentCSUser();
        System.out.println("After Login: " + authController.isLogin());
        LoggingController.getInstance().logging("Test", "Test");

        authController.logout();
        user = authController.getCurrentCSUser();
        System.out.println("After Logout: " + authController.isLogin());
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

    @RequestMapping("/auth_register")
    public void test4() throws Exception {
        AuthenticationController authController = AuthenticationController.getInstance();
        System.out.println(authController.isLogin());
        authController.registerAccountWithEmailPassword("scvinfun@gmail.com", "vinfun2004");
        System.out.println(authController.isLogin());
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
}
