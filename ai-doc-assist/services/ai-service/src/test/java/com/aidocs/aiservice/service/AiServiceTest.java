package com.aidocs.aiservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private AiService aiService;

    @BeforeEach
    void setUp() {
        aiService = new AiService(restTemplate, redisTemplate);
        
        // Mock Redis ValueOperations
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        // Konfiguration via ReflectionTestUtils setzen
        ReflectionTestUtils.setField(aiService, "openAiApiKey", "test-api-key");
        ReflectionTestUtils.setField(aiService, "model", "gpt-3.5-turbo-instruct");
        ReflectionTestUtils.setField(aiService, "timeoutSeconds", 60);
    }

    @Test
    void testAnalyzeText_WithCachedResult_ShouldReturnCache() {
        // Given
        String testText = "Test text for analysis";
        
        // Mock Cache Hit
        when(valueOperations.get(contains(":summary"))).thenReturn("Cached summary");
        when(valueOperations.get(contains(":keywords"))).thenReturn("Cached keywords");
        when(valueOperations.get(contains(":components"))).thenReturn("Cached components");

        // When
        Map<String, String> result = aiService.analyzeText(testText);

        // Then
        assertNotNull(result);
        assertEquals("Cached summary", result.get("summary"));
        assertEquals("Cached keywords", result.get("keywords"));
        assertEquals("Cached components", result.get("suggestedComponents"));
        
        // Verify keine OpenAI Calls gemacht wurden
        verify(restTemplate, never()).exchange(any(), any(), any(), eq(Map.class));
    }

    @Test
    void testAnalyzeText_WithoutCache_ShouldCallOpenAI() {
        // Given
        String testText = "Test text for AI analysis with Spring Boot and React";
        
        // Mock Cache Miss
        when(valueOperations.get(anyString())).thenReturn(null);
        
        // Mock OpenAI Responses
        Map<String, Object> openAiResponse = Map.of(
            "choices", List.of(Map.of("text", "Mocked AI response"))
        );
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(openAiResponse, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
            .thenReturn(responseEntity);

        // When
        Map<String, String> result = aiService.analyzeText(testText);

        // Then
        assertNotNull(result);
        assertNotNull(result.get("summary"));
        assertNotNull(result.get("keywords"));
        assertNotNull(result.get("suggestedComponents"));
        
        // Verify OpenAI wurde 3x aufgerufen (Summary, Keywords, Components)
        verify(restTemplate, times(3)).exchange(anyString(), any(), any(), eq(Map.class));
        
        // Verify Caching wurde aufgerufen
        verify(valueOperations, times(3)).set(anyString(), anyString(), anyLong(), eq(TimeUnit.SECONDS));
    }

    @Test
    void testAnalyzeText_OpenAIFailure_ShouldUseFallback() {
        // Given
        String testText = "Test text with web development and database";
        
        // Mock Cache Miss
        when(valueOperations.get(anyString())).thenReturn(null);
        
        // Mock OpenAI Failure
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
            .thenThrow(new RuntimeException("OpenAI API error"));

        // When
        Map<String, String> result = aiService.analyzeText(testText);

        // Then
        assertNotNull(result);
        assertTrue(result.get("summary").contains("[Lokale Zusammenfassung - OpenAI nicht verfügbar]"));
        assertNotNull(result.get("keywords"));
        assertTrue(result.get("suggestedComponents").contains("Spring Boot")); // Fallback sollte Web-Technologien enthalten
    }

    @Test
    void testAnalyzeText_ShortText_ShouldSkipSummary() {
        // Given
        String shortText = "Kurz"; // Weniger als 100 Zeichen
        
        // Mock Cache Miss
        when(valueOperations.get(anyString())).thenReturn(null);

        // When
        Map<String, String> result = aiService.analyzeText(shortText);

        // Then
        assertNotNull(result);
        assertTrue(result.get("summary").contains("[Text zu kurz für KI-Zusammenfassung]"));
        assertNotNull(result.get("keywords"));
        assertNotNull(result.get("suggestedComponents"));
    }

    @Test
    void testFallbackKeywords_ShouldExtractGermanWords() {
        // Given
        String testText = "Dies ist ein Beispieltext mit verschiedenen Technologien wie Spring Boot und React für die Webentwicklung";
        
        // Mock Cache Miss und OpenAI Failure
        when(valueOperations.get(anyString())).thenReturn(null);
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
            .thenThrow(new RuntimeException("API Error"));

        // When
        Map<String, String> result = aiService.analyzeText(testText);

        // Then
        String keywords = result.get("keywords");
        assertNotNull(keywords);
        assertTrue(keywords.contains("beispieltext") || keywords.contains("technologien") || keywords.contains("webentwicklung"));
        assertFalse(keywords.contains("ist")); // Common words sollten gefiltert werden
        assertFalse(keywords.contains("ein"));
    }

    @Test
    void testFallbackComponents_WebContext_ShouldSuggestWebTechnologies() {
        // Given
        String webText = "Wir entwickeln eine moderne Webanwendung mit Frontend und Backend API";
        
        // Mock Cache Miss und OpenAI Failure
        when(valueOperations.get(anyString())).thenReturn(null);
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
            .thenThrow(new RuntimeException("API Error"));

        // When
        Map<String, String> result = aiService.analyzeText(webText);

        // Then
        String components = result.get("suggestedComponents");
        assertNotNull(components);
        assertTrue(components.contains("React") || components.contains("Angular") || components.contains("Vue.js"));
        assertTrue(components.contains("Spring Boot") || components.contains("Node.js"));
    }

    @Test
    void testFallbackComponents_DatabaseContext_ShouldSuggestDatabases() {
        // Given
        String dbText = "Das System benötigt eine robuste Datenbank für die Speicherung von Dokumenten";
        
        // Mock Cache Miss und OpenAI Failure
        when(valueOperations.get(anyString())).thenReturn(null);
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
            .thenThrow(new RuntimeException("API Error"));

        // When
        Map<String, String> result = aiService.analyzeText(dbText);

        // Then
        String components = result.get("suggestedComponents");
        assertNotNull(components);
        assertTrue(components.contains("PostgreSQL") || components.contains("MongoDB") || components.contains("MySQL"));
    }

    @Test
    void testFallbackComponents_DockerContext_ShouldSuggestDevOps() {
        // Given
        String dockerText = "Deployment mit Docker Container in der Cloud";
        
        // Mock Cache Miss und OpenAI Failure
        when(valueOperations.get(anyString())).thenReturn(null);
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
            .thenThrow(new RuntimeException("API Error"));

        // When
        Map<String, String> result = aiService.analyzeText(dockerText);

        // Then
        String components = result.get("suggestedComponents");
        assertNotNull(components);
        assertTrue(components.contains("Docker"));
        assertTrue(components.contains("Kubernetes") || components.contains("AWS"));
    }

    @Test
    void testIsOpenAiConfigured_WithApiKey_ShouldReturnTrue() {
        // Given - API Key ist bereits in setUp() gesetzt
        
        // When
        boolean configured = aiService.isOpenAiConfigured();

        // Then
        assertTrue(configured);
    }

    @Test
    void testIsOpenAiConfigured_WithoutApiKey_ShouldReturnFalse() {
        // Given
        ReflectionTestUtils.setField(aiService, "openAiApiKey", "");

        // When
        boolean configured = aiService.isOpenAiConfigured();

        // Then
        assertFalse(configured);
    }

    @Test
    void testGetModelInfo_ShouldReturnConfiguredModel() {
        // When
        String model = aiService.getModelInfo();

        // Then
        assertEquals("gpt-3.5-turbo-instruct", model);
    }

    @Test
    void testCaching_ShouldGenerateConsistentCacheKey() {
        // Given
        String testText = "Identical text for caching test";
        
        // Mock Cache Miss first, then Cache Hit
        when(valueOperations.get(anyString()))
            .thenReturn(null)  // First call: cache miss
            .thenReturn("Cached summary")  // Second call: cache hit
            .thenReturn("Cached keywords")
            .thenReturn("Cached components");

        // Mock OpenAI Response für ersten Call
        Map<String, Object> openAiResponse = Map.of(
            "choices", List.of(Map.of("text", "AI response"))
        );
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(openAiResponse, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
            .thenReturn(responseEntity);

        // When - Erster Call sollte OpenAI aufrufen
        Map<String, String> firstResult = aiService.analyzeText(testText);
        
        // Reset Mock für zweiten Call
        reset(valueOperations);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(contains(":summary"))).thenReturn("Cached summary");
        when(valueOperations.get(contains(":keywords"))).thenReturn("Cached keywords");  
        when(valueOperations.get(contains(":components"))).thenReturn("Cached components");

        // When - Zweiter Call mit identischem Text sollte Cache verwenden
        Map<String, String> secondResult = aiService.analyzeText(testText);

        // Then
        assertNotNull(firstResult);
        assertNotNull(secondResult);
        assertEquals("Cached summary", secondResult.get("summary"));
        assertEquals("Cached keywords", secondResult.get("keywords"));
        assertEquals("Cached components", secondResult.get("suggestedComponents"));
    }
}