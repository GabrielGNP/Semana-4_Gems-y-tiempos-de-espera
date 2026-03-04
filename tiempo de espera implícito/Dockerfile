FROM gradle:8.5-jdk17

WORKDIR /app

# Copiar archivos de configuración de Gradle primero (para aprovechar cache de Docker)
COPY build.gradle settings.gradle ./

# Descargar dependencias (capa cacheada si no cambian las dependencias)
RUN gradle dependencies --no-daemon || true

# Copiar código fuente
COPY src/ src/

# Comando por defecto: ejecutar los tests
CMD ["gradle", "test", "--no-daemon", "--info"]
