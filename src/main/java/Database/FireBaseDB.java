package Database;

import Authentication.AuthenticationController;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.apache.http.HttpHeaders.USER_AGENT;

public class FireBaseDB {
    private static FireBaseDB instance = null;
    private static String BASE_URL = null;
    private String projectId;

    public static FireBaseDB getInstance() {
        if (instance == null) {
            instance = new FireBaseDB();
        }
        return instance;
    }

    private FireBaseDB() {
        projectId = "cognitive-system";
        BASE_URL = "https://" + projectId + ".firebaseio.com/";
    }

    public JsonObject getAllData() {
        return getData("");
    }

    public JsonObject getData(String path) {
        AuthenticationController authController = AuthenticationController.getInstance();
        if (!authController.isLogin())
            return null;

        try {
            URL url = new URL(BASE_URL + path + ".json?auth=" + authController.getIdToken());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JsonObject obj;
            String response_str = response.toString();
            if (isJsonValid(response_str)) {
                JsonParser parser = new JsonParser();
                obj = parser.parse(response_str).getAsJsonObject();
            } else {
                obj = new JsonObject();
                String[] path_elements = path.split("/");
                obj.addProperty(path_elements[path_elements.length - 1], response_str.substring(1, response_str.length() - 1));
            }

            return obj;
        } catch (Exception e) {
            return null;
        }
    }

    public JsonObject getAuthKeyData() {
        try {
            URL url = new URL(BASE_URL + "AuthKey.json");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JsonObject obj;
            String response_str = response.toString();
            JsonParser parser = new JsonParser();
            obj = parser.parse(response_str).getAsJsonObject();

            return obj;
        } catch (Exception e) {
            return null;
        }
    }

    public void writeData(String path, JsonObject data) {
        AuthenticationController authController = AuthenticationController.getInstance();
        if (!authController.isLogin())
            return;

        HttpClient httpClient = HttpClientBuilder.create().build();
        try {
            HttpPost request = new HttpPost(BASE_URL + path + ".json?auth=" + authController.getIdToken());
            StringEntity params = new StringEntity(data.toString());
            request.addHeader("content-type", "application/x-www-form-urlencoded");
            request.setEntity(params);
            httpClient.execute(request);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private boolean isJsonValid(String json_str) {
        try {
            new JSONObject(json_str);
        } catch (JSONException ex) {
            try {
                new JSONArray(json_str);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }
}
