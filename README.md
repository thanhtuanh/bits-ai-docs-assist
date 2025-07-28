# 🤖 AI Document Assistant

Ein vollständiges System zur AI-gestützten Dokumentenanalyse mit Angular Frontend und Spring Boot Backend.

## 🚀 Quick Start

### Mit Docker (Empfohlen):
```bash
# 1. OpenAI API Key in .env Datei setzen
echo "OPENAI_API_KEY=your_actual_key" > .env

# 2. Alle Services starten
docker-compose up --build

# 3. Anwendung öffnen
open http://localhost:4200
```

### Manuell:
```bash
# Backend starten
cd backend
mvn spring-boot:run

# Frontend starten (neues Terminal)
cd frontend
npm install
ng serve
```

## 📋 Features

- 📄 **Datei Upload**: PDF, TXT, DOC Unterstützung
- 🤖 **AI Analyse**: GPT-3.5 powered Zusammenfassungen
- 🔑 **Keyword Extraktion**: Automatische Schlüsselwort-Erkennung
- 🛠️ **Tech-Empfehlungen**: KI-basierte Technologie-Vorschläge
- 📱 **Responsive Design**: Moderne, benutzerfreundliche Oberfläche

## 🔧 Technologie Stack

**Frontend:**
- Angular 15
- TypeScript
- Bootstrap CSS
- Nginx (Production)

**Backend:**
- Spring Boot 3.2
- Java 17
- H2 Database
- OpenAI GPT-3.5-turbo
- PDFBox für PDF-Verarbeitung

## 📊 Endpoints

- `POST /api/documents` - Dokument hochladen
- `GET /api/documents/{id}` - Dokument abrufen
- `GET /h2-console` - Database Console

## 🔑 Konfiguration

Setzen Sie Ihren OpenAI API Key in:
- `.env` Datei für Docker
- `backend/src/main/resources/application.properties` für lokale Entwicklung

## 📁 Projektstruktur

```
ai-doc-assist/
├── backend/           # Spring Boot API
├── frontend/          # Angular App
├── docker-compose.yml # Docker Configuration
└── README.md         # Diese Datei
```

## 🔍 Troubleshooting

**Port bereits belegt:**
```bash
docker-compose down
lsof -ti:8080 | xargs kill -9
```

**OpenAI API Fehler:**
- API Key validieren
- Rate Limits prüfen
- Internetverbindung testen

Viel Erfolg mit Ihrem AI Document Assistant! 🎉
