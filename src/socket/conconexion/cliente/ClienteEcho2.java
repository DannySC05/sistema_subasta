package socket.conconexion.cliente;

import java.io.*;

/**
 * Cliente de subasta basado en el cliente Echo original.
 * Muestra un menú de comandos y envía líneas de texto al servidor.
 * Recibe y muestra las respuestas de forma asíncrona.
 */
public class ClienteEcho2 {

    static final String mensajeFin = ".";
    // Debe coincidir con el puerto por defecto de ServidorEcho3 si no se pasa arg.
    static final String PUERTO_POR_DEFECTO = "8007";

    public static void main(String[] args) {
        InputStreamReader is = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(is);

        try {
            // Mensaje de bienvenida y lista de comandos disponibles
            System.out.println("======================================");
            System.out.println("   CLIENTE DE SUBASTA (TCP Concurrente)");
            System.out.println("======================================");
            System.out.println("Comandos disponibles:");
            System.out.println("  JOIN <alias>   -> Registrarse en la subasta");
            System.out.println("  BID <monto>    -> Realizar una oferta");
            System.out.println("  STATUS         -> Ver estado actual de la subasta");
            System.out.println("  HISTORY        -> Ver historial de subastas finalizadas");
            System.out.println("  QUIT o .       -> Salir del cliente");
            System.out.println("======================================");
            System.out.println();

            // Datos de conexión
            System.out.print("¿Cuál es el nombre de la máquina servidora? [default: localhost]: ");
            String nombreMaquina = br.readLine();
            if (nombreMaquina == null || nombreMaquina.trim().length() == 0) {
                nombreMaquina = "localhost";
            }

            System.out.print("¿Cuál es el número de puerto del servidor? [default: "
                    + PUERTO_POR_DEFECTO + "]: ");
            String numPuerto = br.readLine();
            if (numPuerto == null || numPuerto.trim().length() == 0) {
                numPuerto = PUERTO_POR_DEFECTO;
            }

            // Crear el auxiliar que maneja la comunicación
            ClienteEchoAuxiliar2 auxiliar =
                    new ClienteEchoAuxiliar2(nombreMaquina, numPuerto);

            // Iniciar hilo que escucha todas las respuestas del servidor
            auxiliar.iniciarListener(new ClienteEchoAuxiliar2.RespuestaHandler() {
                @Override
                public void manejarRespuesta(String respuesta) {
                    mostrarRespuestaInterpretada(respuesta);
                }
            });

            System.out.println();
            System.out.println("Conectado al servidor de subasta en " +
                    nombreMaquina + ":" + numPuerto);
            System.out.println("Recuerde usar los comandos indicados arriba.");
            System.out.println();

            boolean hecho = false;
            String mensaje;

            while (!hecho) {
                System.out.print("subasta> ");
                mensaje = br.readLine();
                if (mensaje == null) {
                    // entrada estándar cerrada
                    break;
                }
                mensaje = mensaje.trim();
                if (mensaje.length() == 0) {
                    continue; // ignora líneas vacías
                }

                // Manejo de salida
                if (mensaje.equals(mensajeFin) ||
                        mensaje.equalsIgnoreCase("QUIT")) {
                    hecho = true;
                    auxiliar.hecho();
                } else {
                    // Ya NO esperamos una respuesta aquí;
                    // el listener la mostrará cuando llegue.
                    auxiliar.enviarComando(mensaje);
                }
            } // fin while

            System.out.println("Cliente de subasta terminado.");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Interpreta las respuestas del servidor según el protocolo de la subasta
     * y las muestra de forma más amigable.
     */
    private static void mostrarRespuestaInterpretada(String resp) {
        resp = resp.trim();
        if (resp.length() == 0) {
            return;
        }

        String[] partes = resp.split("\\s+");
        String cmd = partes[0].toUpperCase();

        try {
            switch (cmd) {
                case "WELCOME": {
                    // WELCOME <idCliente> <estado>
                    if (partes.length >= 3) {
                        String id = partes[1];
                        String estado = partes[2];
                        System.out.println();
                        System.out.println("[Servidor] Bienvenido. ID: " + id +
                                " | Estado actual de la subasta: " + estado);
                    } else {
                        System.out.println();
                        System.out.println(resp);
                    }
                    break;
                }
                case "CURRENT": {
                    // CURRENT <idSubasta> <oferta> <idGanador> <aliasGanador> <segRest> <estado>
                    if (partes.length >= 7) {
                        String idSubasta = partes[1];
                        String oferta = partes[2];
                        String idGanador = partes[3];
                        String aliasGanador = partes[4];
                        String segRest = partes[5];
                        String estado = partes[6];
                        System.out.println();
                        System.out.println("[Estado actual]");
                        System.out.println("  Subasta: " + idSubasta);
                        System.out.println("  Oferta máxima: " + oferta);
                        System.out.println("  Ganador actual: " + aliasGanador +
                                " (" + idGanador + ")");
                        System.out.println("  Tiempo restante: " + segRest + " segundos");
                        System.out.println("  Estado: " + estado);
                    } else {
                        System.out.println();
                        System.out.println(resp);
                    }
                    break;
                }
                case "NEW_BID": {
                    // NEW_BID <monto> <idCliente> <alias>
                    if (partes.length >= 4) {
                        String monto = partes[1];
                        String id = partes[2];
                        String alias = partes[3];
                        System.out.println();
                        System.out.println("[Nueva oferta]");
                        System.out.println("  " + alias + " (" + id + ") ha ofertado " + monto);
                    } else {
                        System.out.println();
                        System.out.println(resp);
                    }
                    break;
                }
                case "BID_OK": {
                    // BID_OK <monto> <idCliente>
                    if (partes.length >= 3) {
                        String monto = partes[1];
                        String id = partes[2];
                        System.out.println();
                        System.out.println("[Oferta aceptada] Tu oferta de " +
                                monto + " ahora es la máxima (ID: " + id + ").");
                    } else {
                        System.out.println();
                        System.out.println(resp);
                    }
                    break;
                }
                case "BID_REJECT": {
                    // BID_REJECT <motivo>
                    String motivo = (partes.length >= 2) ? partes[1] : "";
                    System.out.println();
                    if ("MENOR_A_ACTUAL".equalsIgnoreCase(motivo)) {
                        System.out.println("[Oferta rechazada] El monto es menor o igual a la oferta actual.");
                    } else if ("SUBASTA_FINALIZADA".equalsIgnoreCase(motivo)) {
                        System.out.println("[Oferta rechazada] La subasta no está activa.");
                    } else {
                        System.out.println("[Oferta rechazada] Motivo: " + motivo);
                    }
                    break;
                }
                case "AUCTION_STARTED": {
                    // AUCTION_STARTED <idSubasta>
                    if (partes.length >= 2) {
                        String idSubasta = partes[1];
                        System.out.println();
                        System.out.println("======================================");
                        System.out.println("  SUBASTA " + idSubasta + " INICIADA");
                        System.out.println("======================================");
                    } else {
                        System.out.println();
                        System.out.println(resp);
                    }
                    break;
                }
                case "AUCTION_ENDED": {
                    // AUCTION_ENDED <idSubasta> <montoGanador> <idGanador> <aliasGanador>
                    if (partes.length >= 5) {
                        String idSubasta = partes[1];
                        String monto = partes[2];
                        String id = partes[3];
                        String alias = partes[4];
                        System.out.println();
                        System.out.println("======================================");
                        System.out.println("  SUBASTA " + idSubasta + " FINALIZADA");
                        System.out.println("  Ganador: " + alias + " (" + id + ")");
                        System.out.println("  Oferta ganadora: " + monto);
                        System.out.println("======================================");
                    } else {
                        System.out.println();
                        System.out.println(resp);
                    }
                    break;
                }
                case "NEW_AUCTION_IN": {
                    // NEW_AUCTION_IN <segundos>
                    String seg = (partes.length >= 2) ? partes[1] : "?";
                    System.out.println();
                    System.out.println("[Servidor] Nueva subasta disponible en " + seg + " segundos.");
                    break;
                }
                case "ERROR": {
                    // ERROR <mensaje...>
                    String msg = resp.substring(6); // después de "ERROR "
                    System.out.println();
                    System.out.println("[ERROR] " + msg);
                    break;
                }
                default:
                    // Cualquier otra línea que no reconozcamos, la mostramos tal cual
                    System.out.println();
                    System.out.println(resp);
            }
        } catch (Exception e) {
            // Si algo falla al parsear, mostramos la respuesta cruda
            System.out.println();
            System.out.println(resp);
        }

        // Vuelve a mostrar el prompt (para que no se “pierda” el subasta>)
        //System.out.print("subasta> ");
    }
}
