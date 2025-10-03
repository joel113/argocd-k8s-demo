FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
ARG JAR_FILE=target/argocd-demo-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
