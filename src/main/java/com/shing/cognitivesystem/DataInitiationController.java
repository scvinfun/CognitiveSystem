package com.shing.cognitivesystem;

import Authentication.AuthenticationController;
import CognitiveServices.DiagnosisController;

public class DataInitiationController {
    public static void init(boolean decision) {
        if (decision) {
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
}
