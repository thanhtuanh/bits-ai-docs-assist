package com.aidocs.aiservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private final RestTemplate restTemplate;
    private final StringRedisTemplate redisTemplate;

    @Value("${openai.api.key:}")
    private String openAiApiKey;

    @Value("${openai.model:gpt-3.5-turbo-instruct}")
    private String model;

    @Value("${openai.timeout:60}")
    private int timeoutSeconds;

    private static final String OPENAI_URL = "https://api.openai.com/v1/completions";
    private static final String CACHE_PREFIX = "ai:analysis:";
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    // Konstruktor mit @Autowired (kein Lombok)
    @Autowired
    public AiService(RestTemplate restTemplate, StringRedisTemplate redisTemplate) {
        this.restTemplate = restTemplate;
        this.redisTemplate = redisTemplate;
    }

    // ... Rest des Codes bleibt identisch zu Ihrer ursprünglichen Implementierung
    public Map<String, String> analyzeText(String text) {
        log.info("Starting AI analysis for text with {} characters", text.length());
        
        Map<String, String> result = new HashMap<>();
        
        try {
            // Check cache first
            String cacheKey = generateCacheKey(text);
            Map<String, String> cachedResult = getCachedResult(cacheKey);
            if (cachedResult != null) {
                log.info("Returning cached AI analysis result");
                return cachedResult;
            }

            // Perform AI analysis
            String summary = getSummary(text);
            String keywords = getKeywords(text);
            String suggestedComponents = getSuggestedComponents(text);

            result.put("summary", summary);
            result.put("keywords", keywords);
            result.put("suggestedComponents", suggestedComponents);

            // Cache the result
            cacheResult(cacheKey, result);
            
            log.info("AI analysis completed successfully");
            return result;

        } catch (Exception e) {
            log.error("AI analysis failed, using fallback: {}", e.getMessage());
            return getFallbackAnalysis(text);
        }
    }

    private String getSummary(String text) {
        if (text.length() < 100) {
            return text + " [Text zu kurz für KI-Zusammenfassung]";
        }

        try {
            String prompt = String.format(
                "Erstelle eine kurze Zusammenfassung (max. 2 Sätze) auf Deutsch für folgenden Text: %s", 
                text.substring(0, Math.min(text.length(), 2000))
            );
            
            return callOpenAi(prompt, 200);
            
        } catch (Exception e) {
            log.warn("OpenAI summarization failed, using fallback: {}", e.getMessage());
            return getFallbackSummary(text);
        }
    }

    private String getKeywords(String text) {
        try {
            String prompt = String.format(
                "Extrahiere 5-10 wichtige Schlüsselwörter (nur die Wörter, durch Komma getrennt) auf Deutsch aus folgendem Text: %s", 
                text.substring(0, Math.min(text.length(), 1500))
            );
            
            return callOpenAi(prompt, 100);
            
        } catch (Exception e) {
            log.warn("OpenAI keyword extraction failed, using fallback: {}", e.getMessage());
            return getFallbackKeywords(text);
        }
    }

    private String getSuggestedComponents(String text) {
        try {
            String prompt = String.format(
                "Basierend auf folgendem Text, empfehle passende Technologie-Komponenten und Tools (nur die Namen, durch Komma getrennt): %s", 
                text.substring(0, Math.min(text.length(), 1500))
            );
            
            return callOpenAi(prompt, 150);
            
        } catch (Exception e) {
            log.warn("OpenAI component suggestion failed, using fallback: {}", e.getMessage());
            return getFallbackComponents(text);
        }
    }

    private String callOpenAi(String prompt, int maxTokens) {
        if (openAiApiKey == null || openAiApiKey.trim().isEmpty()) {
            throw new RuntimeException("OpenAI API key not configured");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = Map.of(
            "model", model,
            "prompt", prompt,
            "max_tokens", maxTokens,
            "temperature", 0.3,
            "top_p", 1.0,
            "frequency_penalty", 0.0,
            "presence_penalty", 0.0
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        try {
            log.debug("Calling OpenAI API with prompt length: {}", prompt.length());
            ResponseEntity<Map> response = restTemplate.exchange(
                OPENAI_URL, HttpMethod.POST, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return extractResponseText(response.getBody());
            } else {
                throw new RuntimeException("OpenAI API returned status: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("OpenAI API call failed: {}", e.getMessage());
            throw new RuntimeException("OpenAI API call failed: " + e.getMessage());
        }
    }

    private String extractResponseText(Map<String, Object> responseBody) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            
            if (choices != null && !choices.isEmpty()) {
                String result = (String) choices.get(0).get("text");
                if (result != null && !result.trim().isEmpty()) {
                    return result.trim();
                }
            }
            throw new RuntimeException("No valid response received from OpenAI");
            
        } catch (ClassCastException e) {
            throw new RuntimeException("Invalid response format from OpenAI API");
        }
    }

    private Map<String, String> getFallbackAnalysis(String text) {
        Map<String, String> result = new HashMap<>();
        result.put("summary", getFallbackSummary(text));
        result.put("keywords", getFallbackKeywords(text));
        result.put("suggestedComponents", getFallbackComponents(text));
        return result;
    }

    private String getFallbackSummary(String text) {
        if (text.length() > 400) {
            return text.substring(0, 400) + "... [Lokale Zusammenfassung - OpenAI nicht verfügbar]";
        }
        return text + " [Lokale Zusammenfassung - OpenAI nicht verfügbar]";
    }

    private String getFallbackKeywords(String text) {
        String[] words = text.toLowerCase()
            .replaceAll("[^a-züäöß\\s]", "")
            .split("\\s+");
        
        Set<String> uniqueKeywords = new HashSet<>();
        
        for (String word : words) {
            if (word.length() >= 4 && word.length() <= 15 && 
                !word.matches(".*[0-9].*") && 
                !isCommonWord(word)) {
                uniqueKeywords.add(word);
            }
        }
        
        String result = uniqueKeywords.stream()
            .sorted()
            .limit(15)
            .collect(Collectors.joining(", "));
        
        if (result.length() > 800) {
            result = result.substring(0, 797) + "...";
        }
        
        return result.isEmpty() ? "Keine Schlüsselwörter gefunden" : result;
    }

    private String getFallbackComponents(String text) {
        text = text.toLowerCase();
        Set<String> suggestions = new HashSet<>();
        
        if (text.contains("web") || text.contains("website") || text.contains("frontend")) {
            suggestions.addAll(Set.of("React", "Angular", "Vue.js"));
        }
        
        if (text.contains("api") || text.contains("rest") || text.contains("backend")) {
            suggestions.addAll(Set.of("Spring Boot", "Node.js", "Express.js"));
        }
        
        if (text.contains("daten") || text.contains("database") || text.contains("speicher")) {
            suggestions.addAll(Set.of("PostgreSQL", "MongoDB", "MySQL"));
        }
        
        if (text.contains("docker") || text.contains("cloud") || text.contains("deployment")) {
            suggestions.addAll(Set.of("Docker", "Kubernetes", "AWS"));
        }
        
        if (text.contains("mobile") || text.contains("app")) {
            suggestions.addAll(Set.of("React Native", "Flutter"));
        }
        
        if (suggestions.isEmpty()) {
            suggestions.addAll(Set.of("Spring Boot", "React", "PostgreSQL", "Docker"));
        }
        
        return String.join(", ", suggestions);
    }

    private boolean isCommonWord(String word) {
        Set<String> commonWords = Set.of(
            "eine", "einer", "eines", "dem", "den", "der", "die", "das",
            "und", "oder", "aber", "doch", "sondern", "für", "mit", "bei",
            "nach", "von", "zu", "an", "auf", "über", "unter", "vor",
            "hinter", "neben", "zwischen", "durch", "ohne", "gegen",
            "wird", "werden", "wurde", "worden", "sein", "haben", "hatte",
            "sind", "waren", "ist", "war", "kann", "könnte", "sollte",
            "würde", "muss", "soll", "will", "nicht", "auch", "noch",
            "nur", "schon", "bereits", "immer", "alle", "jede", "jeden",
            "mehr", "sehr", "dann", "wenn", "dass", "als", "wie", "zum"
        );
        return commonWords.contains(word);
    }

    private String generateCacheKey(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(text.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(text.hashCode());
        }
    }

    private Map<String, String> getCachedResult(String cacheKey) {
        try {
            String cachedSummary = redisTemplate.opsForValue().get(CACHE_PREFIX + cacheKey + ":summary");
            String cachedKeywords = redisTemplate.opsForValue().get(CACHE_PREFIX + cacheKey + ":keywords");
            String cachedComponents = redisTemplate.opsForValue().get(CACHE_PREFIX + cacheKey + ":components");
            
            if (cachedSummary != null && cachedKeywords != null && cachedComponents != null) {
                Map<String, String> result = new HashMap<>();
                result.put("summary", cachedSummary);
                result.put("keywords", cachedKeywords);
                result.put("suggestedComponents", cachedComponents);
                return result;
            }
        } catch (Exception e) {
            log.warn("Failed to get cached result: {}", e.getMessage());
        }
        return null;
    }

    private void cacheResult(String cacheKey, Map<String, String> result) {
        try {
            redisTemplate.opsForValue().set(
                CACHE_PREFIX + cacheKey + ":summary", 
                result.get("summary"), 
                CACHE_TTL.toSeconds(), 
                TimeUnit.SECONDS
            );
            redisTemplate.opsForValue().set(
                CACHE_PREFIX + cacheKey + ":keywords", 
                result.get("keywords"), 
                CACHE_TTL.toSeconds(), 
                TimeUnit.SECONDS
            );
            redisTemplate.opsForValue().set(
                CACHE_PREFIX + cacheKey + ":components", 
                result.get("suggestedComponents"), 
                CACHE_TTL.toSeconds(), 
                TimeUnit.SECONDS
            );
            log.debug("Cached AI analysis result for 24 hours");
        } catch (Exception e) {
            log.warn("Failed to cache result: {}", e.getMessage());
        }
    }

    public boolean isOpenAiConfigured() {
        return openAiApiKey != null && !openAiApiKey.trim().isEmpty();
    }

    public String getModelInfo() {
        return model;
    }
}