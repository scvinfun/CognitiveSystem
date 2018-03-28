package CognitiveServices;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.net.URI;

public class ComputerVisionController {
    private static ComputerVisionController instance = null;

    public static ComputerVisionController getInstance() {
        if (instance == null) {
            instance = new ComputerVisionController();
        }
        return instance;
    }

    public String ComputerVisionService(String imageUrl) {
        String result = "Empty Content";
        HttpClient httpclient = new DefaultHttpClient();
        try {
            URIBuilder builder = new URIBuilder(StaticData.COMPUTERVISION_URLBASE);

            // Request parameters. All of them are optional.
            builder.setParameter("visualFeatures", "Categories,Description,Color");
            builder.setParameter("language", "en");

            // Prepare the URI for the REST API call.
            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);

            // Request headers.
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Ocp-Apim-Subscription-Key", StaticData.getComputerVision_SubscriptionKey());

            // Request body.
            StringEntity reqEntity = new StringEntity("{\"url\":\"" + imageUrl + "\"}");
            request.setEntity(reqEntity);

            // Execute the REST API call and get the response entity.
            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                // Format and display the JSON response.
                String jsonString = EntityUtils.toString(entity);
                JSONObject json = new JSONObject(jsonString);
                result = getCaptionBasedOnConfidence(json.toString(2));
            }
        } catch (Exception e) {
            result = null;
        }

        return result;
    }

    private String getCaptionBasedOnConfidence(String response) {
        JsonObject obj = new JsonParser().parse(response).getAsJsonObject();
        JsonArray captions = obj.get("description").getAsJsonObject().get("captions").getAsJsonArray();
        double confidence = 0;
        String result = "";
        for (JsonElement e : captions) {
            JsonObject e_JsonObject = e.getAsJsonObject();
            double e_confidence = e_JsonObject.get("confidence").getAsDouble();
            if (e_confidence > confidence) {
                confidence = e_confidence;
                result = e_JsonObject.get("text").getAsString();
            }
        }
        return result;

    }
}
