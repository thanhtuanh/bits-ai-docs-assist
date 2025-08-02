# AI Service Test Suite - Vollständige Test-Befehle

# 1. Alle Tests ausführen
mvn clean test

# 2. Nur Unit Tests ausführen
mvn clean test -Dtest="*Test"

# 3. Nur Integration Tests ausführen
mvn clean test -Dtest="*IntegrationTest"

# 4. Performance Tests ausführen
mvn clean test -Dtest="*PerformanceTest"

# 5. Cache-spezifische Tests
mvn clean test -Dtest="RedisCacheTest"

# 6. Controller Tests
mvn clean test -Dtest="AiControllerTest"

# 7. Service Tests
mvn clean test -Dtest="AiServiceTest"

# 8. Tests mit Coverage Report
mvn clean test jacoco:report

# 9. Integration Tests mit Failsafe Plugin
mvn clean verify

# 10. Tests mit spezifischem Profil
mvn clean test -Ptest

# 11. Tests mit Debug-Output
mvn clean test -X

# 12. Parallele Test-Ausführung
mvn clean test -T 1C

# 13. Tests mit JVM-Argumenten für Performance
mvn clean test -DargLine="-Xmx2g -XX:+UseG1GC"

# 14. Spezifische Test-Methode ausführen
mvn clean test -Dtest="AiServiceTest#testAnalyzeText_WithCachedResult_ShouldReturnCache"

# 15. Tests mit Environment Variables
OPENAI_API_KEY="" mvn clean test

# 16. Tests überspringen
mvn clean compile -DskipTests

# 17. Nur fehlgeschlagene Tests wiederholen
mvn clean test -Dsurefire.rerunFailingTestsCount=2

# 18. Test-Report generieren
mvn clean test site

# 19. Kontinuierliche Tests (bei Datei-Änderungen)
mvn clean test -Dcontinuous

# 20. Tests mit Mock-Profil
mvn clean test -Dspring.profiles.active=test

# Test Coverage Befehle
# ==================

# Coverage Report anzeigen
mvn jacoco:report
# Report verfügbar unter: target/site/jacoco/index.html

# Coverage mit Minimum-Threshold prüfen
mvn clean test jacoco:check

# Coverage für Integration Tests
mvn clean verify jacoco:report

# Test Kategorien
# ==============

# Schnelle Tests (Unit Tests ohne Mocks)
mvn clean test -Dgroups="unit"

# Langsame Tests (Integration + Performance)
mvn clean test -Dgroups="integration,performance"

# Nur Fallback-Tests
mvn clean test -Dtest="*Test" -Dtest.openai.enabled=false

# Performance Monitoring
# =====================

# Tests mit JFR (Java Flight Recorder)
mvn clean test -DargLine="-XX:+FlightRecorder -XX:StartFlightRecording=duration=60s,filename=test-recording.jfr"

# Memory Usage Monitoring
mvn clean test -DargLine="-XX:+PrintGCDetails -XX:+PrintGCTimeStamps"

# Test-Datenbank Setup (falls Redis-Integration benötigt)
# ======================================================

# Docker Redis für Tests starten
docker run -d --name redis-test -p 6380:6379 redis:7-alpine

# Tests gegen echte Redis-Instanz
mvn clean test -Dspring.redis.port=6380

# Docker Redis stoppen
docker stop redis-test && docker rm redis-test

# IDE-spezifische Befehle
# ======================

# IntelliJ IDEA: Run Configuration
# VM Options: -Dspring.profiles.active=test -DOPENAI_API_KEY=""
# Program Arguments: 
# Environment Variables: SPRING_PROFILES_ACTIVE=test

# VS Code: launch.json Konfiguration
# {
#   "type": "java",
#   "name": "Test AI Service",
#   "request": "launch",
#   "mainClass": "com.aidocs.aiservice.AiServiceApplication",
#   "vmArgs": "-Dspring.profiles.active=test",
#   "env": {"OPENAI_API_KEY": ""}
# }

# Debugging Tests
# ==============

# Tests mit Debug-Port
mvn clean test -Dmaven.surefire.debug

# Spezifischen Test debuggen
mvn clean test -Dtest="AiServiceTest" -Dmaven.surefire.debug

# Test mit erhöhtem Logging
mvn clean test -Dlogging.level.com.aidocs.aiservice=TRACE

# Test-Reports und Ausgaben
# =========================

# Surefire Reports anzeigen
ls target/surefire-reports/

# Test-Logs anzeigen
tail -f target/surefire-reports/*.txt

# JaCoCo Coverage Report öffnen (macOS)
open target/site/jacoco/index.html

# JaCoCo Coverage Report öffnen (Linux)
xdg-open target/site/jacoco/index.html

# Test-Performance analysieren
# ============================

# Tests mit Zeitlimit
mvn clean test -Dsurefire.timeout=300

# Memory Leak Detection
mvn clean test -DargLine="-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=target/"

# CI/CD Pipeline Befehle
# ======================

# GitHub Actions / Jenkins
mvn clean verify -B -Dmaven.test.failure.ignore=false

# Docker Test Environment
docker-compose -f docker-compose.test.yml up -d
mvn clean test
docker-compose -f docker-compose.test.yml down

# Cleanup nach Tests
# ==================

# Test-Daten aufräumen
mvn clean

# Cache leeren
rm -rf ~/.m2/repository/com/aidocs/

# Docker Test-Container stoppen
docker stop $(docker ps -q --filter "name=*test*")

# Test-Metriken und Monitoring
# ============================

# Test-Execution Time
mvn clean test -Dtest.execution.listener=true

# Test-Coverage Trend
mvn clean test jacoco:report sonar:sonar

# Memory Usage während Tests
mvn clean test -DargLine="-javaagent:path/to/memory-agent.jar"