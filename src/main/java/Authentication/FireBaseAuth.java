package Authentication;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class FireBaseAuth {
    private static final String BASE_URL = "https://www.googleapis.com/identitytoolkit/v3/relyingparty/";
    public static final String OPERATION_SIGNUP = "signupNewUser";
    public static final String OPERATION_SIGNIN = "verifyPassword";
    public static final String OPERATION_GETUSER = "getAccountInfo";
    private String firebaseKey;
    private static FireBaseAuth instance = null;

    private FireBaseAuth() {
        firebaseKey = "AIzaSyApYoWCQ_VzagyZddJbCjLoBIvZ__RBzCw";
    }

    public static FireBaseAuth getInstance() {
        if (instance == null) {
            instance = new FireBaseAuth();
        }
        return instance;
    }

    public JsonObject authenticationCall(String operation, JsonObject body) throws Exception {
        HttpURLConnection urlRequest = null;
        JsonObject rootobj = null;
        try {
            URL url = new URL(BASE_URL + operation + "?key=" + firebaseKey);
            urlRequest = (HttpURLConnection) url.openConnection();
            urlRequest.setDoOutput(true);
            urlRequest.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            OutputStream os = urlRequest.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
            if (operation.equals(OPERATION_SIGNIN) || operation.equals(OPERATION_SIGNUP)) {
                osw.write("{\"email\":\"" + body.get("email").getAsString() + "\",\"password\":\"" + body.get("password").getAsString() + "\",\"returnSecureToken\":" + body.get("returnSecureToken").getAsString() + "}");
            } else if (operation.equals(OPERATION_GETUSER)) {
                osw.write("{\"idToken\":\"" + body.get("idToken").getAsString() + "\"}");
            }
            osw.flush();
            osw.close();
            urlRequest.connect();
            JsonParser jp = new JsonParser(); //from gson
            JsonElement root = jp.parse(new InputStreamReader((InputStream) urlRequest.getContent())); //Convert the input stream to a json element
            rootobj = root.getAsJsonObject(); //May be an array, may be an object.
        } catch (Exception e) {
            return null;
        } finally {
            urlRequest.disconnect();
        }

        return rootobj;
    }
}
