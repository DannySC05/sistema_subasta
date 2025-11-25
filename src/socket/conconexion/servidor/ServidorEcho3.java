package socket.conconexion.servidor;

import java.net.ServerSocket;
import java.net.Socket;
import socket.conconexion.stream.MiSocketStream;

/**
 * Servidor de subasta basado en el modelo de servidor Echo concurrente.
 * Acepta múltiples clientes en paralelo y comparte un estado de subasta.
 */
public class ServidorEcho3 {

    // Duración de cada subasta: 3 minutos (en milisegundos)
    private static final long DURACION_SUBASTA_MS = 3 * 60 * 1000;
    // Cooldown entre subastas: 1 minuto
    private static final long COOLDOWN_MS = 60 * 1000;
    // Mínimo de participantes para iniciar una subasta
    private static final int MIN_PARTICIPANTES = 2;

    public static void main(String[] args) {
        int puertoServidor = 8007; // Puerto por defecto

        if (args.length == 1) {
            puertoServidor = Integer.parseInt(args[0]);
        }

        ServerSocket miSocketConexion = null;

        try {
            miSocketConexion = new ServerSocket(puertoServidor);
            System.out.println("Servidor de subasta iniciado en el puerto " + puertoServidor);

            // Estado global de la subasta
            EstadoSubasta estadoSubasta = new EstadoSubasta(
                    DURACION_SUBASTA_MS, COOLDOWN_MS, MIN_PARTICIPANTES);

            // Gestor de clientes conectados
            GestorClientes gestorClientes = new GestorClientes();

            // Temporizador para orquestar las subastas
            TemporizadorSubasta temporizador = new TemporizadorSubasta(
                    estadoSubasta, gestorClientes);
            Thread hiloTemporizador = new Thread(temporizador);
            hiloTemporizador.start();

            // Bucle principal de aceptación de clientes
            while (true) {
                System.out.println("Espera una conexión.");
                Socket socketAceptado = miSocketConexion.accept();
                MiSocketStream miSocketDatos = new MiSocketStream(socketAceptado);
                System.out.println("Conexión aceptada");

                // Registrar cliente en el gestor
                ClienteConectado cliente = gestorClientes.registrarCliente(miSocketDatos);

                // Arranca un hilo para manejar la sesión de subasta de este cliente
                Thread elHilo = new Thread(
                        new HiloServidorSubasta(cliente, estadoSubasta, gestorClientes)
                );
                elHilo.start();
                // y continúa con el siguiente cliente
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (miSocketConexion != null) {
                try {
                    miSocketConexion.close();
                } catch (Exception e) {
                    // ignorar
                }
            }
        }
    }
}
