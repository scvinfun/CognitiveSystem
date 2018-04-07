package CognitiveServices;

import java.util.Date;

public class DetectionRecord {
    private String uid;
    private String origin_text;
    private String from;
    private String postCreatedAt;
    private String keyPhrase;
    private String diagnosticRule;
    private boolean useSemanticSimilarity;
    private String createdAt;
    private double srScore;
    private String srPair;

    public DetectionRecord(String uid, String origin_text, String postCreatedAt, String keyPhrase, String diagnosticRule, boolean useSemanticSimilarity) {
        this.uid = uid;
        this.origin_text = origin_text;
        this.postCreatedAt = postCreatedAt;
        this.keyPhrase = keyPhrase;
        this.diagnosticRule = diagnosticRule;
        this.useSemanticSimilarity = useSemanticSimilarity;
        this.createdAt = new Date().toString();
    }

    public DetectionRecord(String uid, String origin_text, String postCreatedAt, String keyPhrase, String diagnosticRule, boolean useSemanticSimilarity, double srScore, String srPair) {
        this.uid = uid;
        this.origin_text = origin_text;
        this.postCreatedAt = postCreatedAt;
        this.keyPhrase = keyPhrase;
        this.diagnosticRule = diagnosticRule;
        this.useSemanticSimilarity = useSemanticSimilarity;
        this.createdAt = new Date().toString();
        this.srScore = srScore;
        this.srPair = srPair;
    }

    private DetectionRecord() {
        this.uid = "";
        this.origin_text = "THIS IS DUMMY TEXT.";
        this.from = "";
        this.postCreatedAt = "";
        this.keyPhrase = "";
        this.diagnosticRule = "";
        this.useSemanticSimilarity = false;
        this.createdAt = "";
    }

    public static DetectionRecord getDummyDetectRecord() {
        return new DetectionRecord();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getOrigin_text() {
        return origin_text;
    }

    public void setOrigin_text(String origin_text) {
        this.origin_text = origin_text;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getPostCreatedAt() {
        return postCreatedAt;
    }

    public void setPostCreatedAt(String postCreatedAt) {
        this.postCreatedAt = postCreatedAt;
    }

    public String getKeyPhrase() {
        return keyPhrase;
    }

    public void setKeyPhrase(String keyPhrase) {
        this.keyPhrase = keyPhrase;
    }

    public String getDiagnosticRule() {
        return diagnosticRule;
    }

    public void setDiagnosticRule(String diagnosticRule) {
        this.diagnosticRule = diagnosticRule;
    }

    public boolean isUseSemanticSimilarity() {
        return useSemanticSimilarity;
    }

    public void setUseSemanticSimilarity(boolean useSemanticSimilarity) {
        this.useSemanticSimilarity = useSemanticSimilarity;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public double getSrScore() {
        return srScore;
    }

    public void setSrScore(double srScore) {
        this.srScore = srScore;
    }

    public String getSrPair() {
        return srPair;
    }

    public void setSrPair(String srPair) {
        this.srPair = srPair;
    }
}
