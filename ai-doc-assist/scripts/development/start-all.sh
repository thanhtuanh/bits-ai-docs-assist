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
