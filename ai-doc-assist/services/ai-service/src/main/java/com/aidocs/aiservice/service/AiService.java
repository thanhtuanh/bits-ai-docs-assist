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

    @Autowired
    public AiService(RestTemplate restTemplate, StringRedisTemplate redisTemplate) {
        this.restTemplate = restTemplate;
        this.redisTemplate = redisTemplate;
    }

    public Map<String, String> analyzeText(String text) {
        log.info("Starting AI analysis for text with {} characters", text.length());

        String cacheKey = generateCacheKey(text);

        // 1️⃣ Cache prüfen
        Map<String, String> cachedResult = getCachedResult(cacheKey);
        if (cachedResult != null) {
            log.info("Cache hit → Returning cached result");
            return cachedResult;
        }
        log.info("Cache miss or incomplete cache → Running analysis");

        // 2️⃣ Analyse durchführen (OpenAI oder Fallback)
        Map<String, String> result = new HashMap<>();
        try {
            String summary = getSummary(text);
            String keywords = getKeywords(text);
            String components = getSuggestedComponents(text);

            result.put("summary", summary);
            result.put("keywords", keywords);
            result.put("suggestedComponents", components);

        } catch (Exception e) {
            log.error("AI analysis failed, using fallback: {}", e.getMessage());
            result = getFallbackAnalysis(text);
        }

        // 3️⃣ Neues Ergebnis in Cache speichern
        cacheResult(cacheKey, result);

        log.info("AI analysis completed successfully");
        return result;
    }

    // ================== OpenAI Calls ==================

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

        log.debug("Calling OpenAI API with prompt length: {}", prompt.length());
        ResponseEntity<Map> response = restTemplate.exchange(OPENAI_URL, HttpMethod.POST, entity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return extractResponseText(response.getBody());
        }
        throw new RuntimeException("OpenAI API returned status: " + response.getStatusCode());
    }

    private String extractResponseText(Map<String, Object> responseBody) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");

        if (choices != null && !choices.isEmpty()) {
            String result = (String) choices.get(0).get("text");
            if (result != null && !result.trim().isEmpty()) {
                return result.trim();
            }
        }
        throw new RuntimeException("No valid response from OpenAI");
    }

    // ================== Fallback ==================

    private Map<String, String> getFallbackAnalysis(String text) {
        Map<String, String> result = new HashMap<>();
        result.put("summary", getFallbackSummary(text));
        result.put("keywords", getFallbackKeywords(text));
        result.put("suggestedComponents", getFallbackComponents(text));
        return result;
    }

    private String getFallbackSummary(String text) {
        return text.length() > 400
                ? text.substring(0, 400) + "... [Lokale Zusammenfassung - OpenAI nicht verfügbar]"
                : text + " [Lokale Zusammenfassung - OpenAI nicht verfügbar]";
    }

    private String getFallbackKeywords(String text) {
        String[] words = text.toLowerCase()
                .replaceAll("[^a-züäöß\\s]", "")
                .split("\\s+");

        Set<String> uniqueKeywords = Arrays.stream(words)
                .filter(w -> w.length() >= 4 && w.length() <= 15 && !isCommonWord(w))
                .collect(Collectors.toSet());

        String result = String.join(", ", uniqueKeywords);
        return result.isEmpty() ? "Keine Schlüsselwörter gefunden" : result;
    }

    private String getFallbackComponents(String text) {
        Set<String> suggestions = new HashSet<>();
        text = text.toLowerCase();

        if (text.contains("web") || text.contains("frontend")) suggestions.addAll(Set.of("React", "Angular"));
        if (text.contains("api") || text.contains("backend")) suggestions.addAll(Set.of("Spring Boot", "Node.js"));
        if (text.contains("daten") || text.contains("database")) suggestions.addAll(Set.of("PostgreSQL", "MongoDB"));
        if (text.contains("docker") || text.contains("cloud")) suggestions.addAll(Set.of("Docker", "Kubernetes"));

        if (suggestions.isEmpty()) suggestions.addAll(Set.of("Spring Boot", "React", "PostgreSQL"));
        return String.join(", ", suggestions);
    }

    private boolean isCommonWord(String word) {
        return Set.of("eine", "einer", "der", "die", "das", "und", "oder", "aber", "nicht")
                .contains(word);
    }

    // ================== Cache ==================

    private String generateCacheKey(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(text.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
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
            } else {
                log.warn("Cache incomplete for key={}, using fallback", cacheKey);
                return null; // Force Fallback
            }
        } catch (Exception e) {
            log.warn("Failed to get cached result: {}", e.getMessage());
            return null;
        }
    }


    private void cacheResult(String cacheKey, Map<String, String> result) {
        try {
            redisTemplate.opsForValue().set(CACHE_PREFIX + cacheKey + ":summary",
                    result.get("summary"), CACHE_TTL.toSeconds(), TimeUnit.SECONDS);
            redisTemplate.opsForValue().set(CACHE_PREFIX + cacheKey + ":keywords",
                    result.get("keywords"), CACHE_TTL.toSeconds(), TimeUnit.SECONDS);
            redisTemplate.opsForValue().set(CACHE_PREFIX + cacheKey + ":components",
                    result.get("suggestedComponents"), CACHE_TTL.toSeconds(), TimeUnit.SECONDS);
            log.debug("Cached analysis for 24h (key={})", cacheKey);
        } catch (Exception e) {
            log.warn("Cache write failed: {}", e.getMessage());
        }
    }

    public boolean isOpenAiConfigured() {
        return openAiApiKey != null && !openAiApiKey.trim().isEmpty();
    }

    public String getModelInfo() {
        return model;
    }
}
