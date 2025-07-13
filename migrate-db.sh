#!/bin/bash

echo "🚀 Iniciando migración de base de datos..."

# Esperar a que la base de datos esté lista
echo "⏳ Esperando a que la base de datos esté disponible..."
sleep 10

# Ejecutar migraciones
echo "📊 Ejecutando migraciones..."
./mvnw spring-boot:run -Dspring.profiles.active=prod -Dspring.jpa.hibernate.ddl-auto=update

echo "✅ Migración completada!" 