# Dockerfile para RobotCode Backend con Python support
FROM openjdk:17-jdk-slim

# Instalar Python y dependencias del sistema
RUN apt-get update && apt-get install -y \
    python3 \
    python3-pip \
    python3-dev \
    python3-venv \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

# Crear enlace simbólico para python3
RUN ln -s /usr/bin/python3 /usr/bin/python

# Instalar librerías de Python necesarias
RUN pip3 install --no-cache-dir \
    numpy \
    pandas \
    matplotlib \
    scikit-learn \
    scipy \
    seaborn \
    plotly

# Establecer directorio de trabajo
WORKDIR /app

# Copiar archivos del proyecto
COPY . .

# Hacer el wrapper de Maven ejecutable
RUN chmod +x ./mvnw

# Construir la aplicación
RUN ./mvnw clean package -DskipTests

# Exponer el puerto
EXPOSE 8080

# Variables de entorno
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE=prod
ENV PYTHONPATH=/usr/local/lib/python3.11/site-packages:/usr/lib/python3.11/site-packages
ENV PATH=/usr/bin:/bin:/usr/local/bin:$PATH

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "target/*.jar"]