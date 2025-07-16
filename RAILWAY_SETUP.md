# Configuración Manual en Railway Dashboard

## Variables de Entorno a Agregar:

1. Ve a tu proyecto en Railway
2. Ir a Variables tab
3. Agregar estas variables:

```
NIXPACKS_PYTHON_VERSION=3.11
PYTHONPATH=/usr/lib/python3.11/site-packages:/usr/local/lib/python3.11/site-packages
PATH=/usr/bin:/bin:/usr/local/bin
NIXPACKS_INSTALL_CMD=chmod +x install-python.sh && ./install-python.sh
```

## Forzar Rebuild:

1. Ir a Deployments tab
2. Hacer clic en "Redeploy"
3. Seleccionar "Clear cache and redeploy"

## Verificar Instalación:

Después del deploy, verificar en logs que aparezca:
```
🐍 Instalando Python...
✅ Python instalado correctamente
Python 3.11.x
```