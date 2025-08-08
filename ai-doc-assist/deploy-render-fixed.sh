#!/bin/bash

# 🚀 Improved Render.com Deployment Script
# This script fixes the common deployment issues

set -e  # Exit on any error

echo "🔧 Starting Render.com deployment with fixes..."

# Check if required files exist
if [ ! -f "render.yaml" ]; then
    echo "❌ render.yaml not found!"
    exit 1
fi

if [ ! -f ".env.prod" ]; then
    echo "❌ .env.prod not found!"
    exit 1
fi

# Load production environment
source .env.prod

# Validate required environment variables
if [ -z "$OPENAI_API_KEY" ]; then
    echo "❌ OPENAI_API_KEY not set in .env.prod"
    exit 1
fi

echo "✅ Environment validation passed"

# Build frontend with production configuration
echo "🏗️ Building frontend..."
cd frontend
npm install
npm run build
if [ $? -ne 0 ]; then
    echo "❌ Frontend build failed!"
    exit 1
fi
cd ..

# Build backend
echo "🏗️ Building backend..."
cd backend
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "❌ Backend build failed!"
    exit 1
fi
cd ..

echo "✅ All builds successful"

# Test API endpoints locally before deployment
echo "🧪 Testing configuration..."

# Check if environment URLs are correctly configured
echo "Frontend API URL: $(grep apiUrl frontend/src/environments/environment.prod.ts)"
echo "Backend CORS: $(grep CORS_ALLOWED_ORIGINS render.yaml)"

echo "🚀 Ready for Render.com deployment!"
echo ""
echo "📋 Next steps:"
echo "1. Push changes to your Git repository"
echo "2. In Render.com dashboard, trigger manual deploy for both services"
echo "3. Check service logs for any errors"
echo "4. Test the application at your frontend URL"
echo ""
echo "🔗 Expected URLs:"
echo "- Frontend: https://bits-ai-docs-assist-frontend.onrender.com"
echo "- Backend: https://bits-ai-docs-assist-backend.onrender.com"
echo ""
echo "⚠️  Remember to set OPENAI_API_KEY in Render.com dashboard!"
