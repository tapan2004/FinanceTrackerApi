# AS build stage to compile the Java application
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the pom.xml and download dependencies (cached if pom doesn't change)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests -B

# Final stage to run the application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the jar file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port the app runs on
EXPOSE 8090

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
