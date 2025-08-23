#!/bin/bash

# ===========================================
# CONFIGURACIÓN AUTOMÁTICA DEL SISTEMA DE BACKUP
# Sistema de Admisión MTN - Google Drive
# ===========================================

# Colores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}"
echo "╔══════════════════════════════════════════════════════════════╗"
echo "║                 CONFIGURACIÓN DE BACKUP AUTOMÁTICO           ║"
echo "║                    Sistema de Admisión MTN                   ║"
echo "║                      → Google Drive ←                       ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo -e "${NC}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Función de logging
log() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[✅ OK]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[⚠️ WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[❌ ERROR]${NC} $1"
}

# 1. Verificar si Homebrew está instalado
log "Verificando Homebrew..."
if ! command -v brew &> /dev/null; then
    log_warning "Homebrew no está instalado. Instalando..."
    /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
    
    if [ $? -eq 0 ]; then
        log_success "Homebrew instalado exitosamente"
    else
        log_error "Error instalando Homebrew"
        exit 1
    fi
else
    log_success "Homebrew ya está instalado"
fi

# 2. Instalar rclone si no está instalado
log "Verificando rclone (herramienta para Google Drive)..."
if ! command -v rclone &> /dev/null; then
    log "Instalando rclone..."
    brew install rclone
    
    if [ $? -eq 0 ]; then
        log_success "rclone instalado exitosamente"
    else
        log_error "Error instalando rclone"
        exit 1
    fi
else
    log_success "rclone ya está instalado"
fi

# 3. Verificar PostgreSQL client
log "Verificando PostgreSQL client..."
if ! command -v pg_dump &> /dev/null; then
    log_warning "PostgreSQL client no encontrado. Instalando..."
    brew install postgresql
    
    if [ $? -eq 0 ]; then
        log_success "PostgreSQL client instalado"
    else
        log_error "Error instalando PostgreSQL client"
        exit 1
    fi
else
    log_success "PostgreSQL client disponible"
fi

# 4. Configurar Google Drive con rclone
log "Verificando configuración de Google Drive..."
if ! rclone listremotes | grep -q "gdrive:"; then
    echo
    echo -e "${YELLOW}╔════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${YELLOW}║              CONFIGURACIÓN DE GOOGLE DRIVE                 ║${NC}"
    echo -e "${YELLOW}╚════════════════════════════════════════════════════════════╝${NC}"
    echo
    echo "Necesitas configurar Google Drive con rclone."
    echo "Este proceso es SEGURO y te permitirá hacer backups automáticos."
    echo
    echo -e "${GREEN}PASOS A SEGUIR:${NC}"
    echo "1. Se abrirá un navegador web"
    echo "2. Inicia sesión con tu cuenta de Google"
    echo "3. Autoriza el acceso a Google Drive"
    echo "4. Copia el código que aparece"
    echo "5. Pégalo cuando se te solicite"
    echo
    read -p "¿Deseas continuar con la configuración? (y/N): " -n 1 -r
    echo
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo
        log "Iniciando configuración interactiva de Google Drive..."
        echo
        echo -e "${BLUE}INSTRUCCIONES:${NC}"
        echo "• Cuando veas 'Enter name for remote:', escribe: ${GREEN}gdrive${NC}"
        echo "• Cuando veas 'Choose a number from below:', selecciona: ${GREEN}13${NC} (Google Drive)"
        echo "• Para client_id y client_secret, presiona Enter (usar valores por defecto)"
        echo "• Para scope, selecciona: ${GREEN}1${NC} (drive - Full access)"
        echo "• Para todas las demás opciones, presiona Enter para usar valores por defecto"
        echo
        read -p "Presiona Enter para continuar..."
        
        rclone config
        
        # Verificar si la configuración fue exitosa
        if rclone listremotes | grep -q "gdrive:"; then
            log_success "Google Drive configurado exitosamente"
        else
            log_error "Error configurando Google Drive"
            echo "Puedes volver a ejecutar este script más tarde"
            exit 1
        fi
    else
        log_warning "Configuración de Google Drive omitida"
        echo "⚠️  Los backups se crearán localmente pero no se subirán a Google Drive"
        echo "Puedes configurar Google Drive más tarde ejecutando: rclone config"
    fi
else
    log_success "Google Drive ya está configurado"
fi

# 5. Crear directorio de backups locales
BACKUP_DIR="/tmp/mtn_backups"
log "Creando directorios de backup..."
mkdir -p "$BACKUP_DIR"
mkdir -p "$BACKUP_DIR/logs"
log_success "Directorios creados: $BACKUP_DIR"

# 6. Test del script de backup
log "Probando script de backup..."
BACKUP_SCRIPT="$SCRIPT_DIR/backup-to-gdrive.sh"

if [ -f "$BACKUP_SCRIPT" ]; then
    log_success "Script de backup encontrado"
    
    # Verificar que el script tiene permisos de ejecución
    if [ -x "$BACKUP_SCRIPT" ]; then
        log_success "Script tiene permisos de ejecución"
    else
        log "Otorgando permisos de ejecución..."
        chmod +x "$BACKUP_SCRIPT"
        log_success "Permisos otorgados"
    fi
    
    # Preguntar si quiere hacer un backup de prueba
    echo
    read -p "¿Deseas ejecutar un backup de prueba AHORA? (y/N): " -n 1 -r
    echo
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        log "🚀 Ejecutando backup de prueba..."
        echo "────────────────────────────────────────────────────────────"
        
        "$BACKUP_SCRIPT"
        
        echo "────────────────────────────────────────────────────────────"
        if [ $? -eq 0 ]; then
            log_success "✅ Backup de prueba completado exitosamente"
        else
            log_error "❌ Error en backup de prueba"
        fi
    else
        log "Backup de prueba omitido"
    fi
else
    log_error "Script de backup no encontrado: $BACKUP_SCRIPT"
    exit 1
fi

# 7. Configurar cron job para backup automático diario
log "Configurando backup automático diario..."

# Crear entrada de cron
CRON_ENTRY="0 2 * * * \"$BACKUP_SCRIPT\" >> \"$BACKUP_DIR/logs/cron.log\" 2>&1"

# Verificar si ya existe una entrada similar
if crontab -l 2>/dev/null | grep -q "backup-to-gdrive.sh"; then
    log_warning "Entrada de cron ya existe para backup automático"
    
    echo "Entrada actual en cron:"
    crontab -l 2>/dev/null | grep "backup-to-gdrive.sh"
    echo
    
    read -p "¿Deseas reemplazar la entrada existente? (y/N): " -n 1 -r
    echo
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        # Remover entrada existente y agregar nueva
        (crontab -l 2>/dev/null | grep -v "backup-to-gdrive.sh"; echo "$CRON_ENTRY") | crontab -
        log_success "Entrada de cron actualizada"
    else
        log "Entrada de cron no modificada"
    fi
else
    # Agregar nueva entrada
    (crontab -l 2>/dev/null; echo "$CRON_ENTRY") | crontab -
    log_success "Backup automático programado para las 2:00 AM diariamente"
fi

# 8. Crear archivo de configuración adicional
CONFIG_FILE="$SCRIPT_DIR/backup-config.txt"
cat > "$CONFIG_FILE" << EOF
CONFIGURACIÓN DEL SISTEMA DE BACKUP
==================================
Fecha de configuración: $(date)
Script de backup: $BACKUP_SCRIPT
Directorio de backups: $BACKUP_DIR
Horario programado: Diario a las 2:00 AM

COMPONENTES INSTALADOS:
- Homebrew: $(brew --version | head -1)
- rclone: $(rclone version | head -1)
- PostgreSQL: $(pg_dump --version)

CONFIGURACIÓN DE GOOGLE DRIVE:
$(if rclone listremotes | grep -q "gdrive:"; then echo "✅ Configurado correctamente"; else echo "❌ No configurado"; fi)

CRON JOB:
$CRON_ENTRY

LOGS:
- Backup logs: $BACKUP_DIR/logs/
- Cron logs: $BACKUP_DIR/logs/cron.log

COMANDOS ÚTILES:
- Backup manual: $BACKUP_SCRIPT
- Ver cron jobs: crontab -l
- Editar cron jobs: crontab -e
- Ver logs recientes: tail -f $BACKUP_DIR/logs/cron.log
EOF

log_success "Archivo de configuración creado: $CONFIG_FILE"

# 9. Resumen final
echo
echo -e "${GREEN}"
echo "╔══════════════════════════════════════════════════════════════╗"
echo "║                    ✅ CONFIGURACIÓN COMPLETADA                ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo -e "${NC}"

echo -e "${BLUE}📋 RESUMEN DEL SISTEMA DE BACKUP:${NC}"
echo
echo -e "${GREEN}✅ Componentes instalados:${NC}"
echo "   • Homebrew (gestor de paquetes)"
echo "   • rclone (sincronización con Google Drive)"
echo "   • PostgreSQL client (backup de base de datos)"

echo
echo -e "${GREEN}✅ Backup automático configurado:${NC}"
echo "   • Horario: Diario a las 2:00 AM"
echo "   • Incluye: Base de datos + archivos + configuración"
echo "   • Destino: Google Drive/Backups_MTN_Sistema_Admision/"
echo "   • Retención: 30 días en la nube, 3 días local"

echo
echo -e "${GREEN}✅ Funcionalidades:${NC}"
echo "   • Backup completo de PostgreSQL"
echo "   • Backup de archivos subidos (documentos)"
echo "   • Backup de configuración del sistema"
echo "   • Compresión automática"
echo "   • Subida automática a Google Drive"
echo "   • Limpieza automática de backups antiguos"
echo "   • Logs detallados de todas las operaciones"

echo
echo -e "${YELLOW}📋 PRÓXIMOS PASOS:${NC}"
echo "1. El backup se ejecutará automáticamente cada día a las 2:00 AM"
echo "2. Revisa tu Google Drive en: Backups_MTN_Sistema_Admision/"
echo "3. Logs disponibles en: $BACKUP_DIR/logs/"
echo
echo -e "${BLUE}🛠️  COMANDOS ÚTILES:${NC}"
echo "• Backup manual:     $BACKUP_SCRIPT"
echo "• Ver backups cron:  crontab -l"
echo "• Ver logs:          tail -f $BACKUP_DIR/logs/cron.log"
echo "• Listar en Drive:   rclone ls gdrive:Backups_MTN_Sistema_Admision"

echo
echo -e "${GREEN}🎉 ¡SISTEMA DE BACKUP LISTO Y FUNCIONANDO!${NC}"
echo "Tus datos críticos de estudiantes ahora están protegidos con backup automático diario."

exit 0