package CognitiveServices;

import Authentication.UserSyncController;
import Database.FireBaseDB;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiagnosisController {
    private static DiagnosisController instance = null;
    private static JsonObject diagnosticRules = null;

    public static DiagnosisController getInstance() {
        if (instance == null) {
            instance = new DiagnosisController();
            diagnosticRules = FireBaseDB.getInstance().getData("DiagnosticRule");
        }
        return instance;
    }

    public void diagnose(UserSyncController.POST_TYPE postType, ArrayList<JsonObject> posts) throws Exception {
        // testing data
        posts = new ArrayList<>();
        JsonElement etest = new JsonParser().parse("{\"createAt\":\"Wed Jan 10 22:42:00 CST 2018\",\"text\":\"Testing again \",\"photos\":[\"https://pbs.twimg.com/media/DTL-AEDV4AAPpkW.jpg:large\",\"https://pbs.twimg.com/media/DTL-A7TU0AEbhIb.jpg:large\"]}");
        posts.add(etest.getAsJsonObject());

        // handle each post
        for (JsonObject obj : posts) {
            // get key phrases
            String origin_text = obj.get("text").getAsString();
            ArrayList<String> keyPhrases = TextAnalyticsController.getInstance().TextAnalyticsService(origin_text);

            // get other key phrases if Computer Vision service available
            ArrayList<String> keyPhrases_CV;
            if (obj.has("photos")) {
                String captions = "";
                ComputerVisionController cvc = ComputerVisionController.getInstance();

                for (JsonElement e : obj.get("photos").getAsJsonArray()) {
                    captions += cvc.ComputerVisionService(e.getAsString()) + ". ";
                }
                keyPhrases_CV = TextAnalyticsController.getInstance().TextAnalyticsService(captions);
            }

            // compare with rules

            // fit rule => check isSelfSubject + update db
            // else next
        }
    }

    public boolean isSelfSubject(String text, String keyPhrase) {
        text = text.toLowerCase();
        keyPhrase = keyPhrase.toLowerCase();

        ArrayList<RelationTriple> rts = new ArrayList();
        Document doc = new Document(text);
        for (Sentence sentence : doc.sentences()) {
            Collection<RelationTriple> openie = sentence.openieTriples();
            for (RelationTriple r : openie) {
                rts.add(r);
            }
        }

        boolean result = false;
        if (rts.size() == 0) {
            return true;
        } else {
            for (RelationTriple r : rts) {
                if (r.objectGloss().equals(keyPhrase) && r.subjectGloss().equals("i"))
                    result = true;
                if (r.subjectGloss().contains("my") && r.subjectGloss().contains(keyPhrase))
                    result = true;
            }
        }

        return result;
    }

    private boolean isQuotationSentence(String sentence) {
        Pattern pattern = Pattern.compile("\"(.*?)\"");
        Matcher matcher = pattern.matcher(sentence);
        if (matcher.find())
            return true;
        else
            return false;
    }
}
