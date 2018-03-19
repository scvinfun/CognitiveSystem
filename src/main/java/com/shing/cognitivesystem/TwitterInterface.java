package com.shing.cognitivesystem;

import Authentication.UserSyncController;
import InformationExtractor.SocialStaticData;
import InformationExtractor.TwitterController;
import com.google.gson.JsonObject;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.TwitterProfile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/tw_login")
public class TwitterInterface {
    private Twitter twitter;
    private ConnectionRepository connectionRepository;

    public TwitterInterface(Twitter twitter, ConnectionRepository connectionRepository) {
        this.twitter = SocialStaticData.twitter = twitter;
        this.connectionRepository = SocialStaticData.tw_connectionRepository = connectionRepository;
    }

    @GetMapping
    public String helloTwitter(Model model) {
        if (connectionRepository.findPrimaryConnection(Twitter.class) == null) {
            return "redirect:/connect/twitter";
        }

        TwitterProfile twitterProfile = twitter.userOperations().getUserProfile();
        List<Tweet> list = twitter.timelineOperations().getUserTimeline();
        ArrayList<JsonObject> tweets = TwitterController.getInstance().getTweetDetail(list);

        boolean success = UserSyncController.getInstance().syncData_twitter(twitterProfile.getId(), tweets);
        model.addAttribute("successSync", success + "");

        return "TwitterSyncResult";
    }

}
