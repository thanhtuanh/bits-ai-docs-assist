# Build stage
FROM node:18-alpine AS build

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies with legacy peer deps to avoid conflicts
RUN npm install --legacy-peer-deps

# Copy source code
COPY . .

# Build the Angular app for production
RUN npm run build -- --configuration production

# Runtime stage
FROM nginx:alpine

# Remove default nginx static assets
RUN rm -rf /usr/share/nginx/html/*

# Copy built app to nginx (note: check your dist folder structure)
COPY --from=build /app/dist/ai-doc-assist-frontend /usr/share/nginx/html

# Copy custom nginx config if needed
# COPY nginx.conf /etc/nginx/nginx.conf

# Expose port
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]