package InformationExtractor;

import com.google.gson.JsonObject;
import org.springframework.social.facebook.api.PagedList;
import org.springframework.social.facebook.api.Post;

import java.util.ArrayList;

public class FacebookController {
    private static FacebookController instance = null;

    public static FacebookController getInstance() {
        if (instance == null) {
            instance = new FacebookController();
        }
        return instance;
    }

    public ArrayList<JsonObject> getFacebookDetail(PagedList<Post> feed) {
        ArrayList<JsonObject> result = new ArrayList<>();
        for (Post p : feed) {
            String message = p.getMessage();
            if (message == null)
                continue;
            JsonObject obj = new JsonObject();
            obj.addProperty("createAt", p.getCreatedTime().toString());
            obj.addProperty("text", message);
            if (p.getType().equals(Post.PostType.PHOTO))
                obj.addProperty("photos", p.getPicture());
            result.add(obj);
        }

        return result;
    }
}
