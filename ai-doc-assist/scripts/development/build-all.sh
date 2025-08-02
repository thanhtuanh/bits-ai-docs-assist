#!/bin/bash

echo "🏗️  Building All Microservices"

# Build from root (will build all modules)
mvn clean package -DskipTests

echo "✅ All services built successfully"
echo "💡 Run './scripts/development/start-all.sh' to start the development environment"
