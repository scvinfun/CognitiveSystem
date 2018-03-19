package InformationExtractor;

import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.twitter.api.Twitter;

public class SocialStaticData {
    public static Facebook facebook;
    public static ConnectionRepository fb_connectionRepository;

    public static Twitter twitter;
    public static ConnectionRepository tw_connectionRepository;
}
