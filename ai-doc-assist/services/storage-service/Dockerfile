FROM openjdk:17-jdk-slim

LABEL maintainer="AI-Docs-Assist Team"
LABEL description=" Service"

WORKDIR /app

# Copy the jar file
COPY target/storage-service-1.0.0.jar app.jar

# Expose port
EXPOSE 8083

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8083/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
