package socket.conconexion.servidor;

import java.util.List;
import socket.conconexion.stream.MiSocketStream;

/**
 * Hilo que maneja la sesión de un cliente en el contexto de la subasta.
 * Interpreta comandos:
 *  - JOIN <alias>
 *  - BID <monto>
 *  - STATUS
 *  - HISTORY
 *  - QUIT o "."
 */
class HiloServidorSubasta implements Runnable {

    static final String mensajeFin = ".";
    private final ClienteConectado cliente;
    private final EstadoSubasta estadoSubasta;
    private final GestorClientes gestorClientes;

    HiloServidorSubasta(ClienteConectado cliente,
                        EstadoSubasta estadoSubasta,
                        GestorClientes gestorClientes) {
        this.cliente = cliente;
        this.estadoSubasta = estadoSubasta;
        this.gestorClientes = gestorClientes;
    }

    public void run() {
        MiSocketStream miSocketDatos = cliente.getSocket();
        try {
            boolean hecho = false;
            while (!hecho) {
                String mensaje = miSocketDatos.recibeMensaje();
                if (mensaje == null) {
                    hecho = true;
                    break;
                }

                mensaje = mensaje.trim();
                if (mensaje.length() == 0) {
                    continue;
                }

                if (mensaje.equals(mensajeFin) || mensaje.equalsIgnoreCase("QUIT")) {
                    hecho = true;
                    break;
                }

                procesarComando(mensaje, miSocketDatos);
            }
        } catch (Exception ex) {
            System.out.println("Excepción capturada en hilo de subasta: " + ex);
        } finally {
            try {
                miSocketDatos.close();
            } catch (Exception e) {
                // ignorar
            }
            gestorClientes.eliminarCliente(cliente);
            System.out.println("Cliente " + cliente.getIdCliente() + " desconectado.");
        }
    }

    private void procesarComando(String linea, MiSocketStream socket) {
        try {
            String[] partes = linea.split("\\s+", 2);
            String comando = partes[0].toUpperCase();
            String argumentos = (partes.length > 1) ? partes[1].trim() : "";

            if ("JOIN".equals(comando)) {
                manejarJoin(argumentos, socket);
            } else if ("BID".equals(comando)) {
                manejarBid(argumentos, socket);
            } else if ("STATUS".equals(comando)) {
                manejarStatus(socket);
            } else if ("HISTORY".equals(comando)) {
                manejarHistory(socket);
            } else {
                socket.enviaMensaje("ERROR Comando no reconocido");
            }
        } catch (Exception ex) {
            try {
                socket.enviaMensaje("ERROR Excepción procesando comando: " + ex.getMessage());
            } catch (Exception e2) {
                // ignorar
            }
        }
    }

    private void manejarJoin(String alias, MiSocketStream socket) throws Exception {
        if (alias == null || alias.length() == 0) {
            socket.enviaMensaje("ERROR Debe indicar un alias");
            return;
        }
        cliente.setAlias(alias);
        estadoSubasta.registrarParticipanteParaSiguienteSubasta(cliente);
        String estadoTexto = estadoSubasta.getEstadoTexto();
        socket.enviaMensaje("WELCOME " + cliente.getIdCliente() + " " + estadoTexto);
        System.out.println("Cliente " + cliente.getIdCliente() + " usa alias: " + alias);
    }

    private void manejarBid(String argMonto, MiSocketStream socket) throws Exception {
        if (!estadoSubasta.isSubastaEnCurso()) {
            socket.enviaMensaje("BID_REJECT SUBASTA_FINALIZADA");
            return;
        }
        if (cliente.getAlias() == null || cliente.getAlias().length() == 0) {
            socket.enviaMensaje("ERROR Debe hacer JOIN antes de ofertar");
            return;
        }
        if (argMonto == null || argMonto.length() == 0) {
            socket.enviaMensaje("ERROR Debe indicar el monto de la oferta");
            return;
        }
        double monto;
        try {
            monto = Double.parseDouble(argMonto);
        } catch (NumberFormatException nfe) {
            socket.enviaMensaje("ERROR Monto inválido");
            return;
        }

        boolean esNuevaMaxima = estadoSubasta.registrarOferta(
                monto, cliente.getIdCliente(), cliente.getAlias());

        if (esNuevaMaxima) {
            socket.enviaMensaje("BID_OK " + monto + " " + cliente.getIdCliente());
            String msgBroadcast = "NEW_BID " + monto + " " +
                    cliente.getIdCliente() + " " + cliente.getAlias();
            gestorClientes.broadcastExcept(msgBroadcast, cliente);
        } else {
            socket.enviaMensaje("BID_REJECT MENOR_A_ACTUAL");
        }
    }

    private void manejarStatus(MiSocketStream socket) throws Exception {
        double oferta = estadoSubasta.getOfertaMaxima();
        String idGanador = estadoSubasta.getIdClienteGanador();
        String aliasGanador = estadoSubasta.getAliasGanador();
        long segRest = estadoSubasta.getSegundosRestantes();
        String estadoTexto = estadoSubasta.getEstadoTexto();
        int idSubasta = estadoSubasta.getIdSubastaActual();

        if (idGanador == null) {
            idGanador = "SIN_GANADOR";
        }
        if (aliasGanador == null) {
            aliasGanador = "-";
        }

        // CURRENT <idSubasta> <oferta> <idGanador> <aliasGanador> <segRest> <estado>
        String respuesta = "CURRENT " + idSubasta + " " + oferta + " " +
                idGanador + " " + aliasGanador + " " +
                segRest + " " + estadoTexto;
        socket.enviaMensaje(respuesta);
    }

    private void manejarHistory(MiSocketStream socket) throws Exception {
        List<EstadoSubasta.ResumenSubasta> hist = estadoSubasta.getHistorial();
        if (hist.isEmpty()) {
            socket.enviaMensaje("No hay subastas finalizadas aún.");
            return;
        }
        for (EstadoSubasta.ResumenSubasta r : hist) {
            String idGan = r.getIdGanador();
            String aliasGan = r.getAliasGanador();
            if (idGan == null) {
                idGan = "SIN_GANADOR";
            }
            if (aliasGan == null) {
                aliasGan = "-";
            }
            String montoStr = String.valueOf(r.getOfertaMaxima());
            socket.enviaMensaje("======================================");
            socket.enviaMensaje("  SUBASTA " + r.getId() + " FINALIZADA");
            socket.enviaMensaje("  Ganador: " + aliasGan + " ( " + idGan + " )");
            socket.enviaMensaje("  Oferta ganadora: " + montoStr);
            socket.enviaMensaje("======================================");
        }
    }
}
