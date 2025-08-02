#!/bin/bash

# ============================================================================
# AI-Docs-Assist Microservice Structure Generator
# ============================================================================
# Dieses Script generiert automatisch die neue Microservice-Struktur
# und migriert den bestehenden Code sicher
# ============================================================================

set -e  # Exit on any error

# Farben für Output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Funktionen
print_header() {
    echo -e "${BLUE}============================================================================${NC}"
    echo -e "${BLUE} $1 ${NC}"
    echo -e "${BLUE}============================================================================${NC}"
}

print_step() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${CYAN}ℹ️  $1${NC}"
}

# Prüfen ob wir im richtigen Verzeichnis sind
check_directory() {
    if [[ ! -d "frontend" && ! -d "backend" ]]; then
        print_error "Bitte führen Sie das Script im Hauptverzeichnis von ai-docs-assist aus!"
        print_info "Das Verzeichnis sollte 'frontend' und 'backend' Ordner enthalten."
        exit 1
    fi
}

# Backup des bestehenden Backends
backup_existing_backend() {
    print_step "Sichere bestehenden Backend-Code..."
    
    if [[ -d "backend" ]]; then
        if [[ -d "legacy-backend" ]]; then
            print_warning "legacy-backend existiert bereits. Erstelle backup mit Timestamp..."
            TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
            mv backend "legacy-backend-$TIMESTAMP"
            print_info "Backend gesichert als: legacy-backend-$TIMESTAMP"
        else
            mv backend legacy-backend
            print_info "Backend gesichert als: legacy-backend"
        fi
    else
        print_warning "Kein 'backend' Ordner gefunden - überspringe Backup"
    fi
}

# Hauptstruktur erstellen
create_main_structure() {
    print_step "Erstelle Hauptordnerstruktur..."
    
    # Services Ordner
    mkdir -p services/{api-gateway,auth-service,document-service,storage-service,ai-service,notification-service,user-service}
    
    # Infrastructure Ordner
    mkdir -p infrastructure/{docker,k8s,monitoring,scripts}
    
    # Shared Libraries
    mkdir -p shared/{common-dto,exception-handling,security-config,utils}
    
    # Documentation
    mkdir -p docs/{api,architecture,deployment}
    
    # Scripts
    mkdir -p scripts/{development,deployment,maintenance}
    
    print_info "Hauptordnerstruktur erstellt"
}

# Maven Struktur für Services
create_service_structure() {
    local service_name=$1
    local service_path="services/$service_name"
    
    print_step "Erstelle Struktur für $service_name..."
    
    # Java Package Struktur
    mkdir -p "$service_path/src/main/java/com/aidocs/${service_name//-/}"
    mkdir -p "$service_path/src/main/java/com/aidocs/${service_name//-/}"/{config,controller,service,model,repository,client,exception,util}
    mkdir -p "$service_path/src/main/java/com/aidocs/${service_name//-/}/model"/{dto,entity}
    
    # Resources
    mkdir -p "$service_path/src/main/resources"/{db/migration,templates,static}
    
    # Test Struktur
    mkdir -p "$service_path/src/test/java/com/aidocs/${service_name//-/}"
    mkdir -p "$service_path/src/test/resources"
    
    # Service-spezifische Ordner
    case $service_name in
        "ai-service")
            mkdir -p "$service_path/src/main/resources/prompts"
            ;;
        "storage-service")
            mkdir -p "$service_path/src/main/resources/virus-definitions"
            ;;
        "notification-service")
            mkdir -p "$service_path/src/main/resources/email-templates"
            ;;
    esac
}

# Basis-Dateien erstellen
create_base_files() {
    print_step "Erstelle Basis-Dateien..."
    
    # Root POM.xml
    cat > pom.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.aidocs</groupId>
    <artifactId>ai-docs-assist-parent</artifactId>
    <version>1.0.0</version>
    <packaging>pom</packaging>
    
    <name>AI Docs Assist Microservices</name>
    <description>Parent POM for AI Document Assistant Microservices</description>
    
    <modules>
        <module>services/api-gateway</module>
        <module>services/auth-service</module>
        <module>services/document-service</module>
        <module>services/storage-service</module>
        <module>services/ai-service</module>
        <module>services/notification-service</module>
        <module>services/user-service</module>
        <module>shared/common-dto</module>
    </modules>
    
    <properties>
        <java.version>17</java.version>
        <spring-boot.version>3.2.0</spring-boot.version>
        <spring-cloud.version>2023.0.0</spring-cloud.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
    
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
    
    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-maven-plugin</artifactId>
                    <version>${spring-boot.version}</version>
                </plugin>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>3.11.0</version>
                    <configuration>
                        <source>${java.version}</source>
                        <target>${java.version}</target>
                    </configuration>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>
</project>
EOF

    # .gitignore
    cat > .gitignore << 'EOF'
# Compiled class file
*.class

# Log file
*.log

# BlueJ files
*.ctxt

# Mobile Tools for Java (J2ME)
.mtj.tmp/

# Package Files #
*.jar
*.war
*.nar
*.ear
*.zip
*.tar.gz
*.rar

# virtual machine crash logs
hs_err_pid*

# Maven
target/
pom.xml.tag
pom.xml.releaseBackup
pom.xml.versionsBackup
pom.xml.next
release.properties
dependency-reduced-pom.xml
buildNumber.properties
.mvn/timing.properties
.mvn/wrapper/maven-wrapper.jar

# IDE
.idea/
*.iws
*.iml
*.ipr
.vscode/
*.swp
*.swo
*~

# OS
.DS_Store
Thumbs.db

# Spring Boot
*.pid
*.pid.lock

# Docker
.dockerignore

# Environment variables
.env
.env.local
.env.*.local

# Logs
logs/
*.log

# Runtime data
pids
*.pid
*.seed
*.pid.lock

# Node modules (for frontend)
node_modules/
npm-debug.log*
yarn-debug.log*
yarn-error.log*

# Angular
dist/
.angular/
EOF

    # README.md
    cat > README.md << 'EOF'
# 🚀 AI-Docs-Assist Microservices

Ein vollständiges System zur AI-gestützten Dokumentenanalyse mit Microservice-Architektur.

## 📁 Projektstruktur

```
ai-docs-assist/
├── services/                   # Alle Microservices
│   ├── api-gateway/           # Zentraler Einstiegspunkt (Port 8080)
│   ├── auth-service/          # Authentifizierung (Port 8081)
│   ├── document-service/      # Dokumentenverwaltung (Port 8082)
│   ├── storage-service/       # Dateispeicher (Port 8083)
│   ├── ai-service/           # KI-Analyse (Port 8084)
│   ├── notification-service/  # Benachrichtigungen (Port 8085)
│   └── user-service/         # Benutzerverwaltung (Port 8086)
├── frontend/                  # Angular Anwendung
├── legacy-backend/           # Alter Monolith (Backup)
├── infrastructure/           # Docker, K8s, Monitoring
├── shared/                   # Gemeinsame Libraries
└── scripts/                  # Development & Deployment Scripts
```

## 🚀 Quick Start

### Development Environment
```bash
# Alle Services starten
./scripts/development/start-all.sh

# Einzelnen Service starten
cd services/ai-service
mvn spring-boot:run

# Mit Docker
cd infrastructure
docker-compose -f docker-compose.dev.yml up
```

### Production Deployment
```bash
# Build alle Services
mvn clean package

# Docker Build & Deploy
cd infrastructure
docker-compose -f docker-compose.prod.yml up -d
```

## 🔧 Service Endpoints

| Service | Port | Health Check | API Docs |
|---------|------|--------------|----------|
| API Gateway | 8080 | `/actuator/health` | `/swagger-ui.html` |
| Auth Service | 8081 | `/actuator/health` | `/swagger-ui.html` |
| Document Service | 8082 | `/actuator/health` | `/swagger-ui.html` |
| Storage Service | 8083 | `/actuator/health` | `/swagger-ui.html` |
| AI Service | 8084 | `/api/analysis/health` | `/swagger-ui.html` |
| Notification Service | 8085 | `/actuator/health` | `/swagger-ui.html` |
| User Service | 8086 | `/actuator/health` | `/swagger-ui.html` |

## 📋 Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- Node.js 18+ (für Frontend)
- OpenAI API Key (für KI-Funktionen)

## ⚙️ Konfiguration

```bash
# Environment Variables
export OPENAI_API_KEY=your_openai_key
export DB_PASSWORD=your_db_password
export JWT_SECRET=your_jwt_secret
```

## 🔄 Migration von Monolith

Der alte Monolith-Code befindet sich in `legacy-backend/` als Backup.
Die Migration erfolgt schrittweise durch Extraktion der Services.

## 🏗️ Architektur

- **API Gateway**: Zentraler Einstiegspunkt, Routing, Authentifizierung
- **Service Discovery**: Eureka für automatische Service-Registrierung
- **Database per Service**: PostgreSQL für Business Data, Redis für Cache
- **Message Queues**: RabbitMQ für asynchrone Kommunikation
- **Monitoring**: Prometheus + Grafana
- **Logging**: ELK Stack

## 📚 Dokumentation

- [API Dokumentation](docs/api/)
- [Architektur Übersicht](docs/architecture/)
- [Deployment Guide](docs/deployment/)

## 🤝 Contributing

1. Feature Branch erstellen
2. Änderungen implementieren
3. Tests hinzufügen
4. Pull Request erstellen

## 📞 Support

Bei Fragen oder Problemen:
- Issues auf GitHub erstellen
- Dokumentation in `docs/` prüfen
- Development Team kontaktieren
EOF
}

# Service-spezifische POM-Dateien
create_service_pom() {
    local service_name=$1
    local service_path="services/$service_name"
    local java_package="${service_name//-/}"
    
    # Spezielle Dependencies basierend auf Service
    local specific_deps=""
    case $service_name in
        "api-gateway")
            specific_deps='
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>'
            ;;
        "auth-service")
            specific_deps='
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-oauth2-resource-server</artifactId>
        </dependency>'
            ;;
        "document-service"|"user-service")
            specific_deps='
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>'
            ;;
        "ai-service")
            specific_deps='
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.retry</groupId>
            <artifactId>spring-retry</artifactId>
        </dependency>'
            ;;
        "storage-service")
            specific_deps='
        <dependency>
            <groupId>org.apache.pdfbox</groupId>
            <artifactId>pdfbox</artifactId>
            <version>2.0.27</version>
        </dependency>
        <dependency>
            <groupId>io.minio</groupId>
            <artifactId>minio</artifactId>
            <version>8.5.7</version>
        </dependency>'
            ;;
    esac

    cat > "$service_path/pom.xml" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.aidocs</groupId>
        <artifactId>ai-docs-assist-parent</artifactId>
        <version>1.0.0</version>
        <relativePath>../../pom.xml</relativePath>
    </parent>

    <artifactId>$service_name</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <name>$(echo ${service_name^} | sed 's/-/ /g') Service</name>
    <description>$(echo ${service_name^} | sed 's/-/ /g') Microservice for AI-Docs-Assist</description>

    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- Service Discovery -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>

        <!-- Service Communication -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>

        <!-- Monitoring -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
        </dependency>

        <!-- Utilities -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
$specific_deps

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
EOF
}

# Dockerfile für Services erstellen
create_dockerfile() {
    local service_name=$1
    local service_path="services/$service_name"
    local port
    
    # Port basierend auf Service
    case $service_name in
        "api-gateway") port="8080" ;;
        "auth-service") port="8081" ;;
        "document-service") port="8082" ;;
        "storage-service") port="8083" ;;
        "ai-service") port="8084" ;;
        "notification-service") port="8085" ;;
        "user-service") port="8086" ;;
        *) port="8080" ;;
    esac

    cat > "$service_path/Dockerfile" << EOF
FROM openjdk:17-jdk-slim

LABEL maintainer="AI-Docs-Assist Team"
LABEL description="$(echo ${service_name^} | sed 's/-/ /g') Service"

WORKDIR /app

# Copy the jar file
COPY target/$service_name-1.0.0.jar app.jar

# Expose port
EXPOSE $port

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \\
    CMD curl -f http://localhost:$port/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
EOF
}

# Application.yml für Services
create_application_yml() {
    local service_name=$1
    local service_path="services/$service_name"
    local port
    
    # Port basierend auf Service
    case $service_name in
        "api-gateway") port="8080" ;;
        "auth-service") port="8081" ;;
        "document-service") port="8082" ;;
        "storage-service") port="8083" ;;
        "ai-service") port="8084" ;;
        "notification-service") port="8085" ;;
        "user-service") port="8086" ;;
        *) port="8080" ;;
    esac

    cat > "$service_path/src/main/resources/application.yml" << EOF
server:
  port: $port

spring:
  application:
    name: $service_name

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    fetch-registry: true
    register-with-eureka: true
  instance:
    prefer-ip-address: true

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always

logging:
  level:
    com.aidocs.${service_name//-/}: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [%X{correlationId}] %logger{36} - %msg%n"
EOF

    # Docker-spezifische Konfiguration
    cat > "$service_path/src/main/resources/application-docker.yml" << EOF
eureka:
  client:
    service-url:
      defaultZone: http://eureka-server:8761/eureka/
EOF
}

# Main Application Class erstellen
create_main_class() {
    local service_name=$1
    local service_path="services/$service_name"
    local java_package="${service_name//-/}"
    local class_name="$(echo ${java_package^}Application)"
    
    cat > "$service_path/src/main/java/com/aidocs/$java_package/${class_name}.java" << EOF
package com.aidocs.$java_package;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class $class_name {

    public static void main(String[] args) {
        SpringApplication.run($class_name.class, args);
    }
}
EOF
}

# Docker Compose Dateien erstellen
create_docker_compose() {
    print_step "Erstelle Docker Compose Konfigurationen..."
    
    # Development Docker Compose
    cat > infrastructure/docker-compose.dev.yml << 'EOF'
version: '3.8'

services:
  # Infrastructure Services
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: ai_docs
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - ai-docs-network

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    networks:
      - ai-docs-network

  rabbitmq:
    image: rabbitmq:3-management
    environment:
      RABBITMQ_DEFAULT_USER: admin
      RABBITMQ_DEFAULT_PASS: admin
    ports:
      - "5672:5672"
      - "15672:15672"
    networks:
      - ai-docs-network

  minio:
    image: minio/minio
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin
    ports:
      - "9000:9000"
      - "9001:9001"
    volumes:
      - minio_data:/data
    command: server /data --console-address ":9001"
    networks:
      - ai-docs-network

  eureka-server:
    image: steeltoeoss/eureka-server
    ports:
      - "8761:8761"
    networks:
      - ai-docs-network

  # Microservices
  api-gateway:
    build: 
      context: ../services/api-gateway
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    depends_on:
      - eureka-server
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    networks:
      - ai-docs-network

  auth-service:
    build: 
      context: ../services/auth-service
      dockerfile: Dockerfile
    ports:
      - "8081:8081"
    depends_on:
      - postgres
      - eureka-server
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    networks:
      - ai-docs-network

  document-service:
    build: 
      context: ../services/document-service
      dockerfile: Dockerfile
    ports:
      - "8082:8082"
    depends_on:
      - postgres
      - eureka-server
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    networks:
      - ai-docs-network

  storage-service:
    build: 
      context: ../services/storage-service
      dockerfile: Dockerfile
    ports:
      - "8083:8083"
    depends_on:
      - minio
      - eureka-server
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    networks:
      - ai-docs-network

  ai-service:
    build: 
      context: ../services/ai-service
      dockerfile: Dockerfile
    ports:
      - "8084:8084"
    depends_on:
      - redis
      - eureka-server
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - OPENAI_API_KEY=${OPENAI_API_KEY}
    networks:
      - ai-docs-network

  notification-service:
    build: 
      context: ../services/notification-service
      dockerfile: Dockerfile
    ports:
      - "8085:8085"
    depends_on:
      - rabbitmq
      - eureka-server
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    networks:
      - ai-docs-network

  user-service:
    build: 
      context: ../services/user-service
      dockerfile: Dockerfile
    ports:
      - "8086:8086"
    depends_on:
      - postgres
      - eureka-server
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    networks:
      - ai-docs-network

volumes:
  postgres_data:
  minio_data:

networks:
  ai-docs-network:
    driver: bridge
EOF
}

# Development Scripts erstellen
create_dev_scripts() {
    print_step "Erstelle Development Scripts..."
    
    # Start All Script
    cat > scripts/development/start-all.sh << 'EOF'
#!/bin/bash

echo "🚀 Starting AI-Docs-Assist Development Environment"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Start infrastructure
echo -e "${YELLOW}📦 Starting infrastructure services...${NC}"
cd infrastructure/
docker-compose -f docker-compose.dev.yml up -d postgres redis rabbitmq minio eureka-server

# Wait for services to be ready
echo -e "${YELLOW}⏳ Waiting for services to start...${NC}"
sleep 30

echo -e "${GREEN}✅ Infrastructure ready!${NC}"
echo -e "${GREEN}🌐 Eureka Dashboard: http://localhost:8761${NC}"
echo -e "${GREEN}🗄️  MinIO Console: http://localhost:9001 (admin/admin)${NC}"
echo -e "${GREEN}🐰 RabbitMQ Management: http://localhost:15672 (admin/admin)${NC}"

echo -e "${YELLOW}💡 Start individual services with:${NC}"
echo "cd services/[service-name] && mvn spring-boot:run"
echo -e "${YELLOW}💡 Or start all services with Docker:${NC}"
echo "docker-compose -f infrastructure/docker-compose.dev.yml up --build"
EOF

    # Stop All Script
    cat > scripts/development/stop-all.sh << 'EOF'
#!/bin/bash

echo "🛑 Stopping AI-Docs-Assist Development Environment"

# Stop Docker services
cd infrastructure/
docker-compose -f docker-compose.dev.yml down

# Stop any running Spring Boot services
pkill -f "spring-boot:run"

echo "✅ All services stopped"
EOF

    # Build All Script
    cat > scripts/development/build-all.sh << 'EOF'
#!/bin/bash

echo "🏗️  Building All Microservices"

# Build from root (will build all modules)
mvn clean package -DskipTests

echo "✅ All services built successfully"
echo "💡 Run './scripts/development/start-all.sh' to start the development environment"
EOF

    # Test All Script
    cat > scripts/development/test-all.sh << 'EOF'
#!/bin/bash

echo "🧪 Running Tests for All Services"

# Run tests from root
mvn test

echo "✅ All tests completed"
EOF

    # Make scripts executable
    chmod +x scripts/development/*.sh
}

# Environment Template erstellen
create_env_template() {
    print_step "Erstelle Environment Template..."
    
    cat > .env.template << 'EOF'
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=ai_docs
DB_USERNAME=postgres
DB_PASSWORD=postgres

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379

# OpenAI Configuration
OPENAI_API_KEY=your_openai_api_key_here

# JWT Configuration
JWT_SECRET=your_jwt_secret_here
JWT_EXPIRATION=86400

# MinIO Configuration
MINIO_URL=http://localhost:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin

# RabbitMQ Configuration
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=admin
RABBITMQ_PASSWORD=admin

# Email Configuration (for Notification Service)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your_email@gmail.com
SMTP_PASSWORD=your_app_password

# Application Configuration
ENVIRONMENT=development
LOG_LEVEL=DEBUG
EOF

    print_info "Kopieren Sie .env.template zu .env und fügen Sie Ihre Konfiguration hinzu"
}

# Main Funktion
main() {
    print_header "AI-Docs-Assist Microservice Structure Generator"
    
    # Prüfungen
    check_directory
    
    # Backup
    backup_existing_backend
    
    # Struktur erstellen
    create_main_structure
    
    # Services erstellen
    print_header "Erstelle Microservices"
    SERVICES=("api-gateway" "auth-service" "document-service" "storage-service" "ai-service" "notification-service" "user-service")
    
    for service in "${SERVICES[@]}"; do
        create_service_structure "$service"
        create_service_pom "$service"
        create_dockerfile "$service"
        create_application_yml "$service"
        create_main_class "$service"
    done
    
    # Basis-Dateien
    print_header "Erstelle Konfigurationsdateien"
    create_base_files
    create_docker_compose
    create_dev_scripts
    create_env_template
    
    # Shared Module POM
    cat > shared/common-dto/pom.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.aidocs</groupId>
        <artifactId>ai-docs-assist-parent</artifactId>
        <version>1.0.0</version>
        <relativePath>../../pom.xml</relativePath>
    </parent>

    <artifactId>common-dto</artifactId>
    <packaging>jar</packaging>

    <name>Common DTOs</name>
    <description>Shared DTOs for all microservices</description>

    <dependencies>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>jakarta.validation</groupId>
            <artifactId>jakarta.validation-api</artifactId>
        </dependency>
    </dependencies>
</project>
EOF

    mkdir -p shared/common-dto/src/main/java/com/aidocs/common/dto
    
    print_header "Setup Complete!"
    
    echo -e "${GREEN}✅ Microservice-Struktur erfolgreich erstellt!${NC}"
    echo ""
    echo -e "${CYAN}📁 Projektstruktur:${NC}"
    echo "├── services/ (7 Microservices)"
    echo "├── frontend/ (unverändert)"
    echo "├── legacy-backend/ (Ihr alter Code)"
    echo "├── infrastructure/ (Docker, K8s)"
    echo "├── shared/ (Gemeinsame Libraries)"
    echo "└── scripts/ (Development Tools)"
    echo ""
    echo -e "${YELLOW}🚀 Nächste Schritte:${NC}"
    echo "1. cp .env.template .env && nano .env (Konfiguration anpassen)"
    echo "2. ./scripts/development/build-all.sh (Alle Services bauen)"
    echo "3. ./scripts/development/start-all.sh (Development Environment starten)"
    echo ""
    echo -e "${CYAN}📚 Nützliche Commands:${NC}"
    echo "• mvn clean package (Alle Services bauen)"
    echo "• cd services/ai-service && mvn spring-boot:run (Einzelnen Service starten)"
    echo "• docker-compose -f infrastructure/docker-compose.dev.yml up (Mit Docker starten)"
    echo ""
    echo -e "${GREEN}🎉 Viel Erfolg mit Ihrer Microservice-Architektur!${NC}"
}

# Script ausführen
main "$@"
EOF