package InformationExtractor;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.social.twitter.api.Tweet;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.http.HttpHeaders.USER_AGENT;

public class TwitterController {
    private static TwitterController instance = null;

    public static TwitterController getInstance() {
        if (instance == null) {
            instance = new TwitterController();
        }
        return instance;
    }

    public ArrayList<JsonObject> getTweetDetail(List<Tweet> tweets) {
        ArrayList<JsonObject> result = new ArrayList<>();
        for (Tweet t : tweets) {
            JsonObject obj = new JsonObject();
            obj.addProperty("createAt", t.getCreatedAt().toString());
            // if NOT containing photos
            if (extractTwitterUrl(t.getText()) == null) {
                obj.addProperty("text", t.getText());
            }
            // if containing photos
            else {
                String painMessage = t.getText().replace(extractTwitterUrl(t.getText()), "");
                obj.addProperty("text", painMessage);
                obj.add("photos", extractPhotosUrl(t));
            }
            result.add(obj);
        }

        return result;
    }

    // extract photos url if available in a post
    private JsonArray extractPhotosUrl(Tweet tweet) {
        JsonArray photoURLs = new JsonArray();

        // check containing photos or not
        String twitterUrl = extractTwitterUrl(tweet.getText());
        if (twitterUrl == null)
            return null;
        // if yes, extract the photos url
        List<String> photoUrl_list = extractPhotosUrlFromTwitterUrl(twitterUrl);

        // convert to json array
        for (String photoUrl : photoUrl_list) {
            photoURLs.add(photoUrl);
        }

        return photoURLs;
    }

    private String extractTwitterUrl(String source) {
        List<String> containedUrls = new ArrayList<String>();
        String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(source);

        while (urlMatcher.find()) {
            containedUrls.add(source.substring(urlMatcher.start(0), urlMatcher.end(0)));
        }

        String result_Url = null;
        for (String Url : containedUrls) {
            if (Url.toLowerCase().contains("https://t.co/")) {
                result_Url = Url;
                break;
            }
        }

        return result_Url;
    }

    private List<String> extractPhotosUrlFromTwitterUrl(String twitterUrl) {
        try {
            URL url = new URL(twitterUrl);
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
            String painHeadContent = extractContentInsideTag("head", response.toString()).replaceAll(" ", "");

            Pattern REGEX = Pattern.compile("<metaproperty=\"og:image\"content=\"(.+?)\">");
            List<String> result = new ArrayList<>();
            Matcher matcher = REGEX.matcher(painHeadContent);
            while (matcher.find()) {
                result.add(matcher.group(1));
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    private String extractContentInsideTag(String tag, String source) {
        Pattern TAG_REGEX = Pattern.compile("<" + tag + ">(.+?)</" + tag + ">");
        final Matcher matcher = TAG_REGEX.matcher(source);
        while (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
