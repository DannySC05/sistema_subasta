package socket.conconexion.stream;

import java.io.*;
import java.net.*;

/**
 * Una clase de envoltura de Socket que contiene
 * métodos para mandar y recibir mensajes fácilmente.
 * Sirve tanto para el cliente como para el servidor.
 */
public class MiSocketStream {

    private Socket socket;
    private BufferedReader entrada;
    private PrintWriter salida;

    /**
     * Constructor usado por el CLIENTE.
     * Crea un Socket normal con host y puerto.
     */
    public MiSocketStream(String host, int puerto) throws IOException {
        this(new Socket(host, puerto));
    }

    /**
     * Constructor usado por el SERVIDOR.
     * Recibe un Socket ya aceptado.
     */
    public MiSocketStream(Socket socketAceptado) throws IOException {
        this.socket = socketAceptado;

        // Canal de entrada
        this.entrada = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));

        // Canal de salida
        this.salida = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream()), true);
    }

    /**
     * Enviar un mensaje (termina en salto de línea).
     */
    public void enviaMensaje(String mensaje) {
        salida.println(mensaje);
    }

    /**
     * Recibir un mensaje del canal.
     * Regresa null si el cliente cerró la conexión.
     */
    public String recibeMensaje() throws IOException {
        return entrada.readLine();
    }

    /**
     * IP del otro extremo.
     */
    public InetAddress getInetAddress() {
        return socket.getInetAddress();
    }

    /**
     * Puerto remoto del otro extremo.
     */
    public int getPort() {
        return socket.getPort();
    }

    /**
     * Cerrar el socket y los flujos.
     */
    public void close() throws IOException {
        socket.close();
    }
}
