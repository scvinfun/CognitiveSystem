package CognitiveServices;

public class StaticData {
    private static final String TEXTANALYTICS_ACCESSKEY = "a644830fb6ab4579b97bbb885986f32d";
    private static final String TEXTANALYTICS_HOST = "https://eastasia.api.cognitive.microsoft.com/text/analytics/v2.0/";
    public static final String TEXTANALYTICS_TYPE_LANGUAGES = "languages";
    public static final String TEXTANALYTICS_TYPE_KEYPHRASES = "keyPhrases";
    public static final String TEXTANALYTICS_TYPE_SENTIMENT = "sentiment";

    private static final String COMPUTERVISION_SUBSCRIPTIONKEY = "c7f024fe92584817ab86ba607ce81c32";
    public static final String COMPUTERVISION_URLBASE = "https://eastasia.api.cognitive.microsoft.com/vision/v1.0/analyze";

    public static String getTextAnalytics_url(String serviceName) {
        if (serviceName.equals(TEXTANALYTICS_TYPE_LANGUAGES))
            return TEXTANALYTICS_HOST + TEXTANALYTICS_TYPE_LANGUAGES;
        else if (serviceName.equals(TEXTANALYTICS_TYPE_KEYPHRASES))
            return TEXTANALYTICS_HOST + TEXTANALYTICS_TYPE_KEYPHRASES;
        else if (serviceName.equals(TEXTANALYTICS_TYPE_SENTIMENT))
            return TEXTANALYTICS_HOST + TEXTANALYTICS_TYPE_SENTIMENT;
        else
            return "Error:No such service";
    }

    public static String getTextAnalyics_AccessKey() {
        return TEXTANALYTICS_ACCESSKEY;
    }

    public static String getComputerVision_SubscriptionKey() {
        return COMPUTERVISION_SUBSCRIPTIONKEY;
    }
}
