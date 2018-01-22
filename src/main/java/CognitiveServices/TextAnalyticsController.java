package CognitiveServices;

import com.google.gson.*;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

class Document {
    private String id, language, text;

    public Document(String id, String text) {
        this.id = id;
        this.text = text;
    }

    public Document(String id, String language, String text) {
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

class Documents {
    private List<Document> documents;

    public Documents() {
        this.documents = new ArrayList<Document>();
    }

    public void add(String id, String text) {
        this.documents.add(new Document(id, text));
    }

    public void add(String id, String language, String text) {
        this.documents.add(new Document(id, language, text));
    }

    public void remove(String id) {
        for (int i = 0; i < this.documents.size(); i++)
            if (this.documents.get(i).getId().equals(id))
                this.documents.remove(this.documents.get(i));
    }

    public void updateLanguage() {
        for (Document d : this.documents) {
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

    private String serviceCall(Documents documents, String serviceName) throws Exception {
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

    private String prettify(String json_text) {
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(json_text).getAsJsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(json);
    }

    public String TextAnalyticsService(String... sentences) {
        try {
            Documents documents = new Documents();
            // detecting language
            for (int i = 0; i < sentences.length; i++) {
                documents.add(String.valueOf(i + 1), sentences[i]);
            }
            String languages_response = serviceCall(documents, StaticData.TEXTANALYTICS_TYPE_LANGUAGES);

            // remove the sentences which are not written in english
            ArrayList<String> wrongList = new ArrayList<>();
            JsonArray json_array = new JsonParser().parse(languages_response).getAsJsonObject().getAsJsonArray("documents");
            for (JsonElement element : json_array) {
                JsonObject obj = element.getAsJsonObject();
                String language = getLanguageBaseOnScore(obj.getAsJsonArray("detectedLanguages"));
                if (!language.equals("en"))
                    wrongList.add(obj.get("id").getAsString());
            }
            if (wrongList.size() != 0) {
                for (String id : wrongList) {
                    documents.remove(id);
                }
                documents.updateLanguage();
            }

            // get key phrases
            String keyPhrases_response = serviceCall(documents, StaticData.TEXTANALYTICS_TYPE_KEYPHRASES);

            // analyze sentiment
            String sentiment_response = serviceCall(documents, StaticData.TEXTANALYTICS_TYPE_SENTIMENT);

            return prettify(keyPhrases_response);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private String getLanguageBaseOnScore(JsonArray jsonArray) {
        String language = "unknown";
        double score = 0;
        for (JsonElement element : jsonArray) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.get("score").getAsDouble() > score)
                language = obj.get("iso6391Name").getAsString();
        }

        return language;
    }
}
