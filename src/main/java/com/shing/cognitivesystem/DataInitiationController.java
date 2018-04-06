package com.shing.cognitivesystem;

import Authentication.AuthenticationController;
import CognitiveServices.DiagnosisController;

public class DataInitiationController {
    private static boolean init_active = false;
    private static boolean autoLogin_active = false;

    public static void init() {
        if (DataInitiationController.isInit_active()) {
            System.out.println("==========  START  ==========");
            AuthenticationController ac = AuthenticationController.getInstance();
            ac.fake_login();
            DiagnosisController.getInstance().initStanfordCoreNLPService();
            ac.logout();
            System.out.println("<=========  INIT MESSAGE  =========>");
            System.out.println("INITIATION IS FINISHED");
            System.out.println("");
            System.out.println("==========  END  ==========");
        }
    }

    public static void autoLogin() {
        if (DataInitiationController.isAutoLogin_active()) {
            AuthenticationController.getInstance().loginWithEmailPassword("sukm2004@gmail.com", "sukm2004");
        }
    }

    public static boolean isInit_active() {
        return init_active;
    }

    public static void setInit_active(boolean init_active) {
        DataInitiationController.init_active = init_active;
    }

    public static boolean isAutoLogin_active() {
        return autoLogin_active;
    }

    public static void setAutoLogin_active(boolean autoLogin_active) {
        DataInitiationController.autoLogin_active = autoLogin_active;
    }
}
