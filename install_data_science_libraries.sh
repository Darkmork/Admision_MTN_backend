#!/bin/bash

# Script para instalar librerías de ciencia de datos necesarias para RobotCode
# Este script debe ejecutarse en el servidor donde se ejecuta el backend

echo "🔧 Instalando librerías de ciencia de datos para RobotCode..."

# Verificar si pip3 está disponible
if ! command -v pip3 &> /dev/null; then
    echo "❌ Error: pip3 no está disponible. Instala Python3 y pip3 primero."
    exit 1
fi

# Lista de librerías esenciales para ciencia de datos y machine learning
LIBRARIES=(
    "numpy"
    "pandas"
    "matplotlib"
    "scikit-learn"
    "scipy"
    "seaborn"
    "plotly"
    "jupyter"
    "ipython"
)

echo "📦 Instalando librerías..."

for lib in "${LIBRARIES[@]}"; do
    echo "  📥 Instalando $lib..."
    if pip3 install --user "$lib" > /dev/null 2>&1; then
        echo "  ✅ $lib instalado correctamente"
    else
        echo "  ⚠️  No se pudo instalar $lib (puede que ya esté instalado)"
    fi
done

echo ""
echo "🔍 Verificando instalaciones..."

# Verificar que las librerías principales están disponibles
python3 -c "import numpy; print('✅ NumPy:', numpy.__version__)" 2>/dev/null || echo "❌ NumPy no disponible"
python3 -c "import pandas; print('✅ Pandas:', pandas.__version__)" 2>/dev/null || echo "❌ Pandas no disponible"
python3 -c "import matplotlib; print('✅ Matplotlib:', matplotlib.__version__)" 2>/dev/null || echo "❌ Matplotlib no disponible"
python3 -c "import sklearn; print('✅ Scikit-learn:', sklearn.__version__)" 2>/dev/null || echo "❌ Scikit-learn no disponible"

echo ""
echo "🎉 Instalación completada!"
echo "💡 Si algunas librerías no se instalaron, puedes instalarlas manualmente con:"
echo "   pip3 install --user <nombre_libreria>"
echo ""
echo "🚀 RobotCode ahora puede ejecutar problemas de ciencia de datos y machine learning." 