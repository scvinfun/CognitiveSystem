package CognitiveServices;

import Authentication.AuthenticationController;
import Authentication.UserSyncController;
import Database.FireBaseDB;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreQuote;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

import java.util.*;

public class DiagnosisController {
    private static DiagnosisController instance = null;
    private static Set<Map.Entry<String, JsonElement>> diagnosticRules = null;
    private static ILexicalDatabase wordNetDB = null;
    private static StanfordCoreNLP pipeline = null;

    public static DiagnosisController getInstance() {
        if (instance == null) {
            instance = new DiagnosisController();
            diagnosticRules = FireBaseDB.getInstance().getData("DiagnosticRule").entrySet();
            wordNetDB = new NictWordNet();
        }
        return instance;
    }

    public void diagnose(UserSyncController.POST_TYPE postType, ArrayList<JsonObject> posts) throws Exception {
        // TODO:testing data
        posts = new ArrayList<>();
        //JsonElement etest = new JsonParser().parse("{\"createAt\":\"Wed Jan 10 22:42:00 CST 2018\",\"text\":\"Testing again \",\"photos\":[\"https://pbs.twimg.com/media/DTL-AEDV4AAPpkW.jpg:large\",\"https://pbs.twimg.com/media/DTL-A7TU0AEbhIb.jpg:large\"]}");
        JsonElement etest = new JsonParser().parse("{\"createAt\":\"Wed Jan 10 22:42:00 CST 2018\",\"text\":\"I have a migraine. It wakes me up every morning after five hours of sleep.\"}");
        JsonElement etest2 = new JsonParser().parse("{\"createAt\":\"Wed Jan 10 22:42:00 CST 2018\",\"text\":\"I feel fearful talking with anybody.\"}");
        JsonElement etest3 = new JsonParser().parse("{\"createAt\":\"Wed Jan 10 22:42:00 CST 2018\",\"text\":\"Mary said that \\\"I got headache\\\".\"}");
        JsonElement etest4 = new JsonParser().parse("{\"createAt\":\"Wed Jan 10 22:42:00 CST 2018\",\"text\":\"I said that \\\"I got headache\\\".\"}");
        posts.add(etest.getAsJsonObject());
        posts.add(etest2.getAsJsonObject());
        posts.add(etest3.getAsJsonObject());
        posts.add(etest4.getAsJsonObject());

        ArrayList<DetectionRecord> records = new ArrayList<>();
        // handle each post
        for (JsonObject obj : posts) {
            // get key phrases
            String origin_text = obj.get("text").getAsString();
            String createAt = obj.get("createAt").getAsString();
            ArrayList<String> keyPhrases = TextAnalyticsController.getInstance().TextAnalyticsService(origin_text);

            // get other key phrases if Computer Vision service available
            ArrayList<String> keyPhrases_CV = null;
            if (obj.has("photos")) {
                String captions = "";
                ComputerVisionController cvc = ComputerVisionController.getInstance();

                for (JsonElement e : obj.get("photos").getAsJsonArray()) {
                    captions += cvc.ComputerVisionService(e.getAsString()) + ". ";
                }
                keyPhrases_CV = TextAnalyticsController.getInstance().TextAnalyticsService(captions);
            }

            // compare with rules
            compareRules(origin_text, postType, createAt, records, keyPhrases);
            if (keyPhrases_CV != null)
                compareRules(origin_text, postType, createAt, records, keyPhrases_CV);
        }

        if (records.size() > 0) {
            // check subject of sentence and quoted sentence
            ArrayList<DetectionRecord> records_copy = (ArrayList<DetectionRecord>) records.clone();
            for (DetectionRecord record : records_copy) {
                if (!isSelfSubject(record) || isQuotationSentence(record))
                    records.remove(record);
            }

            // write record to DB
            for (DetectionRecord record : records) {
                JsonObject obj = new JsonObject();
                obj.addProperty("uid", record.getUid());
                obj.addProperty("messageText", record.getOrigin_text());
                obj.addProperty("keyPhrase", record.getKeyPhrase());
                obj.addProperty("createAt", record.getCreatedAt());
                obj.addProperty("DRiD", record.getDiagnosticRule());
                obj.addProperty("from", record.getFrom());
                obj.addProperty("postCreateAt", record.getPostCreatedAt());
                obj.addProperty("useSemanticSimilarity", record.isUseSemanticSimilarity());

                FireBaseDB.getInstance().writeData("RuleDetection", obj);
            }
        }
    }

    private void compareRules(String origin_text, UserSyncController.POST_TYPE postType, String postCreateAt, ArrayList<DetectionRecord> records, ArrayList<String> keyPhrases) {
        for (String keyPhrase : keyPhrases) {
            for (Map.Entry<String, JsonElement> entry : diagnosticRules) {
                boolean breaker = false;
                JsonArray expectedKeyPhrases = entry.getValue().getAsJsonObject().get("expectedKeyPhrases").getAsJsonArray();
                for (JsonElement expectedKeyPhrase : expectedKeyPhrases) {
                    String expectedKeyPhrase_str = expectedKeyPhrase.getAsString();
                    if (keyPhrase.equalsIgnoreCase(expectedKeyPhrase_str)) {
                        addRecord(records, new DetectionRecord(AuthenticationController.getInstance().getCurrentCSUser().getLocalId(), origin_text, postCreateAt, keyPhrase, entry.getKey(), false), postType);
                        breaker = true;
                        break;
                    } else {
                        // find semantic similarity
                        double sr = findSemanticSimilarity(keyPhrase, expectedKeyPhrase_str);
                        if (sr > 0.9) {
                            addRecord(records, new DetectionRecord(AuthenticationController.getInstance().getCurrentCSUser().getLocalId(), origin_text, postCreateAt, keyPhrase, entry.getKey(), true), postType);
                            breaker = true;
                            break;
                        }
                    }
                }
                if (breaker)
                    break;
            }
        }
    }

    private void addRecord(ArrayList<DetectionRecord> source, DetectionRecord record, UserSyncController.POST_TYPE postType) {
        if (postType == UserSyncController.POST_TYPE.TWITTER)
            record.setFrom("TWITTER");
        else
            record.setFrom("FACEBOOK");
        source.add(record);
    }

    private boolean isSelfSubject(DetectionRecord record) {
        String text = record.getOrigin_text().toLowerCase();
        String keyPhrase = record.getKeyPhrase().toLowerCase();

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
                if (r.objectGloss().equals(keyPhrase) && r.subjectGloss().equalsIgnoreCase("i"))
                    result = true;
                if (r.subjectGloss().contains("my") && r.subjectGloss().contains(keyPhrase))
                    result = true;
            }
        }

        return result;
    }

    private boolean isQuotationSentence(DetectionRecord record) {
        if (pipeline == null) {
            Properties props = new Properties();
            props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, depparse, coref, quote");
            pipeline = new StanfordCoreNLP(props);
        }
        CoreDocument document = new CoreDocument(record.getOrigin_text());
        pipeline.annotate(document);

        List<CoreQuote> quotes = document.quotes();
        if (quotes.size() > 0)
            for (CoreQuote q : quotes)
                if (q.hasSpeaker && !q.speaker().get().equalsIgnoreCase("I"))
                    return true;
        return false;
    }

    private double findSemanticSimilarity(String word1, String word2) {
        WS4JConfiguration.getInstance().setMFS(true);
        double score = new WuPalmer(wordNetDB).calcRelatednessOfWords(word1, word2);
        return score;
    }
}
