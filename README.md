# 🤖 AI Document Assistant

Ein vollständiges System zur AI-gestützten Dokumentenanalyse mit Angular Frontend und Spring Boot Backend.

## 🚀 Quick Start

### Mit Docker (Empfohlen):
```bash
# 1. Repository klonen
git clone <repository-url>
cd bits-ai-docs-assist/ai-doc-assist

# 2. OpenAI API Key setzen
export OPENAI_API_KEY="your_actual_key"

# 3. Alle Services starten
docker-compose up --build

# 4. Anwendung öffnen
open http://localhost:4200
```

### Manuell:
```bash
# Backend starten
cd ai-doc-assist/backend
mvn spring-boot:run

# Frontend starten (neues Terminal)
cd ai-doc-assist/frontend
npm install
ng serve
```

## 📋 Features

- 📄 **Datei Upload**: PDF, TXT, DOC, DOCX, MD Unterstützung (max 10MB)
- 🤖 **AI Analyse**: GPT-3.5-turbo powered Zusammenfassungen
- 🔑 **Keyword Extraktion**: Automatische Schlüsselwort-Erkennung
- 🛠️ **Tech-Empfehlungen**: KI-basierte Technologie-Vorschläge
- 📱 **Responsive Design**: Moderne, benutzerfreundliche Oberfläche
- 💰 **Kostenoptimiert**: GPT-3.5-turbo für 90% Kostenersparnis vs GPT-4

## 🔧 Technologie Stack

**Frontend:**
- Angular 15
- TypeScript
- Bootstrap CSS
- Nginx (Production)

**Backend:**
- Spring Boot 2.7.0
- Java 17
- H2 In-Memory Database
- OpenAI GPT-3.5-turbo API
- PDFBox für PDF-Verarbeitung
- Maven Build System

**DevOps:**
- Docker & Docker Compose
- Multi-stage Builds
- Health Checks
- CORS konfiguriert

## 🌐 URLs

**Lokal:**
- Frontend: http://localhost:4200
- Backend: http://localhost:8080
- H2 Console: http://localhost:8080/h2-console

**Production (render.com):**
- Frontend: https://bits-ai-docs-assist-demo.onrender.com
- Backend API: https://bits-ai-docs-assist-demo.onrender.com/api

## 📊 API Endpoints

- `POST /api/documents/upload` - Dokument hochladen und analysieren
- `GET /api/documents` - Alle Dokumente abrufen
- `GET /api/documents/{id}` - Spezifisches Dokument abrufen
- `GET /actuator/health` - Health Check
- `GET /h2-console` - Database Console (nur Development)

## 🔑 Konfiguration

### Lokale Entwicklung:
```bash
# .env Datei erstellen
OPENAI_API_KEY=sk-proj-your-key-here
```

### Production (.env.prod):
```bash
ENVIRONMENT=production
API_FRONTEND_ORIGIN=https://bits-ai-docs-assist-demo.onrender.com
CORS_ALLOWED_ORIGINS=https://bits-ai-docs-assist-demo.onrender.com
OPENAI_API_KEY=sk-proj-your-key-here
OPENAI_MODEL=gpt-3.5-turbo
DEMO_MODE=true
SPRING_PROFILES_ACTIVE=production
```

## 📁 Projektstruktur

```
bits-ai-docs-assist/
├── ai-doc-assist/
│   ├── backend/              # Spring Boot API
│   │   ├── src/main/java/    # Java Source Code
│   │   ├── src/main/resources/ # Configuration Files
│   │   ├── Dockerfile        # Backend Container
│   │   └── pom.xml          # Maven Dependencies
│   ├── frontend/            # Angular App
│   │   ├── src/app/         # Angular Components
│   │   ├── Dockerfile       # Frontend Container
│   │   └── package.json     # NPM Dependencies
│   ├── docker-compose.yml   # Local Development
│   ├── docker-compose.prod.yml # Production
│   ├── deploy-render.sh     # Deployment Script
│   └── DEPLOYMENT.md        # Deployment Guide
├── assets/                  # Project Assets
├── test-tools/             # Testing Utilities
└── README.md               # Diese Datei
```

## 🚀 Deployment

### Render.com Deployment:
```bash
# 1. Production Environment laden
source .env.prod

# 2. Deployment ausführen
./ai-doc-assist/deploy-render.sh

# 3. Oder manuell
cd ai-doc-assist
docker-compose -f docker-compose.prod.yml up -d
```

Detaillierte Deployment-Anleitung: [DEPLOYMENT.md](ai-doc-assist/DEPLOYMENT.md)

## 🔍 Troubleshooting

### Port bereits belegt:
```bash
# Container stoppen
docker-compose down

# Port freigeben
lsof -ti:4200 | xargs kill -9
lsof -ti:8080 | xargs kill -9

# Neu starten
docker-compose up -d
```

### OpenAI API Fehler:
- ✅ API Key validieren: https://platform.openai.com/api-keys
- ✅ Rate Limits prüfen: https://platform.openai.com/usage
- ✅ Internetverbindung testen
- ✅ Model-Verfügbarkeit prüfen (gpt-3.5-turbo)

### CORS Probleme:
- ✅ Frontend URL in Backend CORS-Konfiguration prüfen
- ✅ Browser-Cache leeren
- ✅ Environment Variables prüfen

### Container Probleme:
```bash
# Logs anzeigen
docker-compose logs -f

# Container Status
docker-compose ps

# Neustart erzwingen
docker-compose down && docker-compose up --build
```

## 💡 Entwicklung

### Neue Features hinzufügen:
1. Backend: Controller → Service → Repository Pattern
2. Frontend: Component → Service → Model Pattern
3. Tests schreiben (JUnit + Jasmine)
4. Docker Images rebuilden

### Performance Optimierung:
- ✅ GPT-3.5-turbo für Kosteneffizienz
- ✅ Token-Limits konfiguriert (500 max)
- ✅ Caching implementiert
- ✅ Multi-stage Docker Builds

## 📈 Monitoring

### Kosten überwachen:
- OpenAI Usage Dashboard: https://platform.openai.com/usage
- AWS Q Developer Pro: $19/Monat
- Render.com: Pay-per-use

### Health Checks:
```bash
# Frontend
curl http://localhost:4200

# Backend
curl http://localhost:8080/actuator/health

# API Test
curl -X GET http://localhost:8080/api/documents
```

## 🤝 Contributing

1. Fork das Repository
2. Feature Branch erstellen: `git checkout -b feature/amazing-feature`
3. Änderungen committen: `git commit -m 'Add amazing feature'`
4. Branch pushen: `git push origin feature/amazing-feature`
5. Pull Request erstellen

## 📄 Lizenz

Dieses Projekt steht unter der MIT Lizenz - siehe [LICENSE](LICENSE) für Details.

## 🎯 Roadmap

- [ ] Benutzer-Authentifizierung
- [ ] Dokument-Versionierung
- [ ] Batch-Upload
- [ ] Export-Funktionen
- [ ] Advanced Analytics
- [ ] Multi-Language Support

---

**Viel Erfolg mit Ihrem AI Document Assistant!** 🎉

Für Support und Fragen: [Issues](https://github.com/your-repo/issues)
