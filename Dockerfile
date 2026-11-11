FROM eclipse-temurin:21-jdk AS compilacao
WORKDIR /app
COPY .mvn .mvn
COPY mvnw mvnw.cmd pom.xml ./
COPY src ./src
RUN chmod +x mvnw && ./mvnw -B package

FROM eclipse-temurin:21-jre
WORKDIR /app
ENV TZ=America/Sao_Paulo
ENV JAVA_TOOL_OPTIONS="-Duser.timezone=America/Sao_Paulo"
ENV PORT=8080
COPY --from=compilacao /app/target/realimenta-sp.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
