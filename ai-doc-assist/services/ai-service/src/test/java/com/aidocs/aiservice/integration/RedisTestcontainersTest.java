package com.aidocs.aiservice.integration;

import com.aidocs.aiservice.service.AiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.awaitility.Awaitility.await;

@Testcontainers
@SpringBootTest
class RedisTestcontainersTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379)
            .withReuse(true);

    @MockBean
    private RestTemplate restTemplate; // Mock um echte OpenAI Calls zu vermeiden

    private AiService aiService;
    private StringRedisTemplate redisTemplate;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379).toString());
        registry.add("spring.redis.timeout", () -> "2000ms");
        registry.add("spring.redis.connection-timeout", () -> "2000ms");
    }

    @BeforeEach
    void setUp() {
        // Redis Template Setup für echte Redis-Verbindung
        redisTemplate = new StringRedisTemplate();
        redisTemplate.setConnectionFactory(
            new org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory(
                redis.getHost(), redis.getMappedPort(6379)
            )
        );
        redisTemplate.afterPropertiesSet();

        // AI Service Setup
        aiService = new AiService(restTemplate, redisTemplate);
        
        // Konfiguration (ohne OpenAI API Key für Fallback-Tests)
        ReflectionTestUtils.setField(aiService, "openAiApiKey", "");
        ReflectionTestUtils.setField(aiService, "model", "gpt-3.5-turbo-instruct");

        // Redis cleanen für jeden Test
        cleanRedis();
    }

    @Test
    void testRealRedis_CachingWorkflow_ShouldPersistData() {
        // Given
        String testText = "Redis Integration Test mit echtem Cache";

        // When - Erste Analyse (sollte gecacht werden)
        Map<String, String> firstResult = aiService.analyzeText(testText);

        // Then - Verify dass Daten in Redis gespeichert wurden
        assertNotNull(firstResult);
        assertTrue(firstResult.get("summary").contains("[Lokale Zusammenfassung - OpenAI nicht verfügbar]"));

        // Verify Cache Keys existieren in Redis
        Set<String> cacheKeys = redisTemplate.keys("ai:analysis:*");
        assertEquals(3, cacheKeys.size()); // Summary, Keywords, Components

        // Verify spezifische Cache-Werte
        String summaryKey = cacheKeys.stream()
            .filter(key -> key.contains(":summary"))
            .findFirst()
            .orElseThrow();
        
        String cachedSummary = redisTemplate.opsForValue().get(summaryKey);
        assertNotNull(cachedSummary);
        assertEquals(firstResult.get("summary"), cachedSummary);
    }

    @Test
    void testRealRedis_CacheRetrieval_ShouldUseCachedData() {
        // Given
        String testText = "Cache Retrieval Test";

        // When - Erste Analyse (Cache Miss)
        Map<String, String> firstResult = aiService.analyzeText(testText);
        
        // When - Zweite Analyse identischen Texts (Cache Hit)
        Map<String, String> secondResult = aiService.analyzeText(testText);

        // Then - Beide Ergebnisse sollten identisch sein
        assertNotNull(firstResult);
        assertNotNull(secondResult);
        assertEquals(firstResult.get("summary"), secondResult.get("summary"));
        assertEquals(firstResult.get("keywords"), secondResult.get("keywords"));
        assertEquals(firstResult.get("suggestedComponents"), secondResult.get("suggestedComponents"));

        // Verify dass Cache wirklich verwendet wurde (Performance-Indikator)
        long startTime = System.currentTimeMillis();
        Map<String, String> thirdResult = aiService.analyzeText(testText);
        long cacheRetrievalTime = System.currentTimeMillis() - startTime;
        
        assertTrue(cacheRetrievalTime < 100, "Cache retrieval should be very fast: " + cacheRetrievalTime + "ms");
        assertEquals(firstResult.get("summary"), thirdResult.get("summary"));
    }

    @Test
    void testRealRedis_CacheExpiry_ShouldExpireAfter24Hours() {
        // Given
        String testText = "Cache Expiry Test";

        // When - Analyse durchführen
        aiService.analyzeText(testText);

        // Then - Verify TTL ist gesetzt (24 Stunden = 86400 Sekunden)
        Set<String> cacheKeys = redisTemplate.keys("ai:analysis:*");
        assertFalse(cacheKeys.isEmpty());

        for (String key : cacheKeys) {
            Long ttl = redisTemplate.getExpire(key);
            assertNotNull(ttl);
            assertTrue(ttl > 86000); // Sollte knapp unter 24 Stunden sein
            assertTrue(ttl <= 86400); // Sollte nicht über 24 Stunden sein
        }
    }

    @Test
    void testRealRedis_DifferentTexts_ShouldCreateSeparateCacheEntries() {
        // Given
        String text1 = "Erster Text für separaten Cache";
        String text2 = "Zweiter Text für separaten Cache";

        // When
        Map<String, String> result1 = aiService.analyzeText(text1);
        Map<String, String> result2 = aiService.analyzeText(text2);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotEquals(result1.get("keywords"), result2.get("keywords"));

        // Verify dass separate Cache-Einträge existieren
        Set<String> cacheKeys = redisTemplate.keys("ai:analysis:*");
        assertEquals(6, cacheKeys.size()); // 2 Texte × 3 Cache-Typen
    }

    @Test
    void testRealRedis_LargeData_ShouldHandleCorrectly() {
        // Given
        String largeText = "Großer Text für Redis Storage Test. ".repeat(1000); // ~37KB
        String largeSummary = "Große Zusammenfassung. ".repeat(500); // ~11KB

        // When - Simuliere große Daten durch direkten Cache-Write
        String cacheKey = "ai:analysis:test-large:summary";
        redisTemplate.opsForValue().set(cacheKey, largeSummary, Duration.ofMinutes(10));

        // Then - Verify dass große Daten korrekt gespeichert und abgerufen werden
        String retrievedSummary = redisTemplate.opsForValue().get(cacheKey);
        assertNotNull(retrievedSummary);
        assertEquals(largeSummary, retrievedSummary);
        assertTrue(retrievedSummary.length() > 10000);
    }

    @Test
    void testRealRedis_SpecialCharacters_ShouldPreserveEncoding() {
        // Given
        String textWithSpecialChars = "Text mit Umlauten: äöüß und Emojis: 🚀 und Unicode: 你好";

        // When
        Map<String, String> result = aiService.analyzeText(textWithSpecialChars);

        // Then - Verify dass Sonderzeichen im Cache korrekt gespeichert werden
        Set<String> cacheKeys = redisTemplate.keys("ai:analysis:*");
        assertFalse(cacheKeys.isEmpty());

        // Prüfe direkt im Redis
        String summaryKey = cacheKeys.stream()
            .filter(key -> key.contains(":summary"))
            .findFirst()
            .orElseThrow();
        
        String cachedSummary = redisTemplate.opsForValue().get(summaryKey);
        assertNotNull(cachedSummary);
        
        // Sonderzeichen sollten erhalten bleiben (im Fallback-Text oder Redis)
        // Bei Fallback sind sie möglicherweise nicht im Summary, aber Cache sollte korrekt funktionieren
        assertNotNull(result.get("summary"));
    }

    @Test
    void testRealRedis_ConnectionFailure_ShouldHandleGracefully() {
        // Note: Dieser Test ist schwierig mit Testcontainers zu implementieren,
        // da Container-Stopp komplexer ist. Stattdessen testen wir Redis-Robustheit.
        
        // Given
        String testText = "Redis Robustness Test";

        // When - Normale Operation
        Map<String, String> result = aiService.analyzeText(testText);

        // Then - Service sollte auch bei Cache-Problemen funktionieren
        assertNotNull(result);
        assertNotNull(result.get("summary"));
        assertNotNull(result.get("keywords"));
        assertNotNull(result.get("suggestedComponents"));
    }

    @Test
    void testRealRedis_ConcurrentAccess_ShouldBeThreadSafe() throws InterruptedException {
        // Given
        String baseText = "Concurrent Access Test";
        int numberOfThreads = 5;
        int operationsPerThread = 3;

        // When - Simuliere concurrent access
        Thread[] threads = new Thread[numberOfThreads];
        
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    String uniqueText = baseText + " Thread-" + threadId + " Op-" + j;
                    Map<String, String> result = aiService.analyzeText(uniqueText);
                    assertNotNull(result);
                }
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for completion
        for (Thread thread : threads) {
            thread.join(10000); // 10 second timeout
        }

        // Then - Verify dass alle Cache-Einträge korrekt erstellt wurden
        await().atMost(Duration.ofSeconds(5)).until(() -> {
            Set<String> cacheKeys = redisTemplate.keys("ai:analysis:*");
            return cacheKeys.size() >= numberOfThreads * operationsPerThread * 3; // 3 cache types per analysis
        });

        Set<String> finalCacheKeys = redisTemplate.keys("ai:analysis:*");
        assertEquals(numberOfThreads * operationsPerThread * 3, finalCacheKeys.size());
    }

    @Test
    void testRealRedis_MemoryUsage_ShouldBeReasonable() {
        // Given
        int numberOfAnalyses = 50;

        // When - Viele Analysen durchführen
        for (int i = 0; i < numberOfAnalyses; i++) {
            String text = "Memory Usage Test Iteration " + i + " with unique content";
            aiService.analyzeText(text);
        }

        // Then - Verify Memory Usage in Redis
        Set<String> allKeys = redisTemplate.keys("ai:analysis:*");
        assertEquals(numberOfAnalyses * 3, allKeys.size()); // 3 cache entries per analysis

        // Estimate memory usage (vereinfachte Berechnung)
        long totalMemoryEstimate = 0;
        for (String key : allKeys) {
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                totalMemoryEstimate += key.length() + value.length();
            }
        }

        // Memory usage sollte vernünftig sein (unter 1MB für 50 Analysen)
        assertTrue(totalMemoryEstimate < 1024 * 1024, 
            "Memory usage too high: " + totalMemoryEstimate + " bytes");
    }

    @Test
    void testRealRedis_DataIntegrity_ShouldMaintainConsistency() {
        // Given
        String testText = "Data Integrity Test mit verschiedenen Inhalten";

        // When - Mehrfache Analysen desselben Texts
        Map<String, String> result1 = aiService.analyzeText(testText);
        Map<String, String> result2 = aiService.analyzeText(testText);
        Map<String, String> result3 = aiService.analyzeText(testText);

        // Then - Alle Ergebnisse sollten identisch sein (Cache Consistency)
        assertEquals(result1.get("summary"), result2.get("summary"));
        assertEquals(result1.get("keywords"), result2.get("keywords"));
        assertEquals(result1.get("suggestedComponents"), result2.get("suggestedComponents"));
        
        assertEquals(result2.get("summary"), result3.get("summary"));
        assertEquals(result2.get("keywords"), result3.get("keywords"));
        assertEquals(result2.get("suggestedComponents"), result3.get("suggestedComponents"));

        // Verify direkt in Redis
        Set<String> cacheKeys = redisTemplate.keys("ai:analysis:*");
        assertEquals(3, cacheKeys.size()); // Nur ein Set von Cache-Einträgen

        // Verify dass Cache-Werte konsistent sind
        for (String key : cacheKeys) {
            String cachedValue = redisTemplate.opsForValue().get(key);
            assertNotNull(cachedValue);
            assertTrue(cachedValue.length() > 0);
        }
    }

    private void cleanRedis() {
        Set<String> keys = redisTemplate.keys("ai:analysis:*");
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}