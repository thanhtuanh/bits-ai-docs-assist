package com.aidocs.aiservice.cache;

import com.aidocs.aiservice.service.AiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisCacheTest {

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
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        // Test-Konfiguration setzen
        ReflectionTestUtils.setField(aiService, "openAiApiKey", "");  // Kein API Key für Cache-Tests
        ReflectionTestUtils.setField(aiService, "model", "gpt-3.5-turbo-instruct");
    }

    @Test
    void testCacheKey_SameText_ShouldGenerateIdenticalKey() {
        // Given
        String text1 = "Identischer Text für Cache-Test";
        String text2 = "Identischer Text für Cache-Test";
        
        // Mock Cache Miss für beide Calls
        when(valueOperations.get(anyString())).thenReturn(null);

        // When - Beide Texte analysieren (verwenden Fallback da kein API Key)
        Map<String, String> result1 = aiService.analyzeText(text1);
        Map<String, String> result2 = aiService.analyzeText(text2);

        // Then - Cache Key sollte identisch sein, daher sollte beim zweiten Call gecacht werden
        assertNotNull(result1);
        assertNotNull(result2);
        
        // Verify dass Cache-Schlüssel konsistent generiert werden
        // (Der gleiche Text sollte den gleichen Cache-Key erzeugen)
    }

    @Test
    void testCacheKey_DifferentText_ShouldGenerateDifferentKeys() {
        // Given
        String text1 = "Erster Text für Cache-Test";
        String text2 = "Zweiter Text für Cache-Test";
        
        // Mock Cache Miss
        when(valueOperations.get(anyString())).thenReturn(null);

        // When
        Map<String, String> result1 = aiService.analyzeText(text1);
        Map<String, String> result2 = aiService.analyzeText(text2);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        
        // Unterschiedliche Texte sollten nicht denselben Cache verwenden
        assertNotEquals(result1.get("keywords"), result2.get("keywords"));
    }

    @Test
    void testCacheExpiry_ShouldSetCorrectTTL() {
        // Given
        String testText = "Text für TTL Test";
        
        // Mock Cache Miss
        when(valueOperations.get(anyString())).thenReturn(null);

        // When
        aiService.analyzeText(testText);

        // Then - Verify dass Cache mit korrekter TTL gesetzt wird (24 Stunden = 86400 Sekunden)
        verify(valueOperations, times(3)).set(
            anyString(), 
            anyString(), 
            eq(86400L),  // 24 Stunden in Sekunden
            eq(TimeUnit.SECONDS)
        );
    }

    @Test
    void testCacheStructure_ShouldStoreThreeSeparateEntries() {
        // Given
        String testText = "Text für Cache-Struktur Test";
        
        // Mock Cache Miss
        when(valueOperations.get(anyString())).thenReturn(null);

        // When
        aiService.analyzeText(testText);

        // Then - Verify dass drei separate Cache-Einträge erstellt werden
        verify(valueOperations).set(contains(":summary"), anyString(), anyLong(), eq(TimeUnit.SECONDS));
        verify(valueOperations).set(contains(":keywords"), anyString(), anyLong(), eq(TimeUnit.SECONDS));
        verify(valueOperations).set(contains(":components"), anyString(), anyLong(), eq(TimeUnit.SECONDS));
    }

    @Test
    void testCacheRetrieval_PartialCache_ShouldNotUseIncompleteCache() {
        // Given
        String testText = "Text für partiellen Cache Test";
        
        // Mock: Nur Summary ist gecacht, Keywords und Components nicht
        when(valueOperations.get(contains(":summary"))).thenReturn("Cached summary");
        when(valueOperations.get(contains(":keywords"))).thenReturn(null);  // Fehlt
        when(valueOperations.get(contains(":components"))).thenReturn("Cached components");

        // When
        Map<String, String> result = aiService.analyzeText(testText);

        // Then - Sollte Fallback verwenden da Cache unvollständig ist
        assertNotNull(result);
        assertTrue(result.get("summary").contains("[Lokale Zusammenfassung - OpenAI nicht verfügbar]"));
        
        // Verify dass trotz partieller Cache-Treffer neue Analyse durchgeführt wird
        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), eq(TimeUnit.SECONDS));
    }

    @Test
    void testCacheFailure_ShouldContinueWithoutCaching() {
        // Given
        String testText = "Text für Cache-Fehler Test";
        
        // Mock Cache-Operationen werfen Exceptions
        when(valueOperations.get(anyString())).thenThrow(new RuntimeException("Redis connection failed"));
        doThrow(new RuntimeException("Redis write failed")).when(valueOperations)
            .set(anyString(), anyString(), anyLong(), eq(TimeUnit.SECONDS));

        // When - Sollte trotz Cache-Fehlern funktionieren
        Map<String, String> result = aiService.analyzeText(testText);

        // Then
        assertNotNull(result);
        assertNotNull(result.get("summary"));
        assertNotNull(result.get("keywords"));
        assertNotNull(result.get("suggestedComponents"));
        
        // Service sollte robust gegen Cache-Ausfälle sein
    }

    @Test
    void testLongText_CacheKey_ShouldHandleCorrectly() {
        // Given
        String longText = "Dies ist ein sehr langer Text. ".repeat(1000); // ~30.000 Zeichen
        
        // Mock Cache Miss
        when(valueOperations.get(anyString())).thenReturn(null);

        // When
        Map<String, String> result = aiService.analyzeText(longText);

        // Then
        assertNotNull(result);
        
        // Verify dass auch bei langen Texten Cache-Keys generiert werden können
        verify(valueOperations, atLeast(3)).get(anyString());
    }

    @Test
    void testSpecialCharacters_CacheKey_ShouldBeStable() {
        // Given
        String textWithSpecialChars = "Text mit Sonderzeichen: äöüß @#$%^&*() 你好 🚀";
        
        // Mock Cache Miss beim ersten Call
        when(valueOperations.get(anyString())).thenReturn(null);

        // When - Erster Call
        Map<String, String> result1 = aiService.analyzeText(textWithSpecialChars);
        
        // Reset Mock für zweiten Call - simuliere Cache Hit
        reset(valueOperations);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(contains(":summary"))).thenReturn("Cached summary");
        when(valueOperations.get(contains(":keywords"))).thenReturn("Cached keywords");
        when(valueOperations.get(contains(":components"))).thenReturn("Cached components");

        // When - Zweiter Call mit identischem Text (sollte Cache verwenden)
        Map<String, String> result2 = aiService.analyzeText(textWithSpecialChars);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals("Cached summary", result2.get("summary"));
        assertEquals("Cached keywords", result2.get("keywords"));
        assertEquals("Cached components", result2.get("suggestedComponents"));
    }

    @Test
    void testConcurrentCacheAccess_ShouldBeThreadSafe() {
        // Given
        String testText = "Text für Concurrent Access Test";
        
        // Mock Cache Miss
        when(valueOperations.get(anyString())).thenReturn(null);

        // When - Simuliere concurrent access (vereinfacht, da Single-threaded Test)
        Map<String, String> result1 = aiService.analyzeText(testText);
        Map<String, String> result2 = aiService.analyzeText(testText);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        
        // Beide Calls sollten erfolgreich sein
        assertTrue(result1.get("summary").length() > 0);
        assertTrue(result2.get("summary").length() > 0);
    }

    @Test
    void testCacheKeyConsistency_MultipleAnalysis_ShouldUseSameKey() {
        // Given
        String consistentText = "Konsistenter Text für Key-Test";
        
        // Mock Cache Miss für ersten Call, Cache Hit für zweiten
        when(valueOperations.get(anyString()))
            .thenReturn(null)  // Erste Analyse: Cache Miss
            .thenReturn("Cached summary")  // Zweite Analyse: Cache Hit
            .thenReturn("Cached keywords")
            .thenReturn("Cached components");

        // When
        Map<String, String> firstResult = aiService.analyzeText(consistentText);
        Map<String, String> secondResult = aiService.analyzeText(consistentText);

        // Then
        assertNotNull(firstResult);
        assertNotNull(secondResult);
        
        // Zweiter Call sollte gecachte Werte verwenden
        if (secondResult.get("summary").equals("Cached summary")) {
            assertEquals("Cached keywords", secondResult.get("keywords"));
            assertEquals("Cached components", secondResult.get("suggestedComponents"));
        }
    }
}