FROM maven:3.9.6-eclipse-temurin-17

WORKDIR /workspace

# Copiamos pom primero para aprovechar cache
COPY pom.xml /workspace/pom.xml
RUN mvn -q -e -DskipTests dependency:go-offline

# Ahora copiamos el resto
COPY src /workspace/src

CMD ["mvn", "test"]