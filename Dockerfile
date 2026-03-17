FROM eclipse-temurin:17-jdk-jammy

# Install LibreOffice
RUN apt-get update && apt-get install -y libreoffice

WORKDIR /app

# Copy project
COPY . .

# Build project
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

EXPOSE 8080

# 🔥 Run LibreOffice in background + Spring Boot
CMD libreoffice --headless --accept="socket,host=127.0.0.1,port=2002;urp;" & \
    java -jar target/*.jar