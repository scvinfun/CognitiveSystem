package CognitiveServices;

import Authentication.AuthenticationController;
import Database.FireBaseDB;
import com.google.gson.JsonObject;
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

    public void diagnose(ArrayList<JsonObject> posts) {
        // handle each post
        for (JsonObject obj : posts) {
            String origin_text = obj.get("text").getAsString();

            // handle each sentence in a single post
            Document doc = new Document(origin_text);
            ArrayList<String> keyPhrases = new ArrayList<>();
            for (Sentence sentence : doc.sentences()) {
                // TA get key phrases

                //keyPhrases.add();
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
