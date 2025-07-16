# Dockerfile para RobotCode backend con soporte Docker
FROM openjdk:17-jdk-slim

# Instalar Docker CLI
RUN apt-get update && apt-get install -y \
    apt-transport-https \
    ca-certificates \
    curl \
    gnupg \
    lsb-release \
    && curl -fsSL https://download.docker.com/linux/debian/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg \
    && echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/debian $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null \
    && apt-get update \
    && apt-get install -y docker-ce-cli \
    && rm -rf /var/lib/apt/lists/*

# Establecer directorio de trabajo
WORKDIR /app

# Copiar archivos del proyecto
COPY . .

# Dar permisos al wrapper de Maven
RUN chmod +x ./mvnw

# Construir la aplicación
RUN ./mvnw clean package -DskipTests

# Construir la imagen Python para el sandbox
RUN docker build -f Dockerfile.python -t robotcode-python:latest .

# Exponer puerto
EXPOSE 8080

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "target/RobotCode-0.0.1-SNAPSHOT.jar"]