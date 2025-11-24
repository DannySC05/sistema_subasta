package socket.conconexion.cliente;

import java.io.*;
import java.net.*;

import socket.conconexion.stream.MiSocketStream;

/**
 * Módulo auxiliar para el cliente de subasta.
 * Administra el socket y proporciona métodos para enviar comandos
 * y escuchar respuestas de forma asíncrona.
 */
public class ClienteEchoAuxiliar2 {

    static final String mensajeFin = ".";
    private MiSocketStream miSocket;
    private InetAddress maquinaServidora;
    private int puertoServidor;

    /**
     * Crea el socket de comunicación con el servidor.
     */
    public ClienteEchoAuxiliar2(String nombreMaquina, String numPuerto)
            throws IOException {
        this.maquinaServidora = InetAddress.getByName(nombreMaquina);
        this.puertoServidor = Integer.parseInt(numPuerto);
        // Conecta al servidor usando el wrapper de stream
        this.miSocket = new MiSocketStream(
                this.maquinaServidora.getHostAddress(),
                this.puertoServidor);
    }

    /**
     * Envía un comando al servidor (no espera respuesta).
     */
    public void enviarComando(String mensaje) throws IOException {
        miSocket.enviaMensaje(mensaje);
    }

    /**
     * Versión antigua de obtenerEco, la dejamos por compatibilidad.
     * Envía un mensaje y espera UNA línea de respuesta.
     * (No se usa en el cliente de subasta actual.)
     */
    public String obtenerEco(String mensaje) throws IOException {
        enviarComando(mensaje);
        return miSocket.recibeMensaje();
    }

    /**
     * Inicia un hilo en segundo plano que escucha permanentemente
     * lo que envíe el servidor y lo pasa a un manejador de respuestas.
     */
    public void iniciarListener(RespuestaHandler handler) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String linea;
                    while ((linea = miSocket.recibeMensaje()) != null) {
                        handler.manejarRespuesta(linea);
                    }
                } catch (IOException e) {
                    // El socket se cerró: terminar hilo silenciosamente
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    /**
     * Cierra la conexión limpiamente.
     */
    public void hecho() throws IOException {
        try {
            miSocket.enviaMensaje(mensajeFin);
        } catch (Exception e) {
            // ignorar
        }
        miSocket.close();
    }

    /**
     * Interfaz funcional para manejar respuestas recibidas del servidor.
     */
    public static interface RespuestaHandler {
        void manejarRespuesta(String respuesta);
    }
}
