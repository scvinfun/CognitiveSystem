package CognitiveServices;

import java.util.Date;

public class DetectionRecord {
    private String uid;
    private String origin_text;
    private String from;
    private String postCreatedAt;
    private String keyPhrase;
    private String diagnosticRule;
    private String createdAt;

    public DetectionRecord(String uid, String origin_text, String from, String postCreatedAt, String keyPhrase, String diagnosticRule) {
        this.uid = uid;
        this.origin_text = origin_text;
        this.from = from;
        this.postCreatedAt = postCreatedAt;
        this.keyPhrase = keyPhrase;
        this.diagnosticRule = diagnosticRule;
        this.createdAt = new Date().toString();
    }

    public String getUid() {
        return uid;
    }

    public String getOrigin_text() {
        return origin_text;
    }

    public String getFrom() {
        return from;
    }

    public String getPostCreatedAt() {
        return postCreatedAt;
    }

    public String getKeyPhrase() {
        return keyPhrase;
    }

    public String getDiagnosticRule() {
        return diagnosticRule;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
