package Authentication;

import Common.CS_DateFormatter;
import Database.FireBaseDB;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UserSymptomController {
    private static UserSymptomController instance = null;
    private String path_userSymptom = null;
    private String path_symptoms = null;

    public static UserSymptomController getInstance() {
        if (instance == null) {
            instance = new UserSymptomController();
        }
        return instance;
    }

    private UserSymptomController() {
        path_userSymptom = "RuleDetection";
        path_symptoms = "DiagnosticRule";
    }

    public String getSymptomCount() {
        HashMap<String, Integer> disorders = new HashMap<>();

        JsonObject symptoms_json = FireBaseDB.getInstance().getData(path_symptoms);
        Set<Map.Entry<String, JsonElement>> entrySet = symptoms_json.entrySet();
        for (Map.Entry<String, JsonElement> entry : entrySet) {
            String key = entry.getKey().split("_")[1];
            if (!disorders.containsKey(key))
                disorders.put(key, 1);
            else
                disorders.put(key, disorders.get(key) + 1);
        }

        return new Gson().toJson(disorders);
    }

    public boolean hasDetectedRecord(String uid) {
        JsonObject ruleDetection = getAllUserSymptomData();
        try {
            Set<Map.Entry<String, JsonElement>> entrySet = ruleDetection.entrySet();
            for (Map.Entry<String, JsonElement> entry : entrySet) {
                JsonObject obj = entry.getValue().getAsJsonObject();
                if (obj.get("uid").getAsString().equals(uid))
                    return true;
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public String getUserSymptomData(String uid) throws ParseException {
        ArrayList<JsonObject> userSymptoms = new ArrayList<>();
        JsonObject ruleDetection = getAllUserSymptomData();

        // get user symptom
        Set<Map.Entry<String, JsonElement>> entrySet = ruleDetection.entrySet();
        for (Map.Entry<String, JsonElement> entry : entrySet) {
            JsonObject obj = entry.getValue().getAsJsonObject();
            if (obj.get("uid").getAsString().equals(uid)) {
                String createAt_str = obj.get("createAt").getAsString();
                String postCreateAt_str = obj.get("postCreateAt").getAsString();
                obj.addProperty("createAt", CS_DateFormatter.toDiplayDateFormat(createAt_str));
                obj.addProperty("postCreateAt", CS_DateFormatter.toDiplayDateFormat(postCreateAt_str));
                userSymptoms.add(obj);
            }
        }

        // get symptom names
        ArrayList<String> symptom_names = new ArrayList<>();
        for (JsonObject obj_userSymptoms : userSymptoms) {
            String symptom_name = obj_userSymptoms.get("DRId").getAsString();
            if (!symptom_names.contains(symptom_name))
                symptom_names.add(symptom_name);
        }

        // classify
        Map<String, ArrayList<JsonObject>> result = new HashMap<>();
        for (String name : symptom_names) {
            ArrayList<JsonObject> userSymptoms_copy = (ArrayList<JsonObject>) userSymptoms.clone();
            ArrayList<JsonObject> userSymptoms_group = new ArrayList<>();
            for (JsonObject obj_userSymptoms : userSymptoms_copy) {
                if (name.equals(obj_userSymptoms.get("DRId").getAsString())) {
                    userSymptoms_group.add(obj_userSymptoms);
                    userSymptoms.remove(obj_userSymptoms);
                }
            }
            result.put(name, userSymptoms_group);
        }

        return new Gson().toJson(result);
    }

    public void deleteUserSymptomData(String uid, UserSyncController.POST_TYPE type) {
        String postType = "";
        if (type == UserSyncController.POST_TYPE.TWITTER)
            postType = "TWITTER";
        else if (type == UserSyncController.POST_TYPE.FACEBOOK)
            postType = "FACEBOOK";

        ArrayList<JsonObject> userSymptoms = new ArrayList<>();
        JsonObject ruleDetection = getAllUserSymptomData();

        // get user symptom
        Set<Map.Entry<String, JsonElement>> entrySet = ruleDetection.entrySet();
        for (Map.Entry<String, JsonElement> entry : entrySet) {
            JsonObject obj = entry.getValue().getAsJsonObject();
            if (obj.get("uid").getAsString().equals(uid) && obj.get("from").getAsString().equals(postType)) {
                obj.addProperty("key", entry.getKey());
                userSymptoms.add(obj);
            }
        }

        // delete records
        for (JsonObject obj : userSymptoms)
            FireBaseDB.getInstance().deleteData(path_userSymptom + "/" + obj.get("key").getAsString());
    }

    private JsonObject getAllUserSymptomData() {
        return FireBaseDB.getInstance().getData(path_userSymptom);
    }
}
