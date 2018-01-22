package com.shing.cognitivesystem;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.twitter.api.CursoredList;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.TwitterProfile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.http.HttpHeaders.USER_AGENT;

@Controller
@RequestMapping("/")
public class TwitterController {
    private Twitter twitter;

    private ConnectionRepository connectionRepository;

    @Inject
    public TwitterController(Twitter twitter, ConnectionRepository connectionRepository) {
        this.twitter = twitter;
        this.connectionRepository = connectionRepository;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String helloTwitter(Model model) {
        if (connectionRepository.findPrimaryConnection(Twitter.class) == null) {
            return "redirect:/connect/twitter";
        }

        model.addAttribute(twitter.userOperations().getUserProfile());
        CursoredList<TwitterProfile> friends = twitter.friendOperations().getFriends();
        model.addAttribute("friends", friends);
        List<Tweet> list = twitter.timelineOperations().getUserTimeline();
        ArrayList<JsonObject> tweets = new ArrayList<>();
        for (Tweet t : list) {
            JsonObject obj = new JsonObject();
            obj.addProperty("createAt", t.getCreatedAt().toString());
            if (getPostUrlFromString(t.getText()) == null) {
                obj.addProperty("text", t.getText());
            } else {
                obj.addProperty("text", t.getText().replace(getPostUrlFromString(t.getText()), ""));
                obj.add("photos", getPhotoUrlList(t));
            }
            tweets.add(obj);
        }

        return "hello2";
    }

    private JsonArray getPhotoUrlList(Tweet tweet) {
        JsonArray photoURLs = new JsonArray();
        String postUrl = getPostUrlFromString(tweet.getText());
        if (postUrl == null)
            return null;
        List<String> photoUrl_list = getPhotoUrlsFromPostsUrl(postUrl);
        if (photoUrl_list == null)
            return null;
        for (String photoUrl : photoUrl_list) {
            photoURLs.add(photoUrl);
        }

        return photoURLs;
    }

    private List<String> getPhotoUrlsFromPostsUrl(String postsUrl) {
        try {
            URL url = new URL(postsUrl);
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
            String painHeadContent = getTagValues("head", response.toString()).get(0).replaceAll(" ", "");

            return getPhotosContent(painHeadContent);
        } catch (Exception e) {
            return null;
        }
    }

    private String getPostUrlFromString(String str) {
        List<String> containedUrls = new ArrayList<String>();
        String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(str);

        while (urlMatcher.find()) {
            containedUrls.add(str.substring(urlMatcher.start(0), urlMatcher.end(0)));
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

    private List<String> getTagValues(String tag, String content) {
        Pattern TAG_REGEX = Pattern.compile("<" + tag + ">(.+?)</" + tag + ">");
        final List<String> tagValues = new ArrayList<>();
        final Matcher matcher = TAG_REGEX.matcher(content);
        while (matcher.find()) {
            tagValues.add(matcher.group(1));
        }
        return tagValues;
    }

    private List<String> getPhotosContent(String content) {
        Pattern REGEX = Pattern.compile("<metaproperty=\"og:image\"content=\"(.+?)\">");
        final List<String> tagValues = new ArrayList<>();
        final Matcher matcher = REGEX.matcher(content);
        while (matcher.find()) {
            tagValues.add(matcher.group(1));
        }
        return tagValues;
    }
}
