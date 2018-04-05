package com.shing.cognitivesystem;

import Authentication.AuthenticationController;
import CognitiveServices.DiagnosisController;

public class DataInitiationController {
    private static boolean active = false;

    public static void init() {
        if (DataInitiationController.isActive()) {
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

    public static void setActive(boolean active) {
        DataInitiationController.active = active;
    }

    public static boolean isActive() {
        return active;
    }
}
