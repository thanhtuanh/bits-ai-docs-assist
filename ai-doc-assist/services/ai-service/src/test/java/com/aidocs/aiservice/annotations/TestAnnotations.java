package com.aidocs.aiservice.test.annotations;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

// ===============================
// UNIT TEST ANNOTATIONS
// ===============================

/**
 * Annotation für schnelle Unit Tests
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("unit")
@Timeout(value = 5, unit = TimeUnit.SECONDS)
public @interface UnitTest {
}

/**
 * Annotation für Service Layer Tests
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("unit")
@Tag("service")
@Timeout(value = 10, unit = TimeUnit.SECONDS)
public @interface ServiceTest {
}

/**
 * Annotation für Controller Layer Tests
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("unit")
@Tag("controller")
@Timeout(value = 10, unit = TimeUnit.SECONDS)
public @interface ControllerTest {
}

// ===============================
// INTEGRATION TEST ANNOTATIONS
// ===============================

/**
 * Annotation für Integration Tests
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("integration")
@Timeout(value = 30, unit = TimeUnit.SECONDS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public @interface IntegrationTest {
}

/**
 * Annotation für Redis Integration Tests
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("integration")
@Tag("redis")
@Timeout(value = 30, unit = TimeUnit.SECONDS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public @interface RedisIntegrationTest {
}

/**
 * Annotation für Database Integration Tests
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("integration")
@Tag("database")
@Timeout(value = 45, unit = TimeUnit.SECONDS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public @interface DatabaseIntegrationTest {
}

// ===============================
// PERFORMANCE TEST ANNOTATIONS
// ===============================

/**
 * Annotation für Performance Tests
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("performance")
@Timeout(value = 60, unit = TimeUnit.SECONDS)
public @interface PerformanceTest {
}

/**
 * Annotation für Load Tests
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("performance")
@Tag("load")
@Timeout(value = 120, unit = TimeUnit.SECONDS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("load-test")
public @interface LoadTest {
}

/**
 * Annotation für Stress Tests
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("performance")
@Tag("stress")
@Timeout(value = 180, unit = TimeUnit.SECONDS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("stress-test")
public @interface StressTest {
}

// ===============================
// E2E TEST ANNOTATIONS
// ===============================

/**
 * Annotation für End-to-End Tests
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("e2e")
@Timeout(value = 120, unit = TimeUnit.SECONDS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")
public @interface E2ETest {
}

/**
 * Annotation für Contract Tests
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("contract")
@Tag("api")
@Timeout(value = 30, unit = TimeUnit.SECONDS)
public @interface ContractTest {
}

// ===============================
// SECURITY TEST ANNOTATIONS
// ===============================

/**
 * Annotation für Security Tests
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("security")
@Timeout(value = 30, unit = TimeUnit.SECONDS)
public @interface SecurityTest {
}

/**
 * Annotation für Vulnerability Tests
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("security")
@Tag("vulnerability")
@Timeout(value = 60, unit = TimeUnit.SECONDS)
public @interface VulnerabilityTest {
}

// ===============================
// EXTERNAL INTEGRATION ANNOTATIONS
// ===============================

/**
 * Annotation für OpenAI Integration Tests
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("integration")
@Tag("external")
@Tag("openai")
@Timeout(value = 60, unit = TimeUnit.SECONDS)
public @interface OpenAIIntegrationTest {
}

/**
 * Annotation für WireMock Tests
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("integration")
@Tag("mock")
@Tag("wiremock")
@Timeout(value = 30, unit = TimeUnit.SECONDS)
public @interface WireMockTest {
}

// ===============================
// MONITORING TEST ANNOTATIONS
// ===============================

/**
 * Annotation für Health Check Tests
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("monitoring")
@Tag("health")
@Timeout(value = 15, unit = TimeUnit.SECONDS)
public @interface HealthCheckTest {
}

/**
 * Annotation für Metrics Tests
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("monitoring")
@Tag("metrics")
@Timeout(value = 15, unit = TimeUnit.SECONDS)
public @interface MetricsTest {
}

// ===============================
// CHAOS ENGINEERING ANNOTATIONS
// ===============================

/**
 * Annotation für Chaos Engineering Tests
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("chaos")
@Timeout(value = 180, unit = TimeUnit.SECONDS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("chaos")
public @interface ChaosTest {
}

/**
 * Annotation für Network Failure Tests
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("chaos")
@Tag("network")
@Timeout(value = 120, unit = TimeUnit.SECONDS)
public @interface NetworkChaosTest {
}

// ===============================
// SLOW TEST ANNOTATIONS
// ===============================

/**
 * Annotation für langsame Tests (excluded in standard runs)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("slow")
@Timeout(value = 300, unit = TimeUnit.SECONDS)
public @interface SlowTest {
}

/**
 * Annotation für Tests die nur lokal ausgeführt werden
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("local-only")
@Timeout(value = 60, unit = TimeUnit.SECONDS)
public @interface LocalOnlyTest {
}

// ===============================
// CONDITIONAL TEST ANNOTATIONS
// ===============================

/**
 * Meta-Annotation für Tests die nur mit OpenAI API Key laufen
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("requires-openai")
public @interface RequiresOpenAI {
}

/**
 * Meta-Annotation für Tests die nur mit Redis laufen
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("requires-redis")
public @interface RequiresRedis {
}

/**
 * Meta-Annotation für Tests die nur in Docker-Umgebung laufen
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("requires-docker")
public @interface RequiresDocker {
}

// ===============================
// TEST DATA ANNOTATIONS
// ===============================

/**
 * Annotation für Tests mit kleinen Testdaten
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("small-data")
public @interface SmallDataTest {
}

/**
 * Annotation für Tests mit großen Testdaten
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("large-data")
@Timeout(value = 120, unit = TimeUnit.SECONDS)
public @interface LargeDataTest {
}

// ===============================
// TEST SUITE ANNOTATIONS
// ===============================

/**
 * Annotation für Regression Tests
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("regression")
public @interface RegressionTest {
}

/**
 * Annotation für Smoke Tests
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("smoke")
@Timeout(value = 30, unit = TimeUnit.SECONDS)
public @interface SmokeTest {
}

/**
 * Annotation für Acceptance Tests
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("acceptance")
@Timeout(value = 60, unit = TimeUnit.SECONDS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("acceptance")
public @interface AcceptanceTest {
}

// ===============================
// DEPLOYMENT TEST ANNOTATIONS
// ===============================

/**
 * Annotation für Pre-Deployment Tests
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("pre-deployment")
@Timeout(value = 60, unit = TimeUnit.SECONDS)
public @interface PreDeploymentTest {
}

/**
 * Annotation für Post-Deployment Tests
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("post-deployment")
@Timeout(value = 60, unit = TimeUnit.SECONDS)
public @interface PostDeploymentTest {
}

// ===============================
// FLAKY TEST ANNOTATIONS
// ===============================

/**
 * Annotation für potenziell instabile Tests
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("flaky")
public @interface FlakyTest {
    String reason() default "";
    int maxRetries() default 3;
}

/**
 * Annotation für Tests die nur in CI/CD Pipeline laufen
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("ci-only")
public @interface CIOnlyTest {
}

// ===============================
// ENVIRONMENT SPECIFIC ANNOTATIONS
// ===============================

/**
 * Annotation für Development Environment Tests
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("dev-env")
@ActiveProfiles("dev")
public @interface DevEnvironmentTest {
}

/**
 * Annotation für Production-like Environment Tests
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("prod-like")
@ActiveProfiles("prod-like")
public @interface ProductionLikeTest {
}