FROM eclipse-temurin:17-jdk-jammy

RUN apt-get update && apt-get install -y libreoffice

WORKDIR /app

COPY . .

RUN ./mvnw clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/fileconverter-0.0.1-SNAPSHOT.jar"]