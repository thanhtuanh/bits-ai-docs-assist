package com.aidocs.aiservice.builders;

import java.util.*;

public class AnalysisRequestBuilder {
    private String text = "Default test text";
    private Map<String, Object> additionalFields = new HashMap<>();
    
    public static AnalysisRequestBuilder aRequest() {
        return new AnalysisRequestBuilder();
    }
    
    public AnalysisRequestBuilder withText(String text) {
        this.text = text;
        return this;
    }
    
    public AnalysisRequestBuilder withWebDevelopmentText() {
        this.text = "Moderne Webanwendung mit React Frontend, Spring Boot Backend, PostgreSQL Datenbank und Docker Deployment";
        return this;
    }
    
    public AnalysisRequestBuilder withEmptyText() {
        this.text = "";
        return this;
    }
    
    public Map<String, Object> build() {
        Map<String, Object> request = new HashMap<>();
        request.put("text", text);
        request.putAll(additionalFields);
        return request;
    }
    
    public Map<String, String> buildStringMap() {
        Map<String, String> request = new HashMap<>();
        request.put("text", text);
        return request;
    }
}
