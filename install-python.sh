#!/bin/bash

echo "🐍 Instalando Python..."

# Verificar si Python ya está instalado
if command -v python3 &> /dev/null; then
    echo "✅ Python3 ya está instalado"
    python3 --version
else
    echo "❌ Python3 no está disponible"
    exit 1
fi

# Instalar librerías de Python si pip está disponible
if command -v pip3 &> /dev/null; then
    echo "📦 Instalando librerías de Python..."
    pip3 install numpy pandas matplotlib scikit-learn scipy seaborn
    echo "✅ Librerías instaladas"
else
    echo "⚠️ pip3 no está disponible, saltando instalación de librerías"
fi

echo "✅ Script completado"
python3 --version