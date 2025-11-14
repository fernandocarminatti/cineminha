# ---- BUILD STAGE ----
FROM maven:3.9.4-eclipse-temurin-20 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# ---- RUNTIME STAGE ----
FROM eclipse-temurin:21-jdk-jammy
# ffmpeg with common codecs (aac, eac3, opus)
RUN apt-get update && \
    apt-get install -y ffmpeg && \
    rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
# Entrypoint
ENTRYPOINT ["java","-jar","/app/app.jar"]