# Use Eclipse Temurin JDK 21 for building
FROM eclipse-temurin:24-jdk AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# Use Eclipse Temurin JRE 21 for runtime
FROM eclipse-temurin:24-jre
WORKDIR /app
COPY --from=build /app/target/devices_api-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
