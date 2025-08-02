package com.aidocs.aiservice.test.builders;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.ResultMatcher;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Builder für AI Analysis Request Objects
 */
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
    
    public AnalysisRequestBuilder withMobileAppText() {
        this.text = "Mobile App Entwicklung mit React Native für iOS und Android, Firebase Backend und Push Notifications";
        return this;
    }
    
    public AnalysisRequestBuilder withDatabaseText() {
        this.text = "Datenbank-Design mit PostgreSQL für relationale Daten, MongoDB für NoSQL und Redis für Caching";
        return this;
    }
    
    public AnalysisRequestBuilder withDevOpsText() {
        this.text = "DevOps Pipeline mit Docker, Kubernetes, Jenkins, AWS Cloud und Terraform Infrastructure as Code";
        return this;
    }
    
    public AnalysisRequestBuilder withMachineLearningText() {
        this.text = "Machine Learning Pipeline mit Python, TensorFlow, Apache Spark für Big Data und MLflow für Model Management";
        return this;
    }
    
    public AnalysisRequestBuilder withSpecialCharacters() {
        this.text = "Text mit Umlauten: äöüß, Sonderzeichen: @#$%^&*() und Emojis: 🚀 für Test-Zwecke";
        return this;
    }
    
    public AnalysisRequestBuilder withLongText(int repetitions) {
        this.text = "Sehr langer Text für Performance-Tests mit verschiedenen Technologien. ".repeat(repetitions);
        return this;
    }
    
    public AnalysisRequestBuilder withShortText() {
        this.text = "Kurz";
        return this;
    }
    
    public AnalysisRequestBuilder withEmptyText() {
        this.text = "";
        return this;
    }
    
    public AnalysisRequestBuilder withWhitespaceText() {
        this.text = "   \t\n   ";
        return this;
    }
    
    public AnalysisRequestBuilder withRandomText() {
        this.text = generateRandomText();
        return this;
    }
    
    public AnalysisRequestBuilder withAdditionalField(String key, Object value) {
        this.additionalFields.put(key, value);
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
        additionalFields.forEach((k, v) -> request.put(k, v.toString()));
        return request;
    }
    
    private String generateRandomText() {
        String[] technologies = {"Spring Boot", "React", "Angular", "Vue.js", "Node.js", "PostgreSQL", 
                               "MongoDB", "Redis", "Docker", "Kubernetes", "AWS", "Azure"};
        String[] contexts = {"entwickeln", "implementieren", "deployen", "testen", "optimieren"};
        String[] projects = {"Webanwendung", "Mobile App", "Microservice", "API", "Dashboard"};
        
        Random random = ThreadLocalRandom.current();
        String tech = technologies[random.nextInt(technologies.length)];
        String context = contexts[random.nextInt(contexts.length)];
        String project = projects[random.nextInt(projects.length)];
        
        return String.format("Wir %s eine %s mit %s für bessere Performance und Skalierbarkeit.", 
                           context, project, tech);
    }
}

/**
 * Builder für AI Analysis Response Objects
 */
public class AnalysisResponseBuilder {
    private String summary = "Default test summary";
    private String keywords = "default, test, keywords";
    private String suggestedComponents = "Spring Boot, React, PostgreSQL";
    private Map<String, Object> additionalFields = new HashMap<>();
    
    public static AnalysisResponseBuilder aResponse() {
        return new AnalysisResponseBuilder();
    }
    
    public AnalysisResponseBuilder withSummary(String summary) {
        this.summary = summary;
        return this;
    }
    
    public AnalysisResponseBuilder withKeywords(String keywords) {
        this.keywords = keywords;
        return this;
    }
    
    public AnalysisResponseBuilder withSuggestedComponents(String components) {
        this.suggestedComponents = components;
        return this;
    }
    
    public AnalysisResponseBuilder withWebTechnologies() {
        this.summary = "Moderne Webanwendung mit aktuellen Frontend- und Backend-Technologien";
        this.keywords = "Webanwendung, Frontend, Backend, Modern, Technologie";
        this.suggestedComponents = "React, Angular, Spring Boot, Node.js, PostgreSQL";
        return this;
    }
    
    public AnalysisResponseBuilder withMobileTechnologies() {
        this.summary = "Mobile App-Entwicklung mit Cross-Platform-Technologien";
        this.keywords = "Mobile, App, Cross-Platform, iOS, Android";
        this.suggestedComponents = "React Native, Flutter, Firebase, Expo";
        return this;
    }
    
    public AnalysisResponseBuilder withFallbackMarkers() {
        this.summary = "Text analysis summary [Lokale Zusammenfassung - OpenAI nicht verfügbar]";
        return this;
    }
    
    public AnalysisResponseBuilder withRandomContent() {
        this.summary = generateRandomSummary();
        this.keywords = generateRandomKeywords();
        this.suggestedComponents = generateRandomComponents();
        return this;
    }
    
    public AnalysisResponseBuilder withAdditionalField(String key, Object value) {
        this.additionalFields.put(key, value);
        return this;
    }
    
    public Map<String, Object> build() {
        Map<String, Object> response = new HashMap<>();
        response.put("summary", summary);
        response.put("keywords", keywords);
        response.put("suggestedComponents", suggestedComponents);
        response.putAll(additionalFields);
        return response;
    }
    
    public Map<String, String> buildStringMap() {
        Map<String, String> response = new HashMap<>();
        response.put("summary", summary);
        response.put("keywords", keywords);
        response.put("suggestedComponents", suggestedComponents);
        additionalFields.forEach((k, v) -> response.put(k, v.toString()));
        return response;
    }
    
    private String generateRandomSummary() {
        String[] templates = {
            "Innovative Lösung für moderne Softwareentwicklung",
            "Skalierbare Architektur mit aktuellen Technologien",
            "Effiziente Implementierung für bessere Performance",
            "Robuste Anwendung mit modernen Frameworks"
        };
        return templates[ThreadLocalRandom.current().nextInt(templates.length)];
    }
    
    private String generateRandomKeywords() {
        List<String> keywords = Arrays.asList("modern", "skalierbar", "effizient", "robust", 
                                            "technologie", "framework", "architektur", "performance");
        Collections.shuffle(keywords);
        return String.join(", ", keywords.subList(0, 4));
    }
    
    private String generateRandomComponents() {
        List<String> components = Arrays.asList("Spring Boot", "React", "Angular", "Vue.js", 
                                              "Node.js", "PostgreSQL", "MongoDB", "Redis", "Docker");
        Collections.shuffle(components);
        return String.join(", ", components.subList(0, 3));
    }
}

/**
 * Builder für OpenAI Mock Responses
 */
public class OpenAIMockResponseBuilder {
    private List<Map<String, Object>> choices = new ArrayList<>();
    private Map<String, Object> usage = Map.of("total_tokens", 150);
    private String model = "gpt-3.5-turbo-instruct";
    private HttpStatus status = HttpStatus.OK;
    
    public static OpenAIMockResponseBuilder anOpenAIResponse() {
        return new OpenAIMockResponseBuilder();
    }
    
    public OpenAIMockResponseBuilder withChoice(String text) {
        this.choices.add(Map.of("text", text));
        return this;
    }
    
    public OpenAIMockResponseBuilder withSuccessfulSummary() {
        return withChoice("Professionelle Zusammenfassung der Projektbeschreibung mit modernen Technologien.");
    }
    
    public OpenAIMockResponseBuilder withSuccessfulKeywords() {
        return withChoice("Projekt, Modern, Technologie, Entwicklung, Software");
    }
    
    public OpenAIMockResponseBuilder withSuccessfulComponents() {
        return withChoice("Spring Boot, React, PostgreSQL, Docker, Kubernetes");
    }
    
    public OpenAIMockResponseBuilder withEmptyChoice() {
        this.choices.add(Map.of("text", ""));
        return this;
    }
    
    public OpenAIMockResponseBuilder withNoChoices() {
        this.choices.clear();
        return this;
    }
    
    public OpenAIMockResponseBuilder withUsage(int totalTokens) {
        this.usage = Map.of("total_tokens", totalTokens);
        return this;
    }
    
    public OpenAIMockResponseBuilder withModel(String model) {
        this.model = model;
        return this;
    }
    
    public OpenAIMockResponseBuilder withStatus(HttpStatus status) {
        this.status = status;
        return this;
    }
    
    public OpenAIMockResponseBuilder withRateLimitError() {
        this.status = HttpStatus.TOO_MANY_REQUESTS;
        return this;
    }
    
    public OpenAIMockResponseBuilder withAuthenticationError() {
        this.status = HttpStatus.UNAUTHORIZED;
        return this;
    }
    
    public OpenAIMockResponseBuilder withServerError() {
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        return this;
    }
    
    public ResponseEntity<Map> build() {
        Map<String, Object> body = new HashMap<>();
        body.put("choices", choices);
        body.put("usage", usage);
        body.put("model", model);
        
        return new ResponseEntity<>(body, status);
    }
    
    public Map<String, Object> buildBody() {
        Map<String, Object> body = new HashMap<>();
        body.put("choices", choices);
        body.put("usage", usage);
        body.put("model", model);
        return body;
    }
}

/**
 * Builder für HTTP Headers
 */
public class HttpHeadersBuilder {
    private HttpHeaders headers = new HttpHeaders();
    
    public static HttpHeadersBuilder headers() {
        return new HttpHeadersBuilder();
    }
    
    public HttpHeadersBuilder json() {
        headers.setContentType(MediaType.APPLICATION_JSON);
        return this;
    }
    
    public HttpHeadersBuilder xml() {
        headers.setContentType(MediaType.APPLICATION_XML);
        return this;
    }
    
    public HttpHeadersBuilder formData() {
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return this;
    }
    
    public HttpHeadersBuilder plainText() {
        headers.setContentType(MediaType.TEXT_PLAIN);
        return this;
    }
    
    public HttpHeadersBuilder withBearer(String token) {
        headers.setBearerAuth(token);
        return this;
    }
    
    public HttpHeadersBuilder withBasicAuth(String username, String password) {
        headers.setBasicAuth(username, password);
        return this;
    }
    
    public HttpHeadersBuilder withCustomHeader(String name, String value) {
        headers.add(name, value);
        return this;
    }
    
    public HttpHeadersBuilder withUserAgent(String userAgent) {
        headers.add("User-Agent", userAgent);
        return this;
    }
    
    public HttpHeadersBuilder withCorrelationId(String correlationId) {
        headers.add("X-Correlation-ID", correlationId);
        return this;
    }
    
    public HttpHeaders build() {
        return headers;
    }
}

/**
 * Builder für Health Check Responses
 */
public class HealthResponseBuilder {
    private String status = "UP";
    private String service = "ai-service";
    private Map<String, Object> openai = Map.of("configured", true, "model", "gpt-3.5-turbo-instruct");
    private long timestamp = System.currentTimeMillis();
    private Map<String, Object> additionalFields = new HashMap<>();
    
    public static HealthResponseBuilder aHealthResponse() {
        return new HealthResponseBuilder();
    }
    
    public HealthResponseBuilder up() {
        this.status = "UP";
        return this;
    }
    
    public HealthResponseBuilder down() {
        this.status = "DOWN";
        return this;
    }
    
    public HealthResponseBuilder withService(String service) {
        this.service = service;
        return this;
    }
    
    public HealthResponseBuilder withOpenAIConfigured(boolean configured) {
        this.openai = Map.of("configured", configured, "model", "gpt-3.5-turbo-instruct");
        return this;
    }
    
    public HealthResponseBuilder withTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }
    
    public HealthResponseBuilder withAdditionalField(String key, Object value) {
        this.additionalFields.put(key, value);
        return this;
    }
    
    public Map<String, Object> build() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", status);
        response.put("service", service);
        response.put("openai", openai);
        response.put("timestamp", timestamp);
        response.putAll(additionalFields);
        return response;
    }
}

/**
 * Builder für Performance Test Data
 */
public class PerformanceTestDataBuilder {
    private int numberOfUsers = 10;
    private int requestsPerUser = 5;
    private int durationSeconds = 60;
    private String textTemplate = "Performance test text with technologies";
    
    public static PerformanceTestDataBuilder performanceTestData() {
        return new PerformanceTestDataBuilder();
    }
    
    public PerformanceTestDataBuilder withUsers(int numberOfUsers) {
        this.numberOfUsers = numberOfUsers;
        return this;
    }
    
    public PerformanceTestDataBuilder withRequestsPerUser(int requestsPerUser) {
        this.requestsPerUser = requestsPerUser;
        return this;
    }
    
    public PerformanceTestDataBuilder withDuration(int durationSeconds) {
        this.durationSeconds = durationSeconds;
        return this;
    }
    
    public PerformanceTestDataBuilder withTextTemplate(String textTemplate) {
        this.textTemplate = textTemplate;
        return this;
    }
    
    public PerformanceTestDataBuilder forLoadTest() {
        this.numberOfUsers = 50;
        this.requestsPerUser = 10;
        this.durationSeconds = 120;
        return this;
    }
    
    public PerformanceTestDataBuilder forStressTest() {
        this.numberOfUsers = 100;
        this.requestsPerUser = 20;
        this.durationSeconds = 300;
        return this;
    }
    
    public PerformanceTestDataBuilder forSpikeTest() {
        this.numberOfUsers = 200;
        this.requestsPerUser = 5;
        this.durationSeconds = 60;
        return this;
    }
    
    public int getTotalRequests() {
        return numberOfUsers * requestsPerUser;
    }
    
    public String generateText(int userId, int requestId) {
        return textTemplate + " User-" + userId + " Request-" + requestId + " " + 
               LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME);
    }
    
    public int getNumberOfUsers() { return numberOfUsers; }
    public int getRequestsPerUser() { return requestsPerUser; }
    public int getDurationSeconds() { return durationSeconds; }
}

/**
 * Builder für Result Matchers (MockMvc)
 */
public class ResultMatcherBuilder {
    private List<ResultMatcher> matchers = new ArrayList<>();
    
    public static ResultMatcherBuilder expect() {
        return new ResultMatcherBuilder();
    }
    
    public ResultMatcherBuilder status(HttpStatus status) {
        switch (status) {
            case OK -> matchers.add(status().isOk());
            case BAD_REQUEST -> matchers.add(status().isBadRequest());
            case UNAUTHORIZED -> matchers.add(status().isUnauthorized());
            case FORBIDDEN -> matchers.add(status().isForbidden());
            case NOT_FOUND -> matchers.add(status().isNotFound());
            case METHOD_NOT_ALLOWED -> matchers.add(status().isMethodNotAllowed());
            case UNSUPPORTED_MEDIA_TYPE -> matchers.add(status().isUnsupportedMediaType());
            case INTERNAL_SERVER_ERROR -> matchers.add(status().isInternalServerError());
            default -> matchers.add(status().is(status.value()));
        }
        return this;
    }
    
    public ResultMatcherBuilder jsonContent() {
        matchers.add(content().contentType(MediaType.APPLICATION_JSON));
        return this;
    }
    
    public ResultMatcherBuilder validAnalysisResponse() {
        matchers.add(jsonPath("$.summary").exists());
        matchers.add(jsonPath("$.keywords").exists());
        matchers.add(jsonPath("$.suggestedComponents").exists());
        matchers.add(jsonPath("$.summary").isString());
        matchers.add(jsonPath("$.keywords").isString());
        matchers.add(jsonPath("$.suggestedComponents").isString());
        return this;
    }
    
    public ResultMatcherBuilder errorResponse(String errorMessage) {
        matchers.add(jsonPath("$.error").value(errorMessage));
        return this;
    }
    
    public ResultMatcherBuilder healthResponse() {
        matchers.add(jsonPath("$.status").value("UP"));
        matchers.add(jsonPath("$.service").value("ai-service"));
        matchers.add(jsonPath("$.openai").exists());
        matchers.add(jsonPath("$.timestamp").exists());
        return this;
    }
    
    public ResultMatcherBuilder infoResponse() {
        matchers.add(jsonPath("$.service").value("AI Analysis Service"));
        matchers.add(jsonPath("$.version").value("1.0.0"));
        matchers.add(jsonPath("$.endpoints").exists());
        matchers.add(jsonPath("$.features").exists());
        return this;
    }
    
    public ResultMatcherBuilder noSecurityHeaders() {
        matchers.add(header().doesNotExist("X-Powered-By"));
        matchers.add(header().doesNotExist("Server"));
        return this;
    }
    
    public ResultMatcher[] build() {
        return matchers.toArray(new ResultMatcher[0]);
    }
}

/**
 * Factory für Test Scenarios
 */
public class TestScenarioFactory {
    
    public static Map<String, Object> happyPathScenario() {
        return Map.of(
            "request", AnalysisRequestBuilder.aRequest().withWebDevelopmentText().buildStringMap(),
            "expectedResponse", AnalysisResponseBuilder.aResponse().withWebTechnologies().buildStringMap(),
            "description", "Happy path scenario with web development text"
        );
    }
    
    public static Map<String, Object> errorScenario() {
        return Map.of(
            "request", AnalysisRequestBuilder.aRequest().withEmptyText().buildStringMap(),
            "expectedError", "Text is required",
            "description", "Error scenario with empty text"
        );
    }
    
    public static Map<String, Object> fallbackScenario() {
        return Map.of(
            "request", AnalysisRequestBuilder.aRequest().withDatabaseText().buildStringMap(),
            "expectedResponse", AnalysisResponseBuilder.aResponse().withFallbackMarkers().buildStringMap(),
            "description", "Fallback scenario when OpenAI is not available"
        );
    }
    
    public static Map<String, Object> performanceScenario() {
        return Map.of(
            "request", AnalysisRequestBuilder.aRequest().withLongText(100).buildStringMap(),
            "expectedResponse", AnalysisResponseBuilder.aResponse().withRandomContent().buildStringMap(),
            "description", "Performance scenario with large text input"
        );
    }
    
    public static List<Map<String, Object>> allScenarios() {
        return Arrays.asList(
            happyPathScenario(),
            errorScenario(),
            fallbackScenario(),
            performanceScenario()
        );
    }
}