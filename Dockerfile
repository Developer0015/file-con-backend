FROM openjdk:17-jdk-slim

RUN apt-get update && apt-get install -y libreoffice

WORKDIR /app

COPY . .

RUN ./mvnw clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/*.jar"]