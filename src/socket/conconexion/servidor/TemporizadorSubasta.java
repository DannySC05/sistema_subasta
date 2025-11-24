package socket.conconexion.servidor;

/**
 * Hilo que se encarga de esperar a que termine la duración de la subasta,
 * marcarla como finalizada y notificar a todos los clientes.
 */
public class TemporizadorSubasta implements Runnable {

    private final EstadoSubasta estadoSubasta;
    private final GestorClientes gestorClientes;
    private final long duracionMillis;

    public TemporizadorSubasta(EstadoSubasta estadoSubasta,
                               GestorClientes gestorClientes,
                               long duracionMillis) {
        this.estadoSubasta = estadoSubasta;
        this.gestorClientes = gestorClientes;
        this.duracionMillis = duracionMillis;
    }

    public void run() {
        try {
            Thread.sleep(duracionMillis);
            // Marca la subasta como finalizada
            estadoSubasta.marcarFinalizada();

            double oferta = estadoSubasta.getOfertaMaxima();
            String idGanador = estadoSubasta.getIdClienteGanador();
            String aliasGanador = estadoSubasta.getAliasGanador();

            if (idGanador == null) {
                idGanador = "SIN_GANADOR";
            }
            if (aliasGanador == null) {
                aliasGanador = "-";
            }

            String mensaje = "AUCTION_ENDED " + oferta + " " +
                    idGanador + " " + aliasGanador;
            gestorClientes.broadcast(mensaje);

            System.out.println("Subasta finalizada automáticamente por tiempo.");
        } catch (InterruptedException e) {
            System.out.println("Temporizador de subasta interrumpido: " + e);
        }
    }
}
