package com.aidocs.aiservice.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("e2e")
class AiServiceE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379)
            .withReuse(true);

    @Container
    static GenericContainer<?> eurekaServer = new GenericContainer<>("springcloud/eureka:latest")
            .withExposedPorts(8761)
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379).toString());
        registry.add("eureka.client.service-url.defaultZone", 
            () -> "http://" + eurekaServer.getHost() + ":" + eurekaServer.getMappedPort(8761) + "/eureka/");
        
        // Für E2E Tests ohne echte OpenAI API
        registry.add("openai.api.key", () -> "");
    }

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
    }

    @Test
    void e2e_completeAnalysisWorkflow_ShouldWorkEndToEnd() {
        // Given - Echter HTTP Request
        String analysisText = "Vollständige E2E-Test einer modernen Webanwendung mit Spring Boot, React, " +
                             "PostgreSQL für die Datenpersistierung und Docker für das Deployment in der Cloud.";
        
        Map<String, String> request = Map.of("text", analysisText);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

        // When - POST Request zur Analyse
        ResponseEntity<Map> analysisResponse = restTemplate.exchange(
            baseUrl + "/api/ai/analyze",
            HttpMethod.POST,
            entity,
            Map.class
        );

        // Then - Verify Analysis Response
        assertEquals(HttpStatus.OK, analysisResponse.getStatusCode());
        assertNotNull(analysisResponse.getBody());
        
        Map<String, Object> analysisResult = analysisResponse.getBody();
        assertTrue(analysisResult.containsKey("summary"));
        assertTrue(analysisResult.containsKey("keywords"));
        assertTrue(analysisResult.containsKey("suggestedComponents"));
        
        // Verify Fallback wurde verwendet (da kein echter OpenAI Key)
        String summary = (String) analysisResult.get("summary");
        assertTrue(summary.contains("[Lokale Zusammenfassung - OpenAI nicht verfügbar]"));
        
        // Verify Keywords und Components sind generiert
        String keywords = (String) analysisResult.get("keywords");
        String components = (String) analysisResult.get("suggestedComponents");
        assertNotNull(keywords);
        assertNotNull(components);
        assertFalse(keywords.isEmpty());
        assertFalse(components.isEmpty());

        // Verify Web-Technologien wurden erkannt
        assertTrue(components.contains("Spring Boot") || components.contains("React") || components.contains("PostgreSQL"));
    }

    @Test
    void e2e_healthCheckWorkflow_ShouldProvideCompleteHealthInfo() {
        // When - GET Health Check
        ResponseEntity<Map> healthResponse = restTemplate.getForEntity(
            baseUrl + "/api/ai/health",
            Map.class
        );

        // Then - Verify Health Response
        assertEquals(HttpStatus.OK, healthResponse.getStatusCode());
        assertNotNull(healthResponse.getBody());
        
        Map<String, Object> health = healthResponse.getBody();
        assertEquals("UP", health.get("status"));
        assertEquals("ai-service", health.get("service"));
        assertTrue(health.containsKey("openai"));
        assertTrue(health.containsKey("timestamp"));
        
        // Verify OpenAI Configuration
        @SuppressWarnings("unchecked")
        Map<String, Object> openaiInfo = (Map<String, Object>) health.get("openai");
        assertEquals(false, openaiInfo.get("configured")); // Kein API Key in E2E Tests
        assertEquals("gpt-3.5-turbo-instruct", openaiInfo.get("model"));
    }

    @Test
    void e2e_serviceInfoWorkflow_ShouldProvideCompleteServiceInfo() {
        // When - GET Service Info
        ResponseEntity<Map> infoResponse = restTemplate.getForEntity(
            baseUrl + "/api/ai/info",
            Map.class
        );

        // Then - Verify Info Response
        assertEquals(HttpStatus.OK, infoResponse.getStatusCode());
        assertNotNull(infoResponse.getBody());
        
        Map<String, Object> info = infoResponse.getBody();
        assertEquals("AI Analysis Service", info.get("service"));
        assertEquals("1.0.0", info.get("version"));
        assertTrue(info.containsKey("endpoints"));
        assertTrue(info.containsKey("features"));
        
        // Verify Endpoints sind dokumentiert
        @SuppressWarnings("unchecked")
        Map<String, Object> endpoints = (Map<String, Object>) info.get("endpoints");
        assertEquals("POST /api/ai/analyze", endpoints.get("analyze"));
        assertEquals("GET /api/ai/health", endpoints.get("health"));
        assertEquals("GET /api/ai/info", endpoints.get("info"));
    }

    @Test
    void e2e_cachingWorkflow_ShouldUseCacheOnSecondRequest() {
        // Given
        String testText = "E2E Cache Test mit identischem Text für beide Requests";
        Map<String, String> request = Map.of("text", testText);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

        // When - Erste Analyse (Cache Miss)
        long startTime1 = System.currentTimeMillis();
        ResponseEntity<Map> firstResponse = restTemplate.exchange(
            baseUrl + "/api/ai/analyze",
            HttpMethod.POST,
            entity,
            Map.class
        );
        long firstRequestTime = System.currentTimeMillis() - startTime1;

        // When - Zweite Analyse mit identischem Text (Cache Hit)
        long startTime2 = System.currentTimeMillis();
        ResponseEntity<Map> secondResponse = restTemplate.exchange(
            baseUrl + "/api/ai/analyze",
            HttpMethod.POST,
            entity,
            Map.class
        );
        long secondRequestTime = System.currentTimeMillis() - startTime2;

        // Then - Beide Responses sollten identisch sein
        assertEquals(HttpStatus.OK, firstResponse.getStatusCode());
        assertEquals(HttpStatus.OK, secondResponse.getStatusCode());
        
        Map<String, Object> firstResult = firstResponse.getBody();
        Map<String, Object> secondResult = secondResponse.getBody();
        
        assertEquals(firstResult.get("summary"), secondResult.get("summary"));
        assertEquals(firstResult.get("keywords"), secondResult.get("keywords"));
        assertEquals(firstResult.get("suggestedComponents"), secondResult.get("suggestedComponents"));
        
        // Cache sollte zweiten Request deutlich beschleunigen
        assertTrue(secondRequestTime < firstRequestTime,
            "Cache should make second request faster: " + secondRequestTime + "ms vs " + firstRequestTime + "ms");
    }

    @Test
    void e2e_errorHandlingWorkflow_ShouldHandleInvalidRequests() {
        // Test Case 1: Empty Text
        Map<String, String> emptyRequest = Map.of("text", "");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> emptyEntity = new HttpEntity<>(emptyRequest, headers);

        ResponseEntity<Map> emptyResponse = restTemplate.exchange(
            baseUrl + "/api/ai/analyze",
            HttpMethod.POST,
            emptyEntity,
            Map.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, emptyResponse.getStatusCode());
        Map<String, Object> errorBody = emptyResponse.getBody();
        assertNotNull(errorBody);
        assertEquals("Text is required", errorBody.get("error"));

        // Test Case 2: Missing Content-Type
        ResponseEntity<String> noContentTypeResponse = restTemplate.exchange(
            baseUrl + "/api/ai/analyze",
            HttpMethod.POST,
            new HttpEntity<>("{\"text\":\"test\"}"),
            String.class
        );

        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, noContentTypeResponse.getStatusCode());

        // Test Case 3: Wrong HTTP Method
        ResponseEntity<String> wrongMethodResponse = restTemplate.exchange(
            baseUrl + "/api/ai/analyze",
            HttpMethod.GET,
            null,
            String.class
        );

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, wrongMethodResponse.getStatusCode());
    }

    @Test
    void e2e_differentTextTypes_ShouldHandleVariousInputs() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Test Case 1: Web Development Text
        Map<String, String> webRequest = Map.of("text", 
            "Frontend mit React und Vue.js, Backend mit Spring Boot und Node.js APIs");
        HttpEntity<Map<String, String>> webEntity = new HttpEntity<>(webRequest, headers);
        
        ResponseEntity<Map> webResponse = restTemplate.exchange(
            baseUrl + "/api/ai/analyze", HttpMethod.POST, webEntity, Map.class);
        
        assertEquals(HttpStatus.OK, webResponse.getStatusCode());
        String webComponents = (String) webResponse.getBody().get("suggestedComponents");
        assertTrue(webComponents.contains("React") || webComponents.contains("Spring Boot"));

        // Test Case 2: Database Text
        Map<String, String> dbRequest = Map.of("text", 
            "Datenbank-Design mit PostgreSQL, MongoDB für NoSQL und MySQL für relationale Daten");
        HttpEntity<Map<String, String>> dbEntity = new HttpEntity<>(dbRequest, headers);
        
        ResponseEntity<Map> dbResponse = restTemplate.exchange(
            baseUrl + "/api/ai/analyze", HttpMethod.POST, dbEntity, Map.class);
        
        assertEquals(HttpStatus.OK, dbResponse.getStatusCode());
        String dbComponents = (String) dbResponse.getBody().get("suggestedComponents");
        assertTrue(dbComponents.contains("PostgreSQL") || dbComponents.contains("MongoDB"));

        // Test Case 3: Special Characters
        Map<String, String> specialRequest = Map.of("text", 
            "Entwicklung mit Umlauten: äöüß und Emojis: 🚀 für moderne Apps");
        HttpEntity<Map<String, String>> specialEntity = new HttpEntity<>(specialRequest, headers);
        
        ResponseEntity<Map> specialResponse = restTemplate.exchange(
            baseUrl + "/api/ai/analyze", HttpMethod.POST, specialEntity, Map.class);
        
        assertEquals(HttpStatus.OK, specialResponse.getStatusCode());
        assertNotNull(specialResponse.getBody().get("summary"));
    }

    @Test
    void e2e_concurrentRequests_ShouldHandleLoadCorrectly() throws InterruptedException {
        // Given
        int numberOfThreads = 10;
        int requestsPerThread = 3;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // When - Concurrent Requests
        CompletableFuture<Void>[] futures = IntStream.range(0, numberOfThreads)
            .mapToObj(threadIndex -> CompletableFuture.runAsync(() -> {
                for (int i = 0; i < requestsPerThread; i++) {
                    String uniqueText = "Concurrent E2E Test Thread-" + threadIndex + " Request-" + i + 
                                       " mit Spring Boot und React Technologien";
                    
                    Map<String, String> request = Map.of("text", uniqueText);
                    HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);
                    
                    ResponseEntity<Map> response = restTemplate.exchange(
                        baseUrl + "/api/ai/analyze",
                        HttpMethod.POST,
                        entity,
                        Map.class
                    );
                    
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertTrue(response.getBody().containsKey("summary"));
                }
            }, executor))
            .toArray(CompletableFuture[]::new);

        // Wait for all requests to complete
        CompletableFuture.allOf(futures).join();
        
        executor.shutdown();
        assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));

        // Then - Verify Service is still responsive
        ResponseEntity<Map> healthResponse = restTemplate.getForEntity(
            baseUrl + "/api/ai/health", Map.class);
        assertEquals(HttpStatus.OK, healthResponse.getStatusCode());
        assertEquals("UP", healthResponse.getBody().get("status"));
    }

    @Test
    void e2e_serviceDiscovery_ShouldRegisterWithEureka() {
        // Wait for service registration
        await().atMost(Duration.ofSeconds(30)).until(() -> {
            try {
                ResponseEntity<String> eurekaResponse = restTemplate.getForEntity(
                    "http://" + eurekaServer.getHost() + ":" + eurekaServer.getMappedPort(8761) + "/eureka/apps",
                    String.class
                );
                return eurekaResponse.getStatusCode() == HttpStatus.OK && 
                       eurekaResponse.getBody().contains("AI-SERVICE");
            } catch (Exception e) {
                return false;
            }
        });

        // Verify service is registered
        ResponseEntity<String> eurekaResponse = restTemplate.getForEntity(
            "http://" + eurekaServer.getHost() + ":" + eurekaServer.getMappedPort(8761) + "/eureka/apps",
            String.class
        );
        
        assertEquals(HttpStatus.OK, eurekaResponse.getStatusCode());
        assertTrue(eurekaResponse.getBody().contains("AI-SERVICE"));
    }

    @Test
    void e2e_actuatorEndpoints_ShouldProvideMonitoringData() {
        // Test Actuator Health
        ResponseEntity<Map> actuatorHealth = restTemplate.getForEntity(
            baseUrl + "/actuator/health", Map.class);
        assertEquals(HttpStatus.OK, actuatorHealth.getStatusCode());
        assertEquals("UP", actuatorHealth.getBody().get("status"));

        // Test Actuator Info
        ResponseEntity<Map> actuatorInfo = restTemplate.getForEntity(
            baseUrl + "/actuator/info", Map.class);
        assertEquals(HttpStatus.OK, actuatorInfo.getStatusCode());

        // Test Actuator Metrics
        ResponseEntity<Map> actuatorMetrics = restTemplate.getForEntity(
            baseUrl + "/actuator/metrics", Map.class);
        assertEquals(HttpStatus.OK, actuatorMetrics.getStatusCode());
        assertTrue(actuatorMetrics.getBody().containsKey("names"));

        // Test Prometheus Metrics
        ResponseEntity<String> prometheusMetrics = restTemplate.getForEntity(
            baseUrl + "/actuator/prometheus", String.class);
        assertEquals(HttpStatus.OK, prometheusMetrics.getStatusCode());
        assertTrue(prometheusMetrics.getBody().contains("jvm_"));
    }

    @Test
    void e2e_fullWorkflowScenario_ShouldSimulateRealUsage() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Scenario: Developer analysiert verschiedene Projektbeschreibungen
        String[] projectDescriptions = {
            "E-Commerce Platform mit Spring Boot Backend, React Frontend und PostgreSQL Database",
            "Mobile App Entwicklung mit React Native für iOS und Android mit Firebase Backend",
            "Microservices Architektur mit Docker, Kubernetes und MongoDB für Datenmanagement",
            "Machine Learning Pipeline mit Python, TensorFlow und Apache Kafka für Datenstreaming"
        };

        for (int i = 0; i < projectDescriptions.length; i++) {
            // Step 1: Health Check vor jeder Analyse
            ResponseEntity<Map> healthCheck = restTemplate.getForEntity(
                baseUrl + "/api/ai/health", Map.class);
            assertEquals(HttpStatus.OK, healthCheck.getStatusCode());
            assertEquals("UP", healthCheck.getBody().get("status"));

            // Step 2: Projekt analysieren
            Map<String, String> request = Map.of("text", projectDescriptions[i]);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map> analysisResponse = restTemplate.exchange(
                baseUrl + "/api/ai/analyze", HttpMethod.POST, entity, Map.class);
            
            assertEquals(HttpStatus.OK, analysisResponse.getStatusCode());
            Map<String, Object> result = analysisResponse.getBody();
            
            // Step 3: Verify Analysis Quality
            assertNotNull(result.get("summary"));
            assertNotNull(result.get("keywords"));
            assertNotNull(result.get("suggestedComponents"));
            
            String summary = (String) result.get("summary");
            String keywords = (String) result.get("keywords");
            String components = (String) result.get("suggestedComponents");
            
            assertFalse(summary.isEmpty());
            assertFalse(keywords.isEmpty());
            assertFalse(components.isEmpty());
            
            // Step 4: Verify Technology Detection
            String description = projectDescriptions[i].toLowerCase();
            String componentsLower = components.toLowerCase();
            
            if (description.contains("spring boot")) {
                assertTrue(componentsLower.contains("spring boot"));
            }
            if (description.contains("react")) {
                assertTrue(componentsLower.contains("react"));
            }
            if (description.contains("postgresql")) {
                assertTrue(componentsLower.contains("postgresql"));
            }

            // Kleine Pause zwischen Requests
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Final Health Check
        ResponseEntity<Map> finalHealthCheck = restTemplate.getForEntity(
            baseUrl + "/api/ai/health", Map.class);
        assertEquals(HttpStatus.OK, finalHealthCheck.getStatusCode());
        assertEquals("UP", finalHealthCheck.getBody().get("status"));
    }

    @Test
    void e2e_serviceResilience_ShouldHandleEdgeCases() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Edge Case 1: Very Short Text
        Map<String, String> shortRequest = Map.of("text", "AI");
        HttpEntity<Map<String, String>> shortEntity = new HttpEntity<>(shortRequest, headers);
        ResponseEntity<Map> shortResponse = restTemplate.exchange(
            baseUrl + "/api/ai/analyze", HttpMethod.POST, shortEntity, Map.class);
        assertEquals(HttpStatus.OK, shortResponse.getStatusCode());

        // Edge Case 2: Very Long Text
        String longText = "Sehr langer Projekttext. ".repeat(1000);
        Map<String, String> longRequest = Map.of("text", longText);
        HttpEntity<Map<String, String>> longEntity = new HttpEntity<>(longRequest, headers);
        ResponseEntity<Map> longResponse = restTemplate.exchange(
            baseUrl + "/api/ai/analyze", HttpMethod.POST, longEntity, Map.class);
        assertEquals(HttpStatus.OK, longResponse.getStatusCode());

        // Edge Case 3: Only Numbers
        Map<String, String> numbersRequest = Map.of("text", "123456789 987654321");
        HttpEntity<Map<String, String>> numbersEntity = new HttpEntity<>(numbersRequest, headers);
        ResponseEntity<Map> numbersResponse = restTemplate.exchange(
            baseUrl + "/api/ai/analyze", HttpMethod.POST, numbersEntity, Map.class);
        assertEquals(HttpStatus.OK, numbersResponse.getStatusCode());

        // Edge Case 4: Mixed Languages
        Map<String, String> mixedRequest = Map.of("text", 
            "Deutsch: Spring Boot, English: React Native, Français: Vue.js, 中文: MongoDB");
        HttpEntity<Map<String, String>> mixedEntity = new HttpEntity<>(mixedRequest, headers);
        ResponseEntity<Map> mixedResponse = restTemplate.exchange(
            baseUrl + "/api/ai/analyze", HttpMethod.POST, mixedEntity, Map.class);
        assertEquals(HttpStatus.OK, mixedResponse.getStatusCode());

        // Service sollte nach allen Edge Cases noch funktionsfähig sein
        ResponseEntity<Map> healthResponse = restTemplate.getForEntity(
            baseUrl + "/api/ai/health", Map.class);
        assertEquals(HttpStatus.OK, healthResponse.getStatusCode());
        assertEquals("UP", healthResponse.getBody().get("status"));
    }
}