package Database;

import Authentication.AuthenticationController;

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

    public String getData(String path) {
        AuthenticationController authController = AuthenticationController.getInstance();
        if (!authController.isLogin())
            return null;

        try {
            URL url = new URL(BASE_URL + path + ".json");
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

            return response.toString();
        } catch (Exception e) {
            return e.getMessage();
        }

    }
}
