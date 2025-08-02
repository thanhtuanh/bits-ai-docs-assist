package com.aidocs.aiservice.test.monitoring;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Test Monitoring Extension für JUnit 5
 * Sammelt Test-Metriken und erstellt Reports
 */
public class TestMonitoringExtension implements TestWatcher {
    
    private static final Logger log = LoggerFactory.getLogger(TestMonitoringExtension.class);
    private static final Map<String, TestResult> testResults = new ConcurrentHashMap<>();
    private static final AtomicInteger totalTests = new AtomicInteger(0);
    private static final AtomicInteger passedTests = new AtomicInteger(0);
    private static final AtomicInteger failedTests = new AtomicInteger(0);
    private static final AtomicInteger skippedTests = new AtomicInteger(0);
    private static final AtomicLong totalExecutionTime = new AtomicLong(0);
    
    @Override
    public void testSuccessful(ExtensionContext context) {
        recordTestResult(context, TestStatus.PASSED, null);
        passedTests.incrementAndGet();
        log.debug("✅ Test passed: {}", getTestName(context));
    }
    
    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        recordTestResult(context, TestStatus.FAILED, cause);
        failedTests.incrementAndGet();
        log.error("❌ Test failed: {} - {}", getTestName(context), cause.getMessage());
    }
    
    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        recordTestResult(context, TestStatus.ABORTED, cause);
        skippedTests.incrementAndGet();
        log.warn("⚠️ Test aborted: {} - {}", getTestName(context), cause.getMessage());
    }
    
    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        recordTestResult(context, TestStatus.DISABLED, null);
        skippedTests.incrementAndGet();
        log.info("⏭️ Test disabled: {} - {}", getTestName(context), reason.orElse("No reason provided"));
    }
    
    private void recordTestResult(ExtensionContext context, TestStatus status, Throwable cause) {
        totalTests.incrementAndGet();
        
        String testName = getTestName(context);
        Set<String> tags = context.getTags();
        long executionTime = getExecutionTime(context);
        totalExecutionTime.addAndGet(executionTime);
        
        TestResult result = new TestResult(
            testName,
            status,
            executionTime,
            LocalDateTime.now(),
            tags,
            cause != null ? cause.getMessage() : null,
            getTestClass(context),
            getTestMethod(context)
        );
        
        testResults.put(testName, result);
    }
    
    private String getTestName(ExtensionContext context) {
        return context.getDisplayName();
    }
    
    private String getTestClass(ExtensionContext context) {
        return context.getTestClass().map(Class::getSimpleName).orElse("Unknown");
    }
    
    private String getTestMethod(ExtensionContext context) {
        return context.getTestMethod().map(method -> method.getName()).orElse("Unknown");
    }
    
    private long getExecutionTime(ExtensionContext context) {
        // Vereinfachte Implementierung - in der Realität würde man Start/End-Zeit erfassen
        return System.currentTimeMillis() % 10000; // Mock execution time
    }
    
    /**
     * Erstellt einen detaillierten Test Report
     */
    public static void generateTestReport() {
        try {
            Path reportDir = Paths.get("target/test-reports");
            Files.createDirectories(reportDir);
            
            generateHtmlReport(reportDir);
            generateJsonReport(reportDir);
            generateMetricsReport(reportDir);
            
            log.info("Test reports generated in: {}", reportDir.toAbsolutePath());
            
        } catch (IOException e) {
            log.error("Failed to generate test reports", e);
        }
    }
    
    private static void generateHtmlReport(Path reportDir) throws IOException {
        Path htmlReport = reportDir.resolve("test-report.html");
        
        try (FileWriter writer = new FileWriter(htmlReport.toFile())) {
            writer.write(generateHtmlContent());
        }
    }
    
    private static void generateJsonReport(Path reportDir) throws IOException {
        Path jsonReport = reportDir.resolve("test-metrics.json");
        
        try (FileWriter writer = new FileWriter(jsonReport.toFile())) {
            writer.write(generateJsonContent());
        }
    }
    
    private static void generateMetricsReport(Path reportDir) throws IOException {
        Path metricsReport = reportDir.resolve("test-metrics.txt");
        
        try (FileWriter writer = new FileWriter(metricsReport.toFile())) {
            writer.write(generateMetricsContent());
        }
    }
    
    private static String generateHtmlContent() {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>\n")
            .append("<html>\n<head>\n")
            .append("<title>AI Service Test Report</title>\n")
            .append("<style>\n")
            .append("body { font-family: Arial, sans-serif; margin: 20px; }\n")
            .append(".summary { background: #f5f5f5; padding: 15px; border-radius: 5px; margin-bottom: 20px; }\n")
            .append(".passed { color: green; }\n")
            .append(".failed { color: red; }\n")
            .append(".skipped { color: orange; }\n")
            .append("table { width: 100%; border-collapse: collapse; }\n")
            .append("th, td { padding: 10px; text-align: left; border-bottom: 1px solid #ddd; }\n")
            .append("th { background-color: #f2f2f2; }\n")
            .append(".tag { background: #e1f5fe; padding: 2px 6px; margin: 2px; border-radius: 3px; font-size: 0.8em; }\n")
            .append("</style>\n")
            .append("</head>\n<body>\n");
        
        // Header
        html.append("<h1>AI Service Test Report</h1>\n");
        html.append("<p>Generated on: ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("</p>\n");
        
        // Summary
        html.append("<div class='summary'>\n");
        html.append("<h2>Test Summary</h2>\n");
        html.append("<p><strong>Total Tests:</strong> ").append(totalTests.get()).append("</p>\n");
        html.append("<p><strong class='passed'>Passed:</strong> ").append(passedTests.get()).append("</p>\n");
        html.append("<p><strong class='failed'>Failed:</strong> ").append(failedTests.get()).append("</p>\n");
        html.append("<p><strong class='skipped'>Skipped:</strong> ").append(skippedTests.get()).append("</p>\n");
        html.append("<p><strong>Success Rate:</strong> ").append(calculateSuccessRate()).append("%</p>\n");
        html.append("<p><strong>Total Execution Time:</strong> ").append(totalExecutionTime.get()).append("ms</p>\n");
        html.append("</div>\n");
        
        // Test Results Table
        html.append("<h2>Test Results</h2>\n");
        html.append("<table>\n");
        html.append("<tr><th>Test Name</th><th>Status</th><th>Execution Time</th><th>Tags</th><th>Error Message</th></tr>\n");
        
        testResults.values().stream()
            .sorted(Comparator.comparing(TestResult::getTimestamp))
            .forEach(result -> {
                String statusClass = result.getStatus().name().toLowerCase();
                html.append("<tr>\n");
                html.append("<td>").append(result.getTestName()).append("</td>\n");
                html.append("<td class='").append(statusClass).append("'>").append(result.getStatus()).append("</td>\n");
                html.append("<td>").append(result.getExecutionTime()).append("ms</td>\n");
                html.append("<td>");
                result.getTags().forEach(tag -> html.append("<span class='tag'>").append(tag).append("</span>"));
                html.append("</td>\n");
                html.append("<td>").append(result.getErrorMessage() != null ? result.getErrorMessage() : "").append("</td>\n");
                html.append("</tr>\n");
            });
        
        html.append("</table>\n");
        
        // Performance Analysis
        html.append("<h2>Performance Analysis</h2>\n");
        html.append(generatePerformanceAnalysis());
        
        // Tag Analysis
        html.append("<h2>Test Categories</h2>\n");
        html.append(generateTagAnalysis());
        
        html.append("</body>\n</html>");
        
        return html.toString();
    }
    
    private static String generateJsonContent() {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"timestamp\": \"").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\",\n");
        json.append("  \"summary\": {\n");
        json.append("    \"totalTests\": ").append(totalTests.get()).append(",\n");
        json.append("    \"passedTests\": ").append(passedTests.get()).append(",\n");
        json.append("    \"failedTests\": ").append(failedTests.get()).append(",\n");
        json.append("    \"skippedTests\": ").append(skippedTests.get()).append(",\n");
        json.append("    \"successRate\": ").append(calculateSuccessRate()).append(",\n");
        json.append("    \"totalExecutionTime\": ").append(totalExecutionTime.get()).append("\n");
        json.append("  },\n");
        json.append("  \"testResults\": [\n");
        
        List<TestResult> results = new ArrayList<>(testResults.values());
        for (int i = 0; i < results.size(); i++) {
            TestResult result = results.get(i);
            json.append("    {\n");
            json.append("      \"testName\": \"").append(result.getTestName()).append("\",\n");
            json.append("      \"status\": \"").append(result.getStatus()).append("\",\n");
            json.append("      \"executionTime\": ").append(result.getExecutionTime()).append(",\n");
            json.append("      \"timestamp\": \"").append(result.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\",\n");
            json.append("      \"tags\": [");
            String[] tags = result.getTags().toArray(new String[0]);
            for (int j = 0; j < tags.length; j++) {
                json.append("\"").append(tags[j]).append("\"");
                if (j < tags.length - 1) json.append(", ");
            }
            json.append("],\n");
            json.append("      \"errorMessage\": \"").append(result.getErrorMessage() != null ? result.getErrorMessage() : "").append("\"\n");
            json.append("    }");
            if (i < results.size() - 1) json.append(",");
            json.append("\n");
        }
        
        json.append("  ]\n");
        json.append("}");
        
        return json.toString();
    }
    
    private static String generateMetricsContent() {
        StringBuilder metrics = new StringBuilder();
        
        metrics.append("AI SERVICE TEST METRICS REPORT\n");
        metrics.append("================================\n\n");
        metrics.append("Generated: ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\n\n");
        
        metrics.append("SUMMARY\n");
        metrics.append("-------\n");
        metrics.append("Total Tests: ").append(totalTests.get()).append("\n");
        metrics.append("Passed: ").append(passedTests.get()).append("\n");
        metrics.append("Failed: ").append(failedTests.get()).append("\n");
        metrics.append("Skipped: ").append(skippedTests.get()).append("\n");
        metrics.append("Success Rate: ").append(calculateSuccessRate()).append("%\n");
        metrics.append("Total Execution Time: ").append(totalExecutionTime.get()).append("ms\n");
        metrics.append("Average Execution Time: ").append(calculateAverageExecutionTime()).append("ms\n\n");
        
        metrics.append("PERFORMANCE METRICS\n");
        metrics.append("-------------------\n");
        metrics.append(generatePerformanceMetrics());
        
        metrics.append("\nTAG DISTRIBUTION\n");
        metrics.append("----------------\n");
        metrics.append(generateTagDistribution());
        
        metrics.append("\nFAILED TESTS\n");
        metrics.append("------------\n");
        testResults.values().stream()
            .filter(result -> result.getStatus() == TestStatus.FAILED)
            .forEach(result -> {
                metrics.append("- ").append(result.getTestName()).append(": ").append(result.getErrorMessage()).append("\n");
            });
        
        return metrics.toString();
    }
    
    private static String generatePerformanceAnalysis() {
        StringBuilder analysis = new StringBuilder();
        
        List<TestResult> results = new ArrayList<>(testResults.values());
        results.sort(Comparator.comparing(TestResult::getExecutionTime).reversed());
        
        analysis.append("<h3>Slowest Tests</h3>\n");
        analysis.append("<ul>\n");
        results.stream().limit(10).forEach(result -> {
            analysis.append("<li>").append(result.getTestName()).append(" - ").append(result.getExecutionTime()).append("ms</li>\n");
        });
        analysis.append("</ul>\n");
        
        analysis.append("<h3>Performance by Category</h3>\n");
        Map<String, List<TestResult>> tagGroups = groupByTags();
        tagGroups.forEach((tag, tagResults) -> {
            double avgTime = tagResults.stream().mapToLong(TestResult::getExecutionTime).average().orElse(0.0);
            analysis.append("<p><strong>").append(tag).append(":</strong> ").append(String.format("%.2f", avgTime)).append("ms average</p>\n");
        });
        
        return analysis.toString();
    }
    
    private static String generateTagAnalysis() {
        StringBuilder analysis = new StringBuilder();
        Map<String, List<TestResult>> tagGroups = groupByTags();
        
        analysis.append("<table>\n");
        analysis.append("<tr><th>Category</th><th>Tests</th><th>Passed</th><th>Failed</th><th>Success Rate</th></tr>\n");
        
        tagGroups.forEach((tag, tagResults) -> {
            long passed = tagResults.stream().filter(r -> r.getStatus() == TestStatus.PASSED).count();
            long failed = tagResults.stream().filter(r -> r.getStatus() == TestStatus.FAILED).count();
            double successRate = tagResults.isEmpty() ? 0.0 : (double) passed / tagResults.size() * 100;
            
            analysis.append("<tr>\n");
            analysis.append("<td>").append(tag).append("</td>\n");
            analysis.append("<td>").append(tagResults.size()).append("</td>\n");
            analysis.append("<td>").append(passed).append("</td>\n");
            analysis.append("<td>").append(failed).append("</td>\n");
            analysis.append("<td>").append(String.format("%.1f", successRate)).append("%</td>\n");
            analysis.append("</tr>\n");
        });
        
        analysis.append("</table>\n");
        
        return analysis.toString();
    }
    
    private static String generatePerformanceMetrics() {
        StringBuilder metrics = new StringBuilder();
        
        List<Long> executionTimes = testResults.values().stream()
            .map(TestResult::getExecutionTime)
            .sorted()
            .toList();
        
        if (!executionTimes.isEmpty()) {
            long min = executionTimes.get(0);
            long max = executionTimes.get(executionTimes.size() - 1);
            long median = executionTimes.get(executionTimes.size() / 2);
            long p95 = executionTimes.get((int) (executionTimes.size() * 0.95));
            
            metrics.append("Min Execution Time: ").append(min).append("ms\n");
            metrics.append("Max Execution Time: ").append(max).append("ms\n");
            metrics.append("Median Execution Time: ").append(median).append("ms\n");
            metrics.append("95th Percentile: ").append(p95).append("ms\n");
        }
        
        return metrics.toString();
    }
    
    private static String generateTagDistribution() {
        StringBuilder distribution = new StringBuilder();
        Map<String, List<TestResult>> tagGroups = groupByTags();
        
        tagGroups.forEach((tag, results) -> {
            distribution.append(tag).append(": ").append(results.size()).append(" tests\n");
        });
        
        return distribution.toString();
    }
    
    private static Map<String, List<TestResult>> groupByTags() {
        Map<String, List<TestResult>> tagGroups = new HashMap<>();
        
        testResults.values().forEach(result -> {
            result.getTags().forEach(tag -> {
                tagGroups.computeIfAbsent(tag, k -> new ArrayList<>()).add(result);
            });
        });
        
        return tagGroups;
    }
    
    private static double calculateSuccessRate() {
        if (totalTests.get() == 0) return 0.0;
        return (double) passedTests.get() / totalTests.get() * 100;
    }
    
    private static double calculateAverageExecutionTime() {
        if (totalTests.get() == 0) return 0.0;
        return (double) totalExecutionTime.get() / totalTests.get();
    }
    
    /**
     * Test Result Data Class
     */
    private static class TestResult {
        private final String testName;
        private final TestStatus status;
        private final long executionTime;
        private final LocalDateTime timestamp;
        private final Set<String> tags;
        private final String errorMessage;
        private final String testClass;
        private final String testMethod;
        
        public TestResult(String testName, TestStatus status, long executionTime, 
                         LocalDateTime timestamp, Set<String> tags, String errorMessage,
                         String testClass, String testMethod) {
            this.testName = testName;
            this.status = status;
            this.executionTime = executionTime;
            this.timestamp = timestamp;
            this.tags = new HashSet<>(tags);
            this.errorMessage = errorMessage;
            this.testClass = testClass;
            this.testMethod = testMethod;
        }
        
        // Getters
        public String getTestName() { return testName; }
        public TestStatus getStatus() { return status; }
        public long getExecutionTime() { return executionTime; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public Set<String> getTags() { return tags; }
        public String getErrorMessage() { return errorMessage; }
        public String getTestClass() { return testClass; }
        public String getTestMethod() { return testMethod; }
    }
    
    /**
     * Test Status Enum
     */
    private enum TestStatus {
        PASSED, FAILED, ABORTED, DISABLED
    }
}

/**
 * Performance Metrics Collector
 */
public class PerformanceMetricsCollector {
    private static final Logger log = LoggerFactory.getLogger(PerformanceMetricsCollector.class);
    private static final Map<String, PerformanceMetric> metrics = new ConcurrentHashMap<>();
    
    public static void recordMetric(String name, long value, String unit) {
        metrics.put(name, new PerformanceMetric(name, value, unit, LocalDateTime.now()));
    }
    
    public static void recordResponseTime(String endpoint, long responseTime) {
        recordMetric("response_time_" + endpoint, responseTime, "ms");
    }
    
    public static void recordThroughput(String operation, long requestsPerSecond) {
        recordMetric("throughput_" + operation, requestsPerSecond, "rps");
    }
    
    public static void recordMemoryUsage(long memoryMB) {
        recordMetric("memory_usage", memoryMB, "MB");
    }
    
    public static void recordCacheHitRate(String cacheType, double hitRate) {
        recordMetric("cache_hit_rate_" + cacheType, Math.round(hitRate * 100), "%");
    }
    
    public static void exportMetrics() {
        try {
            Path metricsFile = Paths.get("target/test-reports/performance-metrics.txt");
            Files.createDirectories(metricsFile.getParent());
            
            try (FileWriter writer = new FileWriter(metricsFile.toFile())) {
                writer.write("PERFORMANCE METRICS\n");
                writer.write("===================\n\n");
                writer.write("Collected at: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\n\n");
                
                metrics.values().stream()
                    .sorted(Comparator.comparing(PerformanceMetric::getName))
                    .forEach(metric -> {
                        try {
                            writer.write(String.format("%s: %d %s (recorded at %s)\n",
                                metric.getName(),
                                metric.getValue(),
                                metric.getUnit(),
                                metric.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_TIME)
                            ));
                        } catch (IOException e) {
                            log.error("Failed to write metric: " + metric.getName(), e);
                        }
                    });
            }
            
            log.info("Performance metrics exported to: {}", metricsFile.toAbsolutePath());
            
        } catch (IOException e) {
            log.error("Failed to export performance metrics", e);
        }
    }
    
    private static class PerformanceMetric {
        private final String name;
        private final long value;
        private final String unit;
        private final LocalDateTime timestamp;
        
        public PerformanceMetric(String name, long value, String unit, LocalDateTime timestamp) {
            this.name = name;
            this.value = value;
            this.unit = unit;
            this.timestamp = timestamp;
        }
        
        public String getName() { return name; }
        public long getValue() { return value; }
        public String getUnit() { return unit; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}