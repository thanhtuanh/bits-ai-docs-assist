package com.bits.aidocassist.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AiService {

    @Value("${openai.api.key:}")
    private String openAiApiKey;

    @Value("${ai.analysis.summary.max-input-chars:5000}")
    private int summaryMaxChars;

    @Value("${ai.analysis.keywords.max-input-chars:3000}")
    private int keywordsMaxChars;

    @Value("${ai.analysis.components.max-input-chars:3000}")
    private int componentsMaxChars;

    @Value("${ai.analysis.summary.max-tokens:400}")
    private int summaryMaxTokens;

    @Value("${ai.analysis.keywords.max-tokens:200}")
    private int keywordsMaxTokens;

    @Value("${ai.analysis.components.max-tokens:300}")
    private int componentsMaxTokens;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String OPENAI_URL = "https://api.openai.com/v1/completions";

    // Qualitäts-Metriken
    private final Map<String, QualityMetrics> qualityMetrics = new HashMap<>();

    public String summarizeText(String text) {
        long startTime = System.currentTimeMillis();
        
        if (text.length() < 100) {
            return text + " [Text zu kurz für KI-Zusammenfassung]";
        }

        if (openAiApiKey != null && !openAiApiKey.trim().isEmpty()) {
            try {
                String prompt = createAdvancedSummarizationPrompt(text);
                String result = callOpenAi(prompt, summaryMaxTokens, "summarization");
                
                // Qualität messen
                recordQualityMetrics("summarization", startTime, true, result.length());
                
                System.out.println("✅ OpenAI Zusammenfassung erfolgreich erstellt");
                return result;
            } catch (Exception e) {
                recordQualityMetrics("summarization", startTime, false, 0);
                System.err.println("❌ OpenAI Summarization failed: " + e.getMessage());
                return getFallbackSummary(text);
            }
        } else {
            System.out.println("⚠️ Kein OpenAI API Key - verwende Fallback für Zusammenfassung");
        }
        return getFallbackSummary(text);
    }

    public String extractKeywords(String text) {
        long startTime = System.currentTimeMillis();
        
        if (openAiApiKey != null && !openAiApiKey.trim().isEmpty()) {
            try {
                String prompt = createAdvancedKeywordPrompt(text);
                String result = callOpenAi(prompt, keywordsMaxTokens, "keywords");
                
                recordQualityMetrics("keywords", startTime, true, result.length());
                
                System.out.println("✅ OpenAI Keywords erfolgreich extrahiert");
                return result;
            } catch (Exception e) {
                recordQualityMetrics("keywords", startTime, false, 0);
                System.err.println("❌ OpenAI Keyword extraction failed: " + e.getMessage());
                return getFallbackKeywords(text);
            }
        } else {
            System.out.println("⚠️ Kein OpenAI API Key - verwende Fallback für Keywords");
        }
        return getFallbackKeywords(text);
    }

    public String suggestComponents(String text) {
        long startTime = System.currentTimeMillis();
        
        if (openAiApiKey != null && !openAiApiKey.trim().isEmpty()) {
            try {
                String prompt = createAdvancedComponentPrompt(text);
                String result = callOpenAi(prompt, componentsMaxTokens, "components");
                
                recordQualityMetrics("components", startTime, true, result.length());
                
                System.out.println("✅ OpenAI Komponenten-Empfehlungen erfolgreich erstellt");
                return result;
            } catch (Exception e) {
                recordQualityMetrics("components", startTime, false, 0);
                System.err.println("❌ OpenAI Component suggestion failed: " + e.getMessage());
                return getFallbackComponents(text);
            }
        } else {
            System.out.println("⚠️ Kein OpenAI API Key - verwende Fallback für Komponenten");
        }
        return getFallbackComponents(text);
    }

    // ========================================
    // VERBESSERTE PROMPT-METHODEN
    // ========================================

    private String createAdvancedSummarizationPrompt(String text) {
        String inputText = text.length() > summaryMaxChars ? 
            text.substring(0, summaryMaxChars) + "..." : text;
        
        return String.format(
            "Du bist ein erfahrener Business-Analyst und Technical Writer. " +
            "Erstelle eine präzise, professionelle deutsche Zusammenfassung des folgenden Dokuments.\n\n" +
            
            "AUFGABE:\n" +
            "- Identifiziere das Hauptziel und die wichtigsten Aussagen\n" +
            "- Fokussiere auf konkrete Ergebnisse und Handlungsempfehlungen\n" +
            "- Verwende klare, geschäftliche Sprache\n" +
            "- Länge: Exakt 2-3 vollständige Sätze\n\n" +
            
            "STIL:\n" +
            "- Professionell und objektiv\n" +
            "- Konkret statt abstrakt\n" +
            "- Handlungsorientiert\n\n" +
            
            "DOKUMENT:\n" +
            "==========\n" +
            "%s\n" +
            "==========\n\n" +
            
            "ZUSAMMENFASSUNG:",
            inputText
        );
    }

    private String createAdvancedKeywordPrompt(String text) {
        String inputText = text.length() > keywordsMaxChars ? 
            text.substring(0, keywordsMaxChars) + "..." : text;
        
        return String.format(
            "Du bist ein KI-Experte für Textanalyse und Informationsextraktion. " +
            "Extrahiere die wichtigsten Schlüsselwörter aus dem deutschen Text.\n\n" +
            
            "AUFGABE:\n" +
            "- Identifiziere die 12-15 wichtigsten Begriffe\n" +
            "- Priorisiere: Fachbegriffe, Technologien, Branchen-Terme, Hauptkonzepte\n" +
            "- Bevorzuge spezifische über allgemeine Begriffe\n" +
            "- Verwende die Original-Schreibweise aus dem Text\n\n" +
            
            "KATEGORIEN (in Priorität):\n" +
            "1. Technologien und Tools\n" +
            "2. Fachbegriffe und Methoden\n" +
            "3. Branchen und Bereiche\n" +
            "4. Wichtige Konzepte\n\n" +
            
            "AUSGABE-FORMAT:\n" +
            "- NUR die Wörter, durch Kommas getrennt\n" +
            "- Keine Erklärungen oder zusätzlichen Texte\n" +
            "- Beispiel: React, Spring Boot, Agile, Kubernetes, REST API\n\n" +
            
            "TEXT:\n" +
            "=====\n" +
            "%s\n" +
            "=====\n\n" +
            
            "SCHLÜSSELWÖRTER:",
            inputText
        );
    }

    private String createAdvancedComponentPrompt(String text) {
        String inputText = text.length() > componentsMaxChars ? 
            text.substring(0, componentsMaxChars) + "..." : text;
        
        return String.format(
            "Du bist ein Senior Software-Architekt mit 10+ Jahren Erfahrung in modernen Tech-Stacks. " +
            "Analysiere die Projektbeschreibung und empfehle einen optimalen Technologie-Stack für 2025.\n\n" +
            
            "ANALYSE-KRITERIEN:\n" +
            "- Skalierbarkeit und Performance\n" +
            "- Wartbarkeit und Developer Experience\n" +
            "- Community-Support und Zukunftssicherheit\n" +
            "- Kosten-Nutzen-Verhältnis\n\n" +
            
            "KATEGORIEN ZU BERÜCKSICHTIGEN:\n" +
            "- Frontend: Frameworks, UI-Libraries, Build-Tools\n" +
            "- Backend: Frameworks, APIs, Microservices\n" +
            "- Datenbank: SQL/NoSQL, Caching, Search\n" +
            "- Cloud & DevOps: Container, CI/CD, Monitoring\n" +
            "- Entwicklungstools: Testing, Code-Quality, Documentation\n\n" +
            
            "AUSGABE-FORMAT:\n" +
            "- 8-12 konkrete Technologien\n" +
            "- Durch Kommas getrennt, keine Erklärungen\n" +
            "- Priorisiere moderne, bewährte Lösungen\n" +
            "- Beispiel: Next.js, FastAPI, PostgreSQL, Docker, Kubernetes, TypeScript\n\n" +
            
            "PROJEKTBESCHREIBUNG:\n" +
            "==================\n" +
            "%s\n" +
            "==================\n\n" +
            
            "EMPFOHLENER TECHNOLOGIE-STACK:",
            inputText
        );
    }

    // ========================================
    // OPENAI API AUFRUF
    // ========================================

    private String callOpenAi(String prompt, int maxTokens, String analysisType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo-instruct");
        requestBody.put("prompt", prompt);
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("temperature", 0.3);
        requestBody.put("top_p", 0.9);
        requestBody.put("frequency_penalty", 0.1);
        requestBody.put("presence_penalty", 0.1);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        try {
            System.out.println("🤖 Rufe OpenAI API auf für: " + analysisType);
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

    // ========================================
    // VERBESSERTE FALLBACK-METHODEN
    // ========================================

    private String getFallbackSummary(String text) {
        if (text.length() < 200) {
            return text + " [Lokale Analyse - OpenAI nicht verfügbar]";
        }
        
        // Intelligentere Zusammenfassung: Erste und letzte Sätze
        String[] sentences = text.split("\\. ");
        if (sentences.length >= 3) {
            String firstSentence = sentences[0] + ".";
            String lastSentence = sentences[sentences.length - 1];
            if (!lastSentence.endsWith(".")) lastSentence += ".";
            
            return firstSentence + " " + lastSentence + " [Lokale Analyse - OpenAI nicht verfügbar]";
        }
        
        // Fallback: Erste 300 Zeichen
        return text.substring(0, Math.min(300, text.length())) + "... [Lokale Analyse - OpenAI nicht verfügbar]";
    }

    private String getFallbackKeywords(String text) {
        String[] words = text.toLowerCase()
            .replaceAll("[^a-züäöß\\s]", "")
            .split("\\s+");
        
        Set<String> uniqueKeywords = new HashSet<>();
        Set<String> techTerms = getTechTerms();
        
        // Priorisiere Tech-Begriffe
        for (String word : words) {
            if (techTerms.contains(word.toLowerCase()) && word.length() >= 3) {
                uniqueKeywords.add(capitalizeFirst(word));
            }
        }
        
        // Füge andere relevante Wörter hinzu
        for (String word : words) {
            if (word.length() >= 4 && word.length() <= 15 && 
                !word.matches(".*[0-9].*") && 
                !isCommonWord(word) &&
                uniqueKeywords.size() < 15) {
                uniqueKeywords.add(capitalizeFirst(word));
            }
        }
        
        String result = uniqueKeywords.stream()
            .sorted()
            .limit(15)
            .collect(Collectors.joining(", "));
        
        return result.isEmpty() ? "Keine Schlüsselwörter gefunden" : result;
    }
    
    private String getFallbackComponents(String text) {
        text = text.toLowerCase();
        Set<String> suggestions = new LinkedHashSet<>(); // Ordered set
        
        // Erweiterte Technologie-Erkennung
        Map<String, Set<String>> techCategories = getTechCategories();
        
        for (Map.Entry<String, Set<String>> category : techCategories.entrySet()) {
            for (String keyword : category.getValue()) {
                if (text.contains(keyword)) {
                    suggestions.addAll(getTechRecommendations(category.getKey()));
                }
            }
        }
        
        // Standard-Empfehlungen falls nichts gefunden
        if (suggestions.isEmpty()) {
            suggestions.addAll(Set.of(
                "React", "Spring Boot", "PostgreSQL", "Docker", 
                "TypeScript", "REST API", "Git", "Jest"
            ));
        }
        
        return suggestions.stream()
            .limit(10)
            .collect(Collectors.joining(", "));
    }

    // ========================================
    // HILFSMETHODEN
    // ========================================

    private Set<String> getTechTerms() {
        return Set.of(
            "react", "angular", "vue", "javascript", "typescript", "python", "java", "spring",
            "docker", "kubernetes", "aws", "azure", "mongodb", "postgresql", "mysql", "redis",
            "api", "rest", "graphql", "microservices", "devops", "ci/cd", "git", "jenkins",
            "agile", "scrum", "kanban", "testing", "unit", "integration", "selenium"
        );
    }
    
    private Map<String, Set<String>> getTechCategories() {
        Map<String, Set<String>> categories = new HashMap<>();
        
        categories.put("web", Set.of("web", "website", "frontend", "backend", "html", "css", "javascript"));
        categories.put("mobile", Set.of("mobile", "app", "android", "ios", "react native", "flutter"));
        categories.put("database", Set.of("daten", "database", "sql", "nosql", "speicher", "persistent"));
        categories.put("cloud", Set.of("cloud", "aws", "azure", "gcp", "serverless", "lambda"));
        categories.put("ai", Set.of("ai", "ml", "machine learning", "künstliche intelligenz", "chatbot"));
        categories.put("ecommerce", Set.of("shop", "ecommerce", "payment", "cart", "checkout"));
        
        return categories;
    }
    
    private Set<String> getTechRecommendations(String category) {
        Map<String, Set<String>> recommendations = new HashMap<>();
        
        recommendations.put("web", Set.of("React", "Next.js", "TypeScript", "Tailwind CSS"));
        recommendations.put("mobile", Set.of("React Native", "Flutter", "Expo", "Firebase"));
        recommendations.put("database", Set.of("PostgreSQL", "MongoDB", "Redis", "Prisma"));
        recommendations.put("cloud", Set.of("AWS", "Vercel", "Docker", "Terraform"));
        recommendations.put("ai", Set.of("OpenAI API", "LangChain", "Pinecone", "Hugging Face"));
        recommendations.put("ecommerce", Set.of("Stripe", "Shopify", "WooCommerce", "Medusa"));
        
        return recommendations.getOrDefault(category, new HashSet<>());
    }
    
    private boolean isCommonWord(String word) {
        Set<String> commonWords = Set.of(
            "eine", "einer", "eines", "dem", "den", "der", "die", "das", "und", "oder", "aber", 
            "doch", "sondern", "für", "mit", "bei", "nach", "von", "zu", "an", "auf", "über",
            "unter", "vor", "hinter", "neben", "zwischen", "durch", "ohne", "gegen", "wird",
            "werden", "wurde", "worden", "sein", "haben", "hatte", "sind", "waren", "ist", "war",
            "kann", "könnte", "sollte", "würde", "muss", "soll", "will", "nicht", "auch", "noch",
            "nur", "schon", "bereits", "immer", "alle", "jede", "jeden", "mehr", "sehr", "dann",
            "wenn", "dass", "als", "wie", "zum", "zur", "beim", "beim", "sowie", "bzw", "etc"
        );
        return commonWords.contains(word.toLowerCase());
    }
    
    private String capitalizeFirst(String word) {
        if (word == null || word.isEmpty()) return word;
        return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
    }

    // ========================================
    // QUALITÄTS-METRIKEN
    // ========================================

    private void recordQualityMetrics(String analysisType, long startTime, boolean success, int resultLength) {
        long responseTime = System.currentTimeMillis() - startTime;
        
        QualityMetrics metrics = qualityMetrics.computeIfAbsent(analysisType, k -> new QualityMetrics());
        metrics.recordCall(responseTime, success, resultLength);
        
        System.out.printf("📊 %s: %dms, Success: %s, Length: %d chars%n", 
            analysisType, responseTime, success, resultLength);
    }

    public Map<String, QualityMetrics> getQualityMetrics() {
        return Collections.unmodifiableMap(qualityMetrics);
    }

    // Innere Klasse für Metriken
    public static class QualityMetrics {
        private int totalCalls = 0;
        private int successfulCalls = 0;
        private long totalResponseTime = 0;
        private int totalResultLength = 0;
        private LocalDateTime lastCall;

        public void recordCall(long responseTime, boolean success, int resultLength) {
            totalCalls++;
            if (success) successfulCalls++;
            totalResponseTime += responseTime;
            totalResultLength += resultLength;
            lastCall = LocalDateTime.now();
        }

        public double getSuccessRate() {
            return totalCalls > 0 ? (double) successfulCalls / totalCalls * 100 : 0;
        }

        public double getAvgResponseTime() {
            return totalCalls > 0 ? (double) totalResponseTime / totalCalls : 0;
        }

        public double getAvgResultLength() {
            return successfulCalls > 0 ? (double) totalResultLength / successfulCalls : 0;
        }

        // Getters
        public int getTotalCalls() { return totalCalls; }
        public int getSuccessfulCalls() { return successfulCalls; }
        public LocalDateTime getLastCall() { return lastCall; }
    }
}