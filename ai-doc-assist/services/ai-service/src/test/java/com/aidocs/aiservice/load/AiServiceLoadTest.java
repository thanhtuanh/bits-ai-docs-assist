package com.aidocs.aiservice.load;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
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
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("load-test")
class AiServiceLoadTest {

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

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379).toString());
        // Kein OpenAI API Key für Load Tests (nutzt Fallback)
        registry.add("openai.api.key", () -> "");
        // Optimierte Settings für Load Tests
        registry.add("server.tomcat.max-threads", () -> "200");
        registry.add("server.tomcat.min-spare-threads", () -> "20");
    }

    private String baseUrl;
    private HttpHeaders jsonHeaders;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    @Timeout(60) // Max 60 Sekunden für Load Test
    void loadTest_basicLoad_ShouldHandleModerateTraffic() throws InterruptedException {
        // Given - Moderate Load: 50 concurrent users, 5 requests each
        int numberOfUsers = 50;
        int requestsPerUser = 5;
        int totalRequests = numberOfUsers * requestsPerUser;
        
        ExecutorService executor = Executors.newFixedThreadPool(numberOfUsers);
        CountDownLatch latch = new CountDownLatch(totalRequests);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        AtomicLong totalResponseTime = new AtomicLong(0);
        List<Long> responseTimes = Collections.synchronizedList(new ArrayList<>());

        // When - Execute Load Test
        Instant startTime = Instant.now();
        
        for (int user = 0; user < numberOfUsers; user++) {
            final int userId = user;
            executor.submit(() -> {
                for (int request = 0; request < requestsPerUser; request++) {
                    try {
                        String text = "Load Test User-" + userId + " Request-" + request + 
                                     " analyzing Spring Boot microservice with Redis caching";
                        
                        long requestStart = System.currentTimeMillis();
                        ResponseEntity<Map> response = performAnalysisRequest(text);
                        long responseTime = System.currentTimeMillis() - requestStart;
                        
                        responseTimes.add(responseTime);
                        totalResponseTime.addAndGet(responseTime);
                        
                        if (response.getStatusCode() == HttpStatus.OK) {
                            successCount.incrementAndGet();
                        } else {
                            errorCount.incrementAndGet();
                        }
                        
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }

        // Wait for all requests to complete
        assertTrue(latch.await(45, TimeUnit.SECONDS), "Load test did not complete in time");
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));

        Instant endTime = Instant.now();
        Duration totalDuration = Duration.between(startTime, endTime);

        // Then - Analyze Results
        double successRate = (double) successCount.get() / totalRequests * 100;
        double averageResponseTime = (double) totalResponseTime.get() / totalRequests;
        double requestsPerSecond = (double) totalRequests / totalDuration.toSeconds();
        
        // Performance Assertions
        assertTrue(successRate >= 95.0, "Success rate too low: " + successRate + "%");
        assertTrue(averageResponseTime < 2000, "Average response time too high: " + averageResponseTime + "ms");
        assertTrue(requestsPerSecond > 10, "Throughput too low: " + requestsPerSecond + " RPS");
        assertTrue(errorCount.get() < totalRequests * 0.05, "Too many errors: " + errorCount.get());

        // Response Time Distribution
        responseTimes.sort(Long::compareTo);
        long p50 = responseTimes.get((int) (responseTimes.size() * 0.5));
        long p95 = responseTimes.get((int) (responseTimes.size() * 0.95));
        long p99 = responseTimes.get((int) (responseTimes.size() * 0.99));

        assertTrue(p50 < 1000, "P50 response time too high: " + p50 + "ms");
        assertTrue(p95 < 3000, "P95 response time too high: " + p95 + "ms");
        assertTrue(p99 < 5000, "P99 response time too high: " + p99 + "ms");

        System.out.printf("Load Test Results:\n" +
                         "- Total Requests: %d\n" +
                         "- Success Rate: %.2f%%\n" +
                         "- Average Response Time: %.2fms\n" +
                         "- Throughput: %.2f RPS\n" +
                         "- P50: %dms, P95: %dms, P99: %dms\n",
                         totalRequests, successRate, averageResponseTime, requestsPerSecond, p50, p95, p99);
    }

    @Test
    @Timeout(90) // Max 90 Sekunden für Stress Test
    void stressTest_highLoad_ShouldMaintainStability() throws InterruptedException {
        // Given - High Load: 100 concurrent users, 10 requests each
        int numberOfUsers = 100;
        int requestsPerUser = 10;
        int totalRequests = numberOfUsers * requestsPerUser;
        
        ExecutorService executor = Executors.newFixedThreadPool(numberOfUsers);
        CountDownLatch latch = new CountDownLatch(totalRequests);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        AtomicLong maxResponseTime = new AtomicLong(0);
        AtomicLong minResponseTime = new AtomicLong(Long.MAX_VALUE);

        // When - Execute Stress Test
        Instant startTime = Instant.now();
        
        for (int user = 0; user < numberOfUsers; user++) {
            final int userId = user;
            executor.submit(() -> {
                for (int request = 0; request < requestsPerUser; request++) {
                    try {
                        String text = "Stress Test User-" + userId + " Request-" + request + 
                                     " with complex microservice architecture using Spring Boot, " +
                                     "React frontend, PostgreSQL database, Redis cache, and Docker deployment";
                        
                        long requestStart = System.currentTimeMillis();
                        ResponseEntity<Map> response = performAnalysisRequest(text);
                        long responseTime = System.currentTimeMillis() - requestStart;
                        
                        // Update min/max response times
                        maxResponseTime.updateAndGet(current -> Math.max(current, responseTime));
                        minResponseTime.updateAndGet(current -> Math.min(current, responseTime));
                        
                        if (response.getStatusCode() == HttpStatus.OK) {
                            successCount.incrementAndGet();
                        } else {
                            errorCount.incrementAndGet();
                        }
                        
                        // Kleine zufällige Pause um realistischen Traffic zu simulieren
                        Thread.sleep(new Random().nextInt(100));
                        
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }

        // Wait for completion
        assertTrue(latch.await(75, TimeUnit.SECONDS), "Stress test did not complete in time");
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));

        Instant endTime = Instant.now();
        Duration totalDuration = Duration.between(startTime, endTime);

        // Then - Analyze Stress Test Results
        double successRate = (double) successCount.get() / totalRequests * 100;
        double requestsPerSecond = (double) totalRequests / totalDuration.toSeconds();
        
        // Stress Test Assertions (weniger streng als Load Test)
        assertTrue(successRate >= 90.0, "Success rate under stress too low: " + successRate + "%");
        assertTrue(requestsPerSecond > 5, "Throughput under stress too low: " + requestsPerSecond + " RPS");
        assertTrue(errorCount.get() < totalRequests * 0.1, "Too many errors under stress: " + errorCount.get());
        assertTrue(maxResponseTime.get() < 10000, "Max response time too high: " + maxResponseTime.get() + "ms");

        System.out.printf("Stress Test Results:\n" +
                         "- Total Requests: %d\n" +
                         "- Success Rate: %.2f%%\n" +
                         "- Throughput: %.2f RPS\n" +
                         "- Min/Max Response Time: %d/%dms\n",
                         totalRequests, successRate, requestsPerSecond, 
                         minResponseTime.get(), maxResponseTime.get());
    }

    @Test
    @Timeout(120) // Max 2 Minuten für Endurance Test
    void enduranceTest_sustainedLoad_ShouldMaintainPerformance() throws InterruptedException {
        // Given - Sustained Load über längeren Zeitraum
        int durationMinutes = 2; // Reduziert für Test-Umgebung
        int requestsPerSecond = 10;
        int intervalMs = 1000 / requestsPerSecond; // 100ms zwischen Requests
        int totalRequests = durationMinutes * 60 * requestsPerSecond;
        
        ExecutorService executor = Executors.newFixedThreadPool(20);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        List<Long> responseTimeSnapshots = Collections.synchronizedList(new ArrayList<>());

        // When - Execute Endurance Test
        Instant startTime = Instant.now();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
        
        // Performance Monitoring Task
        scheduler.scheduleAtFixedRate(() -> {
            try {
                long snapshotStart = System.currentTimeMillis();
                ResponseEntity<Map> response = performAnalysisRequest("Endurance test monitoring request");
                long responseTime = System.currentTimeMillis() - snapshotStart;
                responseTimeSnapshots.add(responseTime);
                
                if (response.getStatusCode() != HttpStatus.OK) {
                    System.err.println("Health check failed during endurance test");
                }
            } catch (Exception e) {
                System.err.println("Monitoring request failed: " + e.getMessage());
            }
        }, 0, 30, TimeUnit.SECONDS);

        // Main Load Generation
        CompletableFuture<Void> loadGeneration = CompletableFuture.runAsync(() -> {
            for (int i = 0; i < totalRequests; i++) {
                final int requestId = i;
                executor.submit(() -> {
                    try {
                        String text = "Endurance Test Request-" + requestId + 
                                     " testing sustained performance of AI service";
                        
                        ResponseEntity<Map> response = performAnalysisRequest(text);
                        
                        if (response.getStatusCode() == HttpStatus.OK) {
                            successCount.incrementAndGet();
                        } else {
                            errorCount.incrementAndGet();
                        }
                        
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                    }
                });
                
                try {
                    Thread.sleep(intervalMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        // Wait for completion
        loadGeneration.join();
        scheduler.shutdown();
        executor.shutdown();
        assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));

        Instant endTime = Instant.now();
        Duration actualDuration = Duration.between(startTime, endTime);

        // Then - Analyze Endurance Results
        double successRate = (double) successCount.get() / totalRequests * 100;
        double averageRPS = (double) totalRequests / actualDuration.toSeconds();
        
        // Endurance Test Assertions
        assertTrue(successRate >= 92.0, "Success rate during endurance test too low: " + successRate + "%");
        assertTrue(averageRPS >= requestsPerSecond * 0.8, "Throughput degraded too much: " + averageRPS + " RPS");
        
        // Performance Degradation Check
        if (responseTimeSnapshots.size() >= 2) {
            double firstHalfAvg = responseTimeSnapshots.subList(0, responseTimeSnapshots.size()/2)
                .stream().mapToLong(Long::longValue).average().orElse(0);
            double secondHalfAvg = responseTimeSnapshots.subList(responseTimeSnapshots.size()/2, responseTimeSnapshots.size())
                .stream().mapToLong(Long::longValue).average().orElse(0);
            
            double degradationFactor = secondHalfAvg / firstHalfAvg;
            assertTrue(degradationFactor < 2.0, "Performance degraded too much over time: " + degradationFactor + "x");
        }

        System.out.printf("Endurance Test Results:\n" +
                         "- Duration: %.2f minutes\n" +
                         "- Total Requests: %d\n" +
                         "- Success Rate: %.2f%%\n" +
                         "- Average RPS: %.2f\n",
                         actualDuration.toMinutes(), totalRequests, successRate, averageRPS);
    }

    @Test
    @Timeout(30)
    void spikeTest_suddenTrafficSpike_ShouldHandleGracefully() throws InterruptedException {
        // Given - Sudden Traffic Spike Simulation
        int baselineRequests = 20;
        int spikeRequests = 200;
        int totalRequests = baselineRequests + spikeRequests + baselineRequests;
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        List<Long> responseTimes = Collections.synchronizedList(new ArrayList<>());

        // When - Execute Spike Test
        
        // Phase 1: Baseline Load
        System.out.println("Phase 1: Baseline load...");
        ExecutorService baselineExecutor1 = Executors.newFixedThreadPool(5);
        executePhase(baselineExecutor1, baselineRequests, "Baseline-1", successCount, errorCount, responseTimes);
        
        // Phase 2: Traffic Spike
        System.out.println("Phase 2: Traffic spike...");
        ExecutorService spikeExecutor = Executors.newFixedThreadPool(50);
        executePhase(spikeExecutor, spikeRequests, "Spike", successCount, errorCount, responseTimes);
        
        // Phase 3: Return to Baseline
        System.out.println("Phase 3: Return to baseline...");
        ExecutorService baselineExecutor2 = Executors.newFixedThreadPool(5);
        executePhase(baselineExecutor2, baselineRequests, "Baseline-2", successCount, errorCount, responseTimes);

        // Then - Analyze Spike Test Results
        double successRate = (double) successCount.get() / totalRequests * 100;
        
        // Spike Test Assertions (允许更高的错误率)
        assertTrue(successRate >= 85.0, "Success rate during spike test too low: " + successRate + "%");
        assertTrue(errorCount.get() < totalRequests * 0.15, "Too many errors during spike: " + errorCount.get());
        
        // Verify service recovery
        ResponseEntity<Map> healthCheck = restTemplate.getForEntity(baseUrl + "/api/ai/health", Map.class);
        assertEquals(HttpStatus.OK, healthCheck.getStatusCode());
        assertEquals("UP", healthCheck.getBody().get("status"));

        System.out.printf("Spike Test Results:\n" +
                         "- Total Requests: %d\n" +
                         "- Success Rate: %.2f%%\n" +
                         "- Error Count: %d\n",
                         totalRequests, successRate, errorCount.get());
    }

    @Test
    @Timeout(45)
    void memoryLeakTest_prolongedUsage_ShouldNotLeakMemory() throws InterruptedException {
        // Given - Test für Memory Leaks
        int iterations = 500;
        int pauseBetweenIterations = 50; // ms
        
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        AtomicInteger successCount = new AtomicInteger(0);
        List<Long> memorySnapshots = new ArrayList<>();

        // When - Execute Memory Leak Test
        for (int i = 0; i < iterations; i++) {
            String largeText = "Memory leak test iteration " + i + ". " +
                              "Large text content to test memory usage. ".repeat(100);
            
            try {
                ResponseEntity<Map> response = performAnalysisRequest(largeText);
                if (response.getStatusCode() == HttpStatus.OK) {
                    successCount.incrementAndGet();
                }
                
                // Memory snapshot every 50 iterations
                if (i % 50 == 0) {
                    System.gc(); // Suggest garbage collection
                    Thread.sleep(100); // Wait for GC
                    long currentMemory = runtime.totalMemory() - runtime.freeMemory();
                    memorySnapshots.add(currentMemory);
                }
                
                Thread.sleep(pauseBetweenIterations);
                
            } catch (Exception e) {
                System.err.println("Request failed at iteration " + i + ": " + e.getMessage());
            }
        }

        // Force final garbage collection
        System.gc();
        Thread.sleep(500);
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();

        // Then - Analyze Memory Usage
        long memoryIncrease = finalMemory - initialMemory;
        double memoryIncreasePercent = (double) memoryIncrease / initialMemory * 100;
        
        // Memory Leak Assertions
        assertTrue(memoryIncreasePercent < 50.0, 
            "Memory increase too high, possible leak: " + memoryIncreasePercent + "%");
        assertTrue(successCount.get() >= iterations * 0.95, 
            "Too many failures during memory test: " + successCount.get() + "/" + iterations);

        // Memory Growth Trend Analysis
        if (memorySnapshots.size() >= 3) {
            long firstSnapshot = memorySnapshots.get(0);
            long lastSnapshot = memorySnapshots.get(memorySnapshots.size() - 1);
            double growthFactor = (double) lastSnapshot / firstSnapshot;
            
            assertTrue(growthFactor < 2.0, 
                "Memory growth factor too high: " + growthFactor + "x");
        }

        System.out.printf("Memory Leak Test Results:\n" +
                         "- Iterations: %d\n" +
                         "- Success Rate: %.2f%%\n" +
                         "- Memory Increase: %.2f%% (%d KB)\n",
                         iterations, (double) successCount.get() / iterations * 100,
                         memoryIncreasePercent, memoryIncrease / 1024);
    }

    // Helper Methods

    private ResponseEntity<Map> performAnalysisRequest(String text) {
        Map<String, String> request = Map.of("text", text);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, jsonHeaders);
        
        return restTemplate.exchange(
            baseUrl + "/api/ai/analyze",
            HttpMethod.POST,
            entity,
            Map.class
        );
    }

    private void executePhase(ExecutorService executor, int requestCount, String phaseName,
                             AtomicInteger successCount, AtomicInteger errorCount, 
                             List<Long> responseTimes) throws InterruptedException {
        
        CountDownLatch phaseLatch = new CountDownLatch(requestCount);
        
        for (int i = 0; i < requestCount; i++) {
            final int requestId = i;
            executor.submit(() -> {
                try {
                    String text = phaseName + " Request-" + requestId + 
                                 " testing phase behavior with Spring Boot analysis";
                    
                    long start = System.currentTimeMillis();
                    ResponseEntity<Map> response = performAnalysisRequest(text);
                    long responseTime = System.currentTimeMillis() - start;
                    
                    responseTimes.add(responseTime);
                    
                    if (response.getStatusCode() == HttpStatus.OK) {
                        successCount.incrementAndGet();
                    } else {
                        errorCount.incrementAndGet();
                    }
                    
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    phaseLatch.countDown();
                }
            });
        }
        
        assertTrue(phaseLatch.await(20, TimeUnit.SECONDS), 
            "Phase " + phaseName + " did not complete in time");
        
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
    }
}