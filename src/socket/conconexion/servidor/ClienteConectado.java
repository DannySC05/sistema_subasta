package socket.conconexion.servidor;

import java.net.InetAddress;
import socket.conconexion.stream.MiSocketStream;

/**
 * Representa un cliente conectado al servidor de subasta.
 */
public class ClienteConectado {

    private final String idCliente;
    private String alias;
    private final String ip;
    private final int puerto;
    private final MiSocketStream socket;

    public ClienteConectado(String idCliente, MiSocketStream socket) {
        this.idCliente = idCliente;
        this.socket = socket;

        // Se obtiene la IP y el puerto remoto a partir del socket
        InetAddress addr = socket.getInetAddress();
        this.ip = (addr != null) ? addr.getHostAddress() : "desconocida";
        this.puerto = socket.getPort();
    }

    public String getIdCliente() {
        return idCliente;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getIp() {
        return ip;
    }

    public int getPuerto() {
        return puerto;
    }

    public MiSocketStream getSocket() {
        return socket;
    }
}
