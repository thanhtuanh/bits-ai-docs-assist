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
