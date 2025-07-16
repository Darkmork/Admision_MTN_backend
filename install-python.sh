#!/bin/bash

echo "🐍 Instalando Python..."

# Actualizar sistema
apt-get update

# Instalar Python y dependencias
apt-get install -y python3 python3-pip python3-dev python3-venv

# Crear enlace simbólico
ln -sf /usr/bin/python3 /usr/bin/python

# Verificar instalación
python3 --version
pip3 --version

# Instalar librerías de Python
pip3 install numpy pandas matplotlib scikit-learn scipy seaborn

echo "✅ Python instalado correctamente"
python3 --version