package com.aidocs.aiservice.testutil;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

/**
 * Test Data Factory für AI Service Tests
 * Stellt Testdaten und Mock-Responses bereit
 */
public class TestDataFactory {

    // Test Texte für verschiedene Szenarien
    public static class TestTexts {
        public static final String SHORT_TEXT = "Kurz";
        public static final String EMPTY_TEXT = "";
        public static final String WHITESPACE_TEXT = "   \t\n   ";
        
        public static final String WEB_DEVELOPMENT_TEXT = 
            "Wir entwickeln eine moderne Webanwendung mit React, Vue.js und Angular für das Frontend. " +
            "Das Backend nutzt Spring Boot und Node.js mit REST APIs.";
            
        public static final String DATABASE_TEXT = 
            "Das System benötigt eine robuste Datenbank für die Speicherung. " +
            "PostgreSQL, MongoDB und MySQL sind mögliche Optionen für die Datenpersistierung.";
            
        public static final String DEVOPS_TEXT = 
            "Deployment erfolgt über Docker Container in der Cloud mit Kubernetes. " +
            "AWS und Azure werden als Cloud-Plattformen eingesetzt.";
            
        public static final String MOBILE_TEXT = 
            "Die mobile App wird mit React Native und Flutter entwickelt. " +
            "Cross-Platform Development für iOS und Android.";
            
        public static final String LONG_TEXT = 
            "Dies ist ein sehr langer Text für Performance-Tests. ".repeat(100) +
            "Er enthält verschiedene Technologie-Keywords wie Spring Boot, React, PostgreSQL, " +
            "Docker, Kubernetes, AWS, MongoDB, Angular, Vue.js, Node.js und Express.js. " +
            "Der Text testet die Verarbeitung von umfangreichen Dokumenten mit vielen " +
            "technischen Begriffen und komplexen Strukturen.";
            
        public static final String SPECIAL_CHARS_TEXT = 
            "Text mit Sonderzeichen: äöüß ÄÖÜ @#$%^&*() 你好 🚀 émojis und unicode";
            
        public static final String GERMAN_TECHNICAL_TEXT = 
            "Softwarearchitektur und Systemdesign für eine skalierbare Microservice-Anwendung. " +
            "Die Implementierung verwendet Domain-Driven Design (DDD) Prinzipien mit " +
            "Event-Sourcing und CQRS Patterns. Überwachung durch Prometheus und Grafana.";
    }

    // Mock OpenAI Responses
    public static class MockOpenAIResponses {
        
        public static ResponseEntity<Map> createSuccessResponse(String text) {
            Map<String, Object> response = Map.of(
                "choices", List.of(Map.of("text", text)),
                "usage", Map.of("total_tokens", 150)
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        
        public static ResponseEntity<Map> createSummaryResponse() {
            return createSuccessResponse(
                "Eine moderne Webanwendung mit aktuellen Technologien für optimale User Experience."
            );
        }
        
        public static ResponseEntity<Map> createKeywordsResponse() {
            return createSuccessResponse(
                "Webanwendung, React, Spring Boot, Backend, Frontend, API, Modern"
            );
        }
        
        public static ResponseEntity<Map> createComponentsResponse() {
            return createSuccessResponse(
                "React, Spring Boot, PostgreSQL, Docker, Redis, Nginx"
            );
        }
        
        public static ResponseEntity<Map> createErrorResponse() {
            return new ResponseEntity<>(
                Map.of("error", Map.of("message", "API rate limit exceeded")),
                HttpStatus.TOO_MANY_REQUESTS
            );
        }
        
        public static ResponseEntity<Map> createEmptyResponse() {
            Map<String, Object> response = Map.of(
                "choices", List.of(Map.of("text", "")),
                "usage", Map.of("total_tokens", 0)
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

    // Expected Results für Tests
    public static class ExpectedResults {
        
        public static Map<String, String> createWebDevelopmentResult() {
            return Map.of(
                "summary", "Eine moderne Webanwendung mit aktuellen Technologien",
                "keywords", "webanwendung, react, spring, backend, frontend",
                "suggestedComponents", "React, Angular, Vue.js, Spring Boot, Node.js"
            );
        }
        
        public static Map<String, String> createDatabaseResult() {
            return Map.of(
                "summary", "Robuste Datenbank-Lösung für Speicherung",
                "keywords", "datenbank, speicherung, postgresql, mongodb",
                "suggestedComponents", "PostgreSQL, MongoDB, MySQL"
            );
        }
        
        public static Map<String, String> createFallbackResult(String text) {
            return Map.of(
                "summary", text.length() > 400 ? 
                    text.substring(0, 400) + "... [Lokale Zusammenfassung - OpenAI nicht verfügbar]" :
                    text + " [Lokale Zusammenfassung - OpenAI nicht verfügbar]",
                "keywords", "fallback, keywords, extracted",
                "suggestedComponents", "Spring Boot, React, PostgreSQL, Docker"
            );
        }
    }

    // Cache Test Data
    public static class CacheTestData {
        
        public static final String CACHE_KEY_PREFIX = "ai:analysis:";
        
        public static String generateCacheKey(String text) {
            // Vereinfachte Cache-Key Generation für Tests
            return CACHE_KEY_PREFIX + Math.abs(text.hashCode());
        }
        
        public static Map<String, String> createCachedResponse() {
            return Map.of(
                "summary", "Cached summary response",
                "keywords", "cached, keywords, response", 
                "suggestedComponents", "Cached, Components, List"
            );
        }
    }

    // Request/Response Helper
    public static class RequestHelper {
        
        public static Map<String, String> createAnalyzeRequest(String text) {
            return Map.of("text", text);
        }
        
        public static Map<String, String> createEmptyRequest() {
            return Map.of();
        }
        
        public static Map<String, String> createInvalidRequest() {
            return Map.of("content", "invalid key");
        }
    }

    // Performance Test Data
    public static class PerformanceTestData {
        
        public static String createLargeText(int sizeInKB) {
            String baseText = "Performance test text with technology keywords like Spring Boot, React, PostgreSQL. ";
            int repetitions = (sizeInKB * 1024) / baseText.length();
            return baseText.repeat(Math.max(1, repetitions));
        }
        
        public static String[] createMultipleTexts(int count) {
            String[] texts = new String[count];
            for (int i = 0; i < count; i++) {
                texts[i] = "Performance test text #" + i + " with unique content for cache testing";
            }
            return texts;
        }
    }

    // Validation Helper
    public static class ValidationHelper {
        
        public static boolean isValidAnalysisResult(Map<String, String> result) {
            return result != null &&
                   result.containsKey("summary") &&
                   result.containsKey("keywords") &&
                   result.containsKey("suggestedComponents") &&
                   result.get("summary") != null &&
                   result.get("keywords") != null &&
                   result.get("suggestedComponents") != null;
        }
        
        public static boolean containsFallbackMarker(Map<String, String> result) {
            return result != null &&
                   result.get("summary") != null &&
                   result.get("summary").contains("[Lokale Zusammenfassung - OpenAI nicht verfügbar]");
        }
        
        public static boolean containsWebTechnologies(String components) {
            return components != null &&
                   (components.contains("React") ||
                    components.contains("Angular") ||
                    components.contains("Vue.js") ||
                    components.contains("Spring Boot") ||
                    components.contains("Node.js"));
        }
        
        public static boolean containsDatabaseTechnologies(String components) {
            return components != null &&
                   (components.contains("PostgreSQL") ||
                    components.contains("MongoDB") ||
                    components.contains("MySQL"));
        }
        
        public static boolean containsDevOpsTechnologies(String components) {
            return components != null &&
                   (components.contains("Docker") ||
                    components.contains("Kubernetes") ||
                    components.contains("AWS"));
        }
    }

    // Test Timing Utilities
    public static class TimingHelper {
        
        public static void waitForAsyncOperation(long milliseconds) {
            try {
                Thread.sleep(milliseconds);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        public static long measureExecutionTime(Runnable operation) {
            long startTime = System.currentTimeMillis();
            operation.run();
            return System.currentTimeMillis() - startTime;
        }
    }
}