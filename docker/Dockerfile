FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY starter-common/pom.xml starter-common/
COPY starter-auth/pom.xml starter-auth/
COPY starter-ai/pom.xml starter-ai/
COPY starter-mcp/pom.xml starter-mcp/
COPY starter-agent/pom.xml starter-agent/
COPY starter-web/pom.xml starter-web/
COPY starter-demo/pom.xml starter-demo/
COPY starter-common/src starter-common/src
COPY starter-auth/src starter-auth/src
COPY starter-ai/src starter-ai/src
COPY starter-mcp/src starter-mcp/src
COPY starter-agent/src starter-agent/src
COPY starter-web/src starter-web/src
COPY starter-demo/src starter-demo/src
RUN mvn -q -pl starter-demo -am package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/starter-demo/target/starter-demo-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
