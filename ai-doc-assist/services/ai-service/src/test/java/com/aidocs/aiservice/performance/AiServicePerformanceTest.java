package com.aidocs.aiservice.performance;

import com.aidocs.aiservice.service.AiService;
import com.aidocs.aiservice.testutil.TestDataFactory;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiServicePerformanceTest {

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
        
        // Kein API Key für Performance Tests (nutzt Fallback)
        ReflectionTestUtils.setField(aiService, "openAiApiKey", "");
        ReflectionTestUtils.setField(aiService, "model", "gpt-3.5-turbo-instruct");
    }

    @Test
    void testSingleAnalysis_Performance_ShouldCompleteWithinTimeout() {
        // Given
        String testText = TestDataFactory.TestTexts.GERMAN_TECHNICAL_TEXT;
        
        // Mock Cache Miss
        when(valueOperations.get(anyString())).thenReturn(null);

        // When & Then
        long executionTime = TestDataFactory.TimingHelper.measureExecutionTime(() -> {
            Map<String, String> result = aiService.analyzeText(testText);
            assertNotNull(result);
            assertTrue(TestDataFactory.ValidationHelper.isValidAnalysisResult(result));
        });

        // Fallback Analysis sollte unter 1 Sekunde dauern
        assertTrue(executionTime < 1000, "Analysis took too long: " + executionTime + "ms");
    }

    @Test
    void testLargeText_Performance_ShouldHandleEfficiently() {
        // Given
        String largeText = TestDataFactory.PerformanceTestData.createLargeText(10); // 10KB Text
        
        // Mock Cache Miss
        when(valueOperations.get(anyString())).thenReturn(null);

        // When
        long executionTime = TestDataFactory.TimingHelper.measureExecutionTime(() -> {
            Map<String, String> result = aiService.analyzeText(largeText);
            assertNotNull(result);
        });

        // Auch große Texte sollten schnell verarbeitet werden (Fallback)
        assertTrue(executionTime < 2000, "Large text analysis took too long: " + executionTime + "ms");
    }

    @Test
    void testConcurrentAnalysis_Performance_ShouldScaleWell() throws InterruptedException {
        // Given
        int numberOfThreads = 10;
        int analysesPerThread = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        
        // Mock Cache Miss für alle Requests
        when(valueOperations.get(anyString())).thenReturn(null);

        // When
        long startTime = System.currentTimeMillis();
        
        CompletableFuture<Void>[] futures = IntStream.range(0, numberOfThreads)
            .mapToObj(threadIndex -> CompletableFuture.runAsync(() -> {
                for (int i = 0; i < analysesPerThread; i++) {
                    String text = "Thread " + threadIndex + " Analysis " + i + " " + TestDataFactory.TestTexts.WEB_DEVELOPMENT_TEXT;
                    Map<String, String> result = aiService.analyzeText(text);
                    assertNotNull(result);
                }
            }, executor))
            .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
        long totalTime = System.currentTimeMillis() - startTime;

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        // Then
        int totalAnalyses = numberOfThreads * analysesPerThread;
        double averageTimePerAnalysis = (double) totalTime / totalAnalyses;
        
        assertTrue(averageTimePerAnalysis < 500, "Average analysis time too high: " + averageTimePerAnalysis + "ms");
        assertTrue(totalTime < 10000, "Total concurrent execution time too high: " + totalTime + "ms");
    }

    @Test
    void testCache_Performance_ShouldImproveWithCaching() {
        // Given
        String testText = TestDataFactory.TestTexts.DATABASE_TEXT;
        
        // Erstes Mal: Cache Miss
        when(valueOperations.get(anyString())).thenReturn(null);

        // When - Erste Analyse (ohne Cache)
        long firstAnalysisTime = TestDataFactory.TimingHelper.measureExecutionTime(() -> {
            Map<String, String> result = aiService.analyzeText(testText);
            assertNotNull(result);
        });

        // Setup Cache Hit für zweite Analyse
        when(valueOperations.get(anyString().contains(":summary") ? anyString() : ""))
            .thenReturn("Cached summary");
        when(valueOperations.get(anyString().contains(":keywords") ? anyString() : ""))
            .thenReturn("Cached keywords");  
        when(valueOperations.get(anyString().contains(":components") ? anyString() : ""))
            .thenReturn("Cached components");

        // When - Zweite Analyse (mit Cache)
        long cachedAnalysisTime = TestDataFactory.TimingHelper.measureExecutionTime(() -> {
            Map<String, String> result = aiService.analyzeText(testText);
            assertNotNull(result);
        });

        // Then - Cache sollte deutlich schneller sein
        assertTrue(cachedAnalysisTime < firstAnalysisTime, 
            "Cached analysis (" + cachedAnalysisTime + "ms) should be faster than first analysis (" + firstAnalysisTime + "ms)");
        assertTrue(cachedAnalysisTime < 100, "Cached analysis should be very fast: " + cachedAnalysisTime + "ms");
    }

    @Test
    void testMemoryUsage_LargeTexts_ShouldNotCauseMemoryLeak() {
        // Given
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // Mock Cache Miss
        when(valueOperations.get(anyString())).thenReturn(null);

        // When - Verarbeite viele große Texte
        for (int i = 0; i < 50; i++) {
            String largeText = TestDataFactory.PerformanceTestData.createLargeText(5); // 5KB per iteration
            Map<String, String> result = aiService.analyzeText(largeText);
            assertNotNull(result);
            
            // Gelegentlich Garbage Collection triggern
            if (i % 10 == 0) {
                System.gc();
                TestDataFactory.TimingHelper.waitForAsyncOperation(100);
            }
        }

        // Then - Memory usage sollte nicht explodieren
        System.gc();
        TestDataFactory.TimingHelper.waitForAsyncOperation(500);
        
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;
        
        // Memory increase sollte unter 50MB bleiben
        assertTrue(memoryIncrease < 50 * 1024 * 1024, 
            "Memory increase too high: " + (memoryIncrease / 1024 / 1024) + "MB");
    }

    @Test
    void testFallbackKeywordExtraction_Performance_ShouldScaleWithTextSize() {
        // Given
        String[] texts = {
            TestDataFactory.PerformanceTestData.createLargeText(1),   // 1KB
            TestDataFactory.PerformanceTestData.createLargeText(5),   // 5KB
            TestDataFactory.PerformanceTestData.createLargeText(10),  // 10KB
            TestDataFactory.PerformanceTestData.createLargeText(20)   // 20KB
        };
        
        // Mock Cache Miss
        when(valueOperations.get(anyString())).thenReturn(null);

        // When & Then
        long previousTime = 0;
        for (int i = 0; i < texts.length; i++) {
            final int index = i;
            long executionTime = TestDataFactory.TimingHelper.measureExecutionTime(() -> {
                Map<String, String> result = aiService.analyzeText(texts[index]);
                assertNotNull(result);
                assertNotNull(result.get("keywords"));
            });

            // Performance sollte mit Textgröße nicht exponentiell wachsen
            if (i > 0) {
                // Toleriere bis zu 3x Verlangsamung bei 2x Textgröße
                assertTrue(executionTime < previousTime * 3, 
                    "Performance degradation too high for text size " + (i + 1) + 
                    ": " + executionTime + "ms vs previous " + previousTime + "ms");
            }
            
            previousTime = executionTime;
        }
    }

    @Test
    void testCacheKeyGeneration_Performance_ShouldBeConstant() {
        // Given
        String[] texts = TestDataFactory.PerformanceTestData.createMultipleTexts(1000);
        
        // Mock alle als Cache Miss
        when(valueOperations.get(anyString())).thenReturn(null);

        // When
        long totalTime = TestDataFactory.TimingHelper.measureExecutionTime(() -> {
            for (String text : texts) {
                // Nur Cache-Key Generation testen (kein Full Analysis)
                Map<String, String> result = aiService.analyzeText(text.substring(0, Math.min(50, text.length())));
                assertNotNull(result);
            }
        });

        // Then
        double averageTimePerCacheKey = (double) totalTime / texts.length;
        assertTrue(averageTimePerCacheKey < 10, 
            "Cache key generation too slow: " + averageTimePerCacheKey + "ms per key");
    }

    @Test
    void testServiceStartup_Performance_ShouldInitializeQuickly() {
        // Given
        long startTime = System.currentTimeMillis();

        // When - Simuliere Service Initialization
        AiService newService = new AiService(restTemplate, redisTemplate);
        ReflectionTestUtils.setField(newService, "openAiApiKey", "");
        
        // Erste Analyse nach Initialization
        when(valueOperations.get(anyString())).thenReturn(null);
        Map<String, String> result = newService.analyzeText("Quick startup test");
        
        long initTime = System.currentTimeMillis() - startTime;

        // Then
        assertNotNull(result);
        assertTrue(initTime < 2000, "Service initialization too slow: " + initTime + "ms");
    }

    @Test
    void testStressTest_MultipleTextTypes_ShouldRemaineStable() {
        // Given
        String[] diverseTexts = {
            TestDataFactory.TestTexts.WEB_DEVELOPMENT_TEXT,
            TestDataFactory.TestTexts.DATABASE_TEXT,
            TestDataFactory.TestTexts.DEVOPS_TEXT,
            TestDataFactory.TestTexts.MOBILE_TEXT,
            TestDataFactory.TestTexts.GERMAN_TECHNICAL_TEXT,
            TestDataFactory.TestTexts.SPECIAL_CHARS_TEXT
        };
        
        // Mock Cache Miss
        when(valueOperations.get(anyString())).thenReturn(null);

        // When - Stress test mit verschiedenen Text-Typen
        long totalStartTime = System.currentTimeMillis();
        int iterations = 100;
        
        for (int i = 0; i < iterations; i++) {
            for (String text : diverseTexts) {
                Map<String, String> result = aiService.analyzeText(text);
                assertNotNull(result);
                assertTrue(TestDataFactory.ValidationHelper.isValidAnalysisResult(result));
            }
        }
        
        long totalTime = System.currentTimeMillis() - totalStartTime;

        // Then
        int totalAnalyses = iterations * diverseTexts.length;
        double averageTime = (double) totalTime / totalAnalyses;
        
        assertTrue(averageTime < 200, "Stress test average time too high: " + averageTime + "ms");
        assertTrue(totalTime < 60000, "Stress test took too long: " + totalTime + "ms");
    }
}