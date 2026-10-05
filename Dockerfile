FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/target/simple-search-engine-1.0.0.jar app.jar
COPY --from=build /build/target/lib ./lib
CMD ["java", "-cp", "app.jar:lib/*", "com.github.franckteddev.search.Main"]