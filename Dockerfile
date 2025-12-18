FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the uberjar
COPY target/mini-htmx-0.1.0-standalone.jar /app/app.jar

# Expose the default port
EXPOSE 8181

# Run the application
CMD ["java", "-jar", "/app/app.jar", "8181"]
