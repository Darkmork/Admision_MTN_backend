#!/bin/bash

echo "🚀 Iniciando RobotCode Backend..."

# Verificar que estamos en el directorio correcto
echo "📁 Directorio actual: $(pwd)"
echo "📋 Contenido del directorio:"
ls -la

# Verificar si existe el JAR
if [ -f "target/*.jar" ]; then
    echo "✅ JAR encontrado"
    ls -la target/*.jar
else
    echo "❌ JAR no encontrado, compilando..."
    ./mvnw clean install -DskipTests
fi

# Configurar variables de entorno
export SPRING_PROFILES_ACTIVE=prod
export SERVER_PORT=${PORT:-8080}
export SERVER_ADDRESS=0.0.0.0

echo "🔧 Configuración:"
echo "   - Puerto: $SERVER_PORT"
echo "   - Perfil: $SPRING_PROFILES_ACTIVE"
echo "   - Dirección: $SERVER_ADDRESS"

# Iniciar la aplicación
echo "🚀 Iniciando aplicación..."
if [ -f "target/*.jar" ]; then
    java -jar target/*.jar
else
    ./mvnw spring-boot:run -Dspring.profiles.active=prod
fi 