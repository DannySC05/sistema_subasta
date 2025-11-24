Desde C:\VI SEMESTRE\Distribuidos\subasta

javac -d bin src\socket\conconexion\stream\MiSocketStream.java src\socket\conconexion\servidor\*.java src\socket\conconexion\cliente\*.java

PARA SERVIDOR
# Ver estado
sudo systemctl status subasta

# Ver logs en tiempo real
sudo journalctl -u subasta -f

# Ver últimas 50 líneas de logs
sudo journalctl -u subasta -n 50

# Reiniciar servicio
sudo systemctl restart subasta

# Detener servicio
sudo systemctl stop subasta

# Iniciar servicio
sudo systemctl start subasta

# Deshabilitar inicio automático
sudo systemctl disable subasta
```

---

# Resumen de tu Configuración Final
```
╔════════════════════════════════════════╗
║   SERVIDOR DE SUBASTA - PRODUCCIÓN     ║
╚════════════════════════════════════════╝

📍 IP Pública: 157.151.212.197
🔌 Puerto: 8007
🔑 SSH: ssh-key-2025-11-19.key
👤 Usuario: ubuntu
☕ Java: OpenJDK 24
📁 Directorio: /home/ubuntu/servidor-subasta

═══════════════════════════════════════
CONEXIÓN SSH:
═══════════════════════════════════════
DESDE: C:\VI SEMESTRE\Distribuidos\subasta\Claves

ssh -i ssh-key-2025-11-19.key ubuntu@157.151.212.197

═══════════════════════════════════════
CLIENTE (desde tu PC):
═══════════════════════════════════════
Servidor: 157.151.212.197
Puerto: 8007