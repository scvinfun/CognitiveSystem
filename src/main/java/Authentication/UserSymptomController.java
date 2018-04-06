package Authentication;

import Database.FireBaseDB;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UserSymptomController {
    private static UserSymptomController instance = null;
    private String path = null;

    public static UserSymptomController getInstance() {
        if (instance == null) {
            instance = new UserSymptomController();
        }
        return instance;
    }

    private UserSymptomController() {
        path = "RuleDetection";
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

    public String getUserSymptomData(String uid) {
        ArrayList<JsonObject> userSymptoms = new ArrayList<>();
        JsonObject ruleDetection = getAllUserSymptomData();

        // get user symptom
        Set<Map.Entry<String, JsonElement>> entrySet = ruleDetection.entrySet();
        for (Map.Entry<String, JsonElement> entry : entrySet) {
            JsonObject obj = entry.getValue().getAsJsonObject();
            if (obj.get("uid").getAsString().equals(uid))
                userSymptoms.add(obj);
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

    private JsonObject getAllUserSymptomData() {
        return FireBaseDB.getInstance().getData(path);
    }
}
