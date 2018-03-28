package CognitiveServices;

import com.google.gson.*;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.trees.Tree;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class Document_TA {
    private String id, language, text;

    public Document_TA(String id, String text) {
        this.id = id;
        this.text = text;
    }

    public Document_TA(String id, String language, String text) {
        this.id = id;
        this.language = language;
        this.text = text;
    }

    public String getId() {
        return this.id;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}

class Documents_TA {
    private List<Document_TA> documents;

    public Documents_TA() {
        this.documents = new ArrayList<Document_TA>();
    }

    public void add(String id, String text) {
        this.documents.add(new Document_TA(id, text));
    }

    public void add(String id, String language, String text) {
        this.documents.add(new Document_TA(id, language, text));
    }

    public void remove(String id) {
        for (int i = 0; i < this.documents.size(); i++)
            if (this.documents.get(i).getId().equals(id))
                this.documents.remove(this.documents.get(i));
    }

    public void updateLanguage() {
        for (Document_TA d : this.documents) {
            d.setLanguage("en");
        }
    }
}

public class TextAnalyticsController {

    private static TextAnalyticsController instance = null;

    public static TextAnalyticsController getInstance() {
        if (instance == null) {
            instance = new TextAnalyticsController();
        }
        return instance;
    }

    public ArrayList<String> TextAnalyticsService(String sentences) throws Exception {
        // detecting language
        Documents_TA documents = new Documents_TA();
        documents.add("0", sentences);
        String languages_response = serviceCall(documents, StaticData.TEXTANALYTICS_TYPE_LANGUAGES);
        JsonObject obj = new JsonParser().parse(languages_response).getAsJsonObject().getAsJsonArray("documents").get(0).getAsJsonObject();
        String[] language = getLanguageBaseOnScore(obj.getAsJsonArray("detectedLanguages")).split("-");
        // not eng language
        if (!language[0].equals("en"))
            return null;
        // is eng language but less score (may contain other language)
        if (Double.valueOf(language[1]) < 0.9)
            return null;

        // extract key phrases including both TA service and parsing
        Document doc = new Document(sentences);
        ArrayList<String> sentences_list = new ArrayList<>();
        for (Sentence sentence : doc.sentences()) {
            sentences_list.add(sentence.toString());
        }
        // TA service
        ArrayList<String> keyPhrases_N = getKeyPhrases(sentences_list);
        // parsing
        ArrayList<String> keyPhrases_A = getAdjKeyPhrases(sentences);

        // error response
        if (keyPhrases_A == null || keyPhrases_N == null)
            return null;

        // combine results of two service
        ArrayList<String> result = new ArrayList<>();
        result.addAll(keyPhrases_N);
        result.addAll(keyPhrases_A);

        // remove duplicated key phrases
        Set<String> hs = new HashSet<>();
        hs.addAll(result);
        result.clear();
        result.addAll(hs);

        return result;
    }

    private String serviceCall(Documents_TA documents, String serviceName) throws Exception {
        String text = new Gson().toJson(documents);
        byte[] encoded_text = text.getBytes("UTF-8");

        URL url = new URL(StaticData.getTextAnalytics_url(serviceName));
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "text/json");
        connection.setRequestProperty("Ocp-Apim-Subscription-Key", StaticData.getTextAnalyics_AccessKey());
        connection.setDoOutput(true);

        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.write(encoded_text, 0, encoded_text.length);
        wr.flush();
        wr.close();

        StringBuilder response = new StringBuilder();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        return response.toString();
    }

    private ArrayList<String> getKeyPhrases(ArrayList<String> sentences) {
        try {
            // convent sentences to class Document
            Documents_TA documents = new Documents_TA();
            for (int i = 0; i < sentences.size(); i++) {
                documents.add(String.valueOf(i + 1), sentences.get(i));
            }

            // get key phrases
            String keyPhrases_response = serviceCall(documents, StaticData.TEXTANALYTICS_TYPE_KEYPHRASES);

            // analyze sentiment
            //String sentiment_response = serviceCall(documents, StaticData.TEXTANALYTICS_TYPE_SENTIMENT);

            // convert response to ArrayList
            JsonParser parser = new JsonParser();
            JsonObject json = parser.parse(keyPhrases_response).getAsJsonObject();
            ArrayList<String> keyPhrases = new ArrayList<>();
            if (json.has("documents")) {
                for (JsonElement e : json.get("documents").getAsJsonArray())
                    for (JsonElement e2 : e.getAsJsonObject().get("keyPhrases").getAsJsonArray())
                        keyPhrases.add(e2.getAsString());
            }

            return keyPhrases;
        } catch (Exception e) {
            return null;
        }
    }

    private String getLanguageBaseOnScore(JsonArray jsonArray) {
        String language = "unknown";
        double score = 0;
        for (JsonElement element : jsonArray) {
            JsonObject obj = element.getAsJsonObject();
            double obj_score = obj.get("score").getAsDouble();
            if (obj_score > score) {
                score = obj_score;
                language = obj.get("iso6391Name").getAsString();
            }
        }

        return language + "-" + score;
    }

    private ArrayList<String> getAdjKeyPhrases(String sentences) {
        sentences = sentences.toLowerCase();
        ArrayList<Tree> keyPhrases = new ArrayList<>();
        ArrayList<String> result = new ArrayList<>();

        Document doc = new Document(sentences);
        for (Sentence sentence : doc.sentences()) {
            Tree tree = sentence.parse();
            getAllLeaf(keyPhrases, tree);

            if (keyPhrases.size() > 0) {
                for (Tree t : keyPhrases) {
                    if (t.toString().contains("JJ") || t.toString().contains("JJR") || t.toString().contains("JJS"))
                        result.add(t.getChild(0).toString());
                }
            }
        }

        return result;
    }

    private void getAllLeaf(ArrayList<Tree> container, Tree tree) {
        if (tree.depth() == 1) {
            container.add(tree);
            return;
        }

        for (Tree t : tree.children()) {
            getAllLeaf(container, t);
        }
    }
}
