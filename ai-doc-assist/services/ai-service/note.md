
# 📝 AI-Service Test Notes

## 1️⃣ Testarten

- **Unit Tests**  
  - Schnell, ohne Docker
  - Befehl:
    ```bash
    mvn test -DskipITs
    ```

- **Integration Tests (IT)**  
  - Starten Redis & ggf. andere Services via Testcontainers  
  - Lokal optional ausführen:
    ```bash
    mvn verify -P integration-test
    ```

- **E2E Tests**
  - Nutzen Eureka + Redis via Testcontainers  
  - **Mock-Eureka Image `quay.io/mock-eureka-server:1.0` ist nicht verfügbar**  
  - Lösung: Image ersetzen oder E2E nur in CI ausführen

---

## 2️⃣ Docker & Testcontainers Hinweise

- **Fehler Eureka-Image 404**
  - Ersetzen in `AiServiceE2ETest`:
    ```java
    static GenericContainer<?> eurekaServer = 
        new GenericContainer<>("springcloud/eureka:latest");
    ```

- **Redis Testcontainer Fehler**
  - Sicherstellen, dass Container vor Nutzung startet:
    ```java
    @BeforeAll
    static void startContainers() {
        redis.start();
    }
    ```

---

## 3️⃣ Laufzeitoptimierung

- **Lokal** nur Unit Tests:
  ```bash
  mvn test -DskipITs -DskipE2E
  ```
- **CI**: Alle Tests inkl. E2E laufen

---

## 4️⃣ Empfehlung für Workflow

- Lokale Entwicklung: **Unit + evtl. Integration Tests**
- CI/CD: **Alle Tests (Unit + IT + E2E)**  
- Dokumentation aller Teststrategien in `dev.md` im Root-Projekt
