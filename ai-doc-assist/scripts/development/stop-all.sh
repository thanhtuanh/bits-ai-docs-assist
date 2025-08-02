#!/bin/bash

echo "🛑 Stopping AI-Docs-Assist Development Environment"

# Stop Docker services
cd infrastructure/
docker-compose -f docker-compose.dev.yml down

# Stop any running Spring Boot services
pkill -f "spring-boot:run"

echo "✅ All services stopped"
