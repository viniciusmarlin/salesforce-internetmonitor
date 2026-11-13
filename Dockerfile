# Etapa 1: Compila o projeto com Maven e Java 21
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copia primeiro o pom.xml para aproveitar o cache de dependências do Docker
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código-fonte e gera o .jar
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Imagem final leve apenas com o JRE 21 para rodar a API
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia o JAR gerado na etapa anterior
COPY --from=build /app/target/internetping-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]