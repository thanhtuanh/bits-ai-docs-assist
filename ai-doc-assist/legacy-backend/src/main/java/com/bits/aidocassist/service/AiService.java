package com.bits.aidocassist.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiService {

    @Value("${openai.api.key:}")
    private String openAiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String OPENAI_URL = "https://api.openai.com/v1/completions";

    public String summarizeText(String text) {
        if (text.length() < 100) {
            return text + " [Text zu kurz für KI-Zusammenfassung]";
        }

        if (openAiApiKey != null && !openAiApiKey.trim().isEmpty()) {
            try {
                String prompt = createSummarizationPrompt(text);
                String result = callOpenAi(prompt, 200);
                System.out.println("✅ OpenAI Zusammenfassung erfolgreich erstellt");
                return result;
            } catch (Exception e) {
                System.err.println("❌ OpenAI Summarization failed: " + e.getMessage());
                return getFallbackSummary(text);
            }
        } else {
            System.out.println("⚠️ Kein OpenAI API Key - verwende Fallback für Zusammenfassung");
        }
        return getFallbackSummary(text);
    }

    public String extractKeywords(String text) {
        if (openAiApiKey != null && !openAiApiKey.trim().isEmpty()) {
            try {
                String prompt = createKeywordPrompt(text);
                String result = callOpenAi(prompt, 100);
                System.out.println("✅ OpenAI Keywords erfolgreich extrahiert");
                return result;
            } catch (Exception e) {
                System.err.println("❌ OpenAI Keyword extraction failed: " + e.getMessage());
                return getFallbackKeywords(text);
            }
        } else {
            System.out.println("⚠️ Kein OpenAI API Key - verwende Fallback für Keywords");
        }
        return getFallbackKeywords(text);
    }

    public String suggestComponents(String text) {
        if (openAiApiKey != null && !openAiApiKey.trim().isEmpty()) {
            try {
                String prompt = createComponentPrompt(text);
                String result = callOpenAi(prompt, 150);
                System.out.println("✅ OpenAI Komponenten-Empfehlungen erfolgreich erstellt");
                return result;
            } catch (Exception e) {
                System.err.println("❌ OpenAI Component suggestion failed: " + e.getMessage());
                return getFallbackComponents(text);
            }
        } else {
            System.out.println("⚠️ Kein OpenAI API Key - verwende Fallback für Komponenten");
        }
        return getFallbackComponents(text);
    }

    private String callOpenAi(String prompt, int maxTokens) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo-instruct");
        requestBody.put("prompt", prompt);
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("temperature", 0.3);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        try {
            System.out.println("🤖 Rufe OpenAI API auf...");
            ResponseEntity<Map> response = restTemplate.exchange(
                OPENAI_URL, HttpMethod.POST, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    String result = (String) choices.get(0).get("text");
                    return result != null ? result.trim() : "Keine Antwort erhalten";
                }
            } else {
                System.err.println("❌ OpenAI API Status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("❌ HTTP Request to OpenAI failed: " + e.getMessage());
            throw e;
        }
        
        return "API-Aufruf fehlgeschlagen";
    }

    private String createSummarizationPrompt(String text) {
        String inputText = text.length() > 2000 ? text.substring(0, 2000) + "..." : text;
        return String.format(
            "Erstelle eine präzise, professionelle Zusammenfassung des folgenden deutschen Dokuments. " +
            "Die Zusammenfassung soll 2-3 Sätze lang sein und die wichtigsten Punkte hervorheben:\n\n%s\n\nZusammenfassung:",
            inputText
        );
    }

    private String createKeywordPrompt(String text) {
        String inputText = text.length() > 1500 ? text.substring(0, 1500) + "..." : text;
        return String.format(
            "Extrahiere die 12 wichtigsten Schlüsselwörter aus dem folgenden deutschen Text. " +
            "Gib nur die Wörter zurück, getrennt durch Kommas:\n\n%s\n\nSchlüsselwörter:",
            inputText
        );
    }

    private String createComponentPrompt(String text) {
        String inputText = text.length() > 1500 ? text.substring(0, 1500) + "..." : text;
        return String.format(
            "Basierend auf der folgenden deutschen Projektbeschreibung, empfehle die 8-10 wichtigsten " +
            "modernen Technologien und Frameworks. Gib nur die Namen zurück, getrennt durch Kommas:\n\n%s\n\nTechnologien:",
            inputText
        );
    }

    // Fallback-Methoden
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
        
        // Web-Technologien
        if (text.contains("web") || text.contains("website") || text.contains("frontend")) {
            suggestions.addAll(Set.of("React", "Angular", "Vue.js"));
        }
        
        // Backend-Technologien
        if (text.contains("api") || text.contains("rest") || text.contains("backend")) {
            suggestions.addAll(Set.of("Spring Boot", "Node.js", "Express.js"));
        }
        
        // Datenbank
        if (text.contains("daten") || text.contains("database") || text.contains("speicher")) {
            suggestions.addAll(Set.of("PostgreSQL", "MongoDB", "MySQL"));
        }
        
        // Cloud & DevOps
        if (text.contains("docker") || text.contains("cloud") || text.contains("deployment")) {
            suggestions.addAll(Set.of("Docker", "Kubernetes", "AWS"));
        }
        
        // Mobile
        if (text.contains("mobile") || text.contains("app")) {
            suggestions.addAll(Set.of("React Native", "Flutter"));
        }
        
        // Testing
        if (text.contains("test") || text.contains("qualität")) {
            suggestions.addAll(Set.of("JUnit", "Jest", "Cypress"));
        }
        
        // Sicherheit
        if (text.contains("sicherheit") || text.contains("security") || text.contains("authentifizierung")) {
            suggestions.addAll(Set.of("Spring Security", "JWT", "OAuth"));
        }
        
        // Standard-Technologien falls nichts spezifisches gefunden
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
}