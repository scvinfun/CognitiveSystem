package com.shing.cognitivesystem;

import Authentication.UserSyncController;
import InformationExtractor.FacebookController;
import InformationExtractor.SocialStaticData;
import com.google.gson.JsonObject;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.PagedList;
import org.springframework.social.facebook.api.Post;
import org.springframework.social.facebook.api.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.text.ParseException;
import java.util.ArrayList;

@Controller
@RequestMapping("/fb_login")
public class FacebookInterface {
    private Facebook facebook;
    private ConnectionRepository connectionRepository;

    public FacebookInterface(Facebook facebook, ConnectionRepository connectionRepository) {
        this.facebook = SocialStaticData.facebook = facebook;
        this.connectionRepository = SocialStaticData.fb_connectionRepository = connectionRepository;
    }

    @GetMapping
    public String helloFacebook(Model model) throws ParseException {
        if (connectionRepository.findPrimaryConnection(Facebook.class) == null) {
            return "redirect:/connect/facebook";
        }

        User currentFacebookUser = facebook.fetchObject("me", User.class, "id", "about", "age_range", "birthday", "context", "cover", "currency", "devices", "education", "email", "favorite_athletes", "favorite_teams", "first_name", "gender", "hometown", "inspirational_people", "installed", "install_type", "is_verified", "languages", "last_name", "link", "locale", "location", "meeting_for", "middle_name", "name", "name_format", "political", "quotes", "payment_pricepoints", "relationship_status", "religion", "security_settings", "significant_other", "sports", "test_group", "timezone", "third_party_id", "updated_time", "verified", "video_upload_limits", "viewer_can_send_gift", "website", "work");
        PagedList<Post> feed = facebook.feedOperations().getFeed();
        ArrayList<JsonObject> facebookDetail = FacebookController.getInstance().getFacebookDetail(feed);

        boolean success = UserSyncController.getInstance().syncData_facebook(Long.parseLong(currentFacebookUser.getId()), facebookDetail);
        model.addAttribute("successSync", success + "");

        return "FacebookSyncResult";
    }

}