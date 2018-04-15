package CognitiveServices;

import Authentication.AuthenticationController;
import Authentication.UserSyncController;
import Database.FireBaseDB;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shing.cognitivesystem.DataInitiationController;
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
import edu.stanford.nlp.trees.Tree;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

import static org.apache.http.HttpHeaders.USER_AGENT;

public class DiagnosisController {
    private static DiagnosisController instance = null;
    private Set<Map.Entry<String, JsonElement>> diagnosticRules = null;
    private ILexicalDatabase wordNetDB = null;
    private StanfordCoreNLP pipeline = null;
    private String externalServiceUrl = null;
    private String externalServiceFunction = null;

    public static DiagnosisController getInstance() {
        if (instance == null) {
            instance = new DiagnosisController();
        }
        return instance;
    }

    private DiagnosisController() {
        diagnosticRules = FireBaseDB.getInstance().getData("DiagnosticRule").entrySet();
        wordNetDB = new NictWordNet();
        externalServiceUrl = "http://localhost:8090/";
        externalServiceFunction = "stanfordcorenlpService";
    }

    public int diagnose(UserSyncController.POST_TYPE postType, ArrayList<JsonObject> posts) throws Exception {
        // TODO:testing data
//        posts = new ArrayList<>();
//        JsonElement etestn = new JsonParser().parse("{\"createAt\":\"Wed Jan 10 22:42:00 CST 2018\",\"text\":\"this message is used for fake.\"}");
//        JsonElement etest0 = new JsonParser().parse("{\"createAt\":\"Wed Jan 10 22:42:00 CST 2018\",\"text\":\"I'm 16 and my blood pressure is HIGH.\",\"photos\":[\"https://pbs.twimg.com/media/DTL-AEDV4AAPpkW.jpg:large\",\"https://pbs.twimg.com/media/DTL-A7TU0AEbhIb.jpg:large\"]}");
//        JsonElement etest = new JsonParser().parse("{\"createAt\":\"Wed Jan 10 22:42:00 CST 2018\",\"text\":\"I have a migraine. It wakes me up every morning after five hours of sleep.\"}");
//        JsonElement etest2 = new JsonParser().parse("{\"createAt\":\"Wed Jan 10 22:42:00 CST 2018\",\"text\":\"I feel fearful talking with anybody.\"}");
//        JsonElement etest3 = new JsonParser().parse("{\"createAt\":\"Wed Jan 10 22:42:00 CST 2018\",\"text\":\"Mary said that \\\"I got headache\\\".\"}");
//        JsonElement etest4 = new JsonParser().parse("{\"createAt\":\"Wed Jan 10 22:42:00 CST 2018\",\"text\":\"I said that \\\"I got headache\\\".\"}");
//        JsonElement etest5 = new JsonParser().parse("{\"createAt\":\"Wed Jan 10 22:42:00 CST 2018\",\"text\":\"I am so overwhelmed by co-workers. I took a new job to limit my stress. I used to be a leader and wanted to reduce my stress so I just became a worker bee. Now I feel so pressured. because I can't say anything to co-workers who are not working and not acting like they are part of the team.\"}");
//        JsonElement etest6 = new JsonParser().parse("{\"createAt\":\"Wed Jan 10 22:42:00 CST 2018\",\"text\":\"I am totally uninterested in anything.\"}");
//        JsonElement etest7 = new JsonParser().parse("{\"createAt\":\"Wed Jan 10 22:42:00 CST 2018\",\"text\":\"feel upset...\"}");
//        JsonElement etest8 = new JsonParser().parse("{\"createAt\":\"Wed Jan 10 22:42:00 CST 2018\",\"text\":\"I have a migraine.\"}");
//        JsonElement etest9 = new JsonParser().parse("{\"createAt\":\"Wed Jan 10 22:42:00 CST 2018\",\"text\":\"I don't have a migraine.\"}");
//        posts.add(etestn.getAsJsonObject());
//        posts.add(etest0.getAsJsonObject());
//        posts.add(etest.getAsJsonObject());
//        posts.add(etest2.getAsJsonObject());
//        posts.add(etest3.getAsJsonObject());
//        posts.add(etest4.getAsJsonObject());
//        posts.add(etest5.getAsJsonObject());
//        posts.add(etest6.getAsJsonObject());
//        posts.add(etest7.getAsJsonObject());
//        posts.add(etest8.getAsJsonObject());
//        posts.add(etest9.getAsJsonObject());

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
                String captions_toText = "";
                ComputerVisionController cvc = ComputerVisionController.getInstance();

                JsonElement photos = obj.get("photos");
                if (photos.isJsonArray()) {
                    for (JsonElement e : obj.get("photos").getAsJsonArray()) {
                        captions += cvc.ComputerVisionService(e.getAsString()) + ". ";
                        captions_toText += "[photo_description: " + captions + "]";
                    }
                } else {
                    captions += cvc.ComputerVisionService(photos.getAsString()) + ". ";
                    captions_toText += "[photo_description: " + captions + "]";
                }
                keyPhrases_CV = TextAnalyticsController.getInstance().TextAnalyticsService(captions);

                // add caption to sentence
                origin_text += " " + captions_toText;
            }

            // compare with rules
            if (keyPhrases != null)
                compareRules(origin_text, postType, createAt, records, keyPhrases);
            if (keyPhrases_CV != null)
                compareRules(origin_text, postType, createAt, records, keyPhrases_CV);
        }

        if (records.size() > 0) {
            // check subject of sentence and quoted sentence
            ArrayList<DetectionRecord> records_copy = (ArrayList<DetectionRecord>) records.clone();
            for (DetectionRecord record : records_copy) {
                if (DataInitiationController.isInit_active()) {
                    // local function
                    if (!isSelfSubject(record) || isQuotationSentence(record) || isFutureTense(record) || isNegation(record))
                        records.remove(record);
                } else {
                    // external function
                    if (stanfordcorenlpService_call(record) || isFutureTense(record) || isNegation(record))
                        records.remove(record);
                }
            }

            // write record to DB
            if (records.size() > 0) {
                for (DetectionRecord record : records) {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("uid", record.getUid());
                    obj.addProperty("messageText", record.getOrigin_text().replaceAll("“", "\"").replaceAll("”", "\"").replaceAll("…", "..."));
                    obj.addProperty("keyPhrase", record.getKeyPhrase());
                    obj.addProperty("createAt", record.getCreatedAt());
                    obj.addProperty("DRId", record.getDiagnosticRule());
                    obj.addProperty("from", record.getFrom());
                    obj.addProperty("postCreateAt", record.getPostCreatedAt());

                    boolean useSemanticSimilarity = record.isUseSemanticSimilarity();
                    obj.addProperty("useSemanticSimilarity", useSemanticSimilarity);
                    if (useSemanticSimilarity) {
                        obj.addProperty("semanticSimilarityScore", record.getSrScore());
                        obj.addProperty("semanticSimilarityPair", record.getSrPair());
                    }

                    FireBaseDB.getInstance().writeData("RuleDetection", obj);
                }
            }
        }

        return records.size();
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
                        double srScore = findSemanticSimilarity(keyPhrase, expectedKeyPhrase_str);
                        if (srScore > 0.9) {
                            addRecord(records, new DetectionRecord(AuthenticationController.getInstance().getCurrentCSUser().getLocalId(), origin_text, postCreateAt, keyPhrase, entry.getKey(), true, srScore, keyPhrase + " - " + expectedKeyPhrase_str), postType);
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

    public boolean isFutureTense(DetectionRecord record) {
        ArrayList detectedWords = new ArrayList();
        ArrayList<Tree> keyPhrases = new ArrayList<>();
        Document doc = new Document(record.getOrigin_text());
        for (Sentence sentence : doc.sentences()) {
            Tree tree = sentence.parse();
            TextAnalyticsController.getInstance().getAllLeaf(keyPhrases, tree);
        }

        if (keyPhrases.size() > 0) {
            for (Tree t : keyPhrases) {
                if (t.toString().contains("MD") && (t.getChild(0).toString().equalsIgnoreCase("will") || t.getChild(0).toString().equalsIgnoreCase("shall")))
                    detectedWords.add(t.getChild(0).toString());
            }
        }

        if (detectedWords.size() > 0)
            return true;
        else
            return false;
    }

    public boolean isNegation(DetectionRecord record) {
        boolean result = false;

        ArrayList<Dependency> dependencies = new ArrayList<>();
        Document doc = new Document(record.getOrigin_text());
        for (Sentence sentence : doc.sentences()) {
            if (!sentence.toString().contains(record.getKeyPhrase()))
                continue;

            List<Optional<Integer>> dependency_index_list = sentence.governors();
            List<Optional<String>> dependency_label_list = sentence.incomingDependencyLabels();
            for (int i = 0; i < dependency_index_list.size(); i++) {
                Optional<Integer> dependency_index = dependency_index_list.get(i);
                Optional<String> dependency_label = dependency_label_list.get(i);
                if (dependency_index.get() != -1)
                    dependencies.add(new Dependency(dependency_label.get(), sentence.word(i), sentence.word(dependency_index.get())));
            }
            break;
        }

        if (dependencies.size() > 0) {
            Dependency neg_dependency = null;
            Dependency linker_dependency = null;
            for (Dependency d : dependencies) {
                if (d.getLabel().equals("neg")) {
                    neg_dependency = d;
                    break;
                }
            }

            if (neg_dependency == null)
                return false;

            for (Dependency d : dependencies) {
                if (d.getTarget().equals(record.getKeyPhrase())) {
                    linker_dependency = d;
                    break;
                }
            }

            if (linker_dependency == null)
                return false;

            if (neg_dependency.getPointer().equals(linker_dependency.getPointer()))
                result = true;
        }

        return result;
    }

    private boolean stanfordcorenlpService_call(DetectionRecord record) {
        try {
            URL url = new URL(externalServiceUrl + externalServiceFunction + "?text=" + URLEncoder.encode(record.getOrigin_text(), "UTF-8") + "&keyPhrase=" + URLEncoder.encode(record.getKeyPhrase(), "UTF-8"));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", USER_AGENT);
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return Boolean.valueOf(response.toString());
        } catch (Exception e) {

        }

        return true;
    }

    private double findSemanticSimilarity(String word1, String word2) {
        WS4JConfiguration.getInstance().setMFS(true);
        double score = new WuPalmer(wordNetDB).calcRelatednessOfWords(word1, word2);
        return score;
    }

    public void initStanfordCoreNLPService() {
        DetectionRecord dummy = DetectionRecord.getDummyDetectRecord();
        isSelfSubject(dummy);
        isQuotationSentence(dummy);
    }
}
