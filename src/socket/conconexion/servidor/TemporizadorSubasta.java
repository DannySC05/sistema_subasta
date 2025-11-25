package socket.conconexion.servidor;

/**
 * Hilo que orquesta las subastas:
 *  - Espera a que haya suficientes participantes y termine el cooldown
 *  - Inicia la subasta
 *  - Espera la duración configurada
 *  - Finaliza la subasta, almacena el resultado y avisa a los clientes
 */
public class TemporizadorSubasta implements Runnable {

    private final EstadoSubasta estadoSubasta;
    private final GestorClientes gestorClientes;

    public TemporizadorSubasta(EstadoSubasta estadoSubasta,
                               GestorClientes gestorClientes) {
        this.estadoSubasta = estadoSubasta;
        this.gestorClientes = gestorClientes;
    }

    public void run() {
        while (true) {
            try {
                // Esperar hasta que se cumplan las condiciones para iniciar
                while (!estadoSubasta.condicionesParaIniciar()) {
                    Thread.sleep(1000);
                }

                int id = estadoSubasta.iniciarSubasta();
                if (id < 0) {
                    continue;
                }

                gestorClientes.broadcast("AUCTION_STARTED " + id);

                long duracion = estadoSubasta.getDuracionSubastaMillis();
                Thread.sleep(duracion);

                EstadoSubasta.ResumenSubasta resumen = estadoSubasta.finalizarSubasta();
                if (resumen != null) {
                    String idGan = resumen.getIdGanador();
                    String aliasGan = resumen.getAliasGanador();
                    if (idGan == null) {
                        idGan = "SIN_GANADOR";
                    }
                    if (aliasGan == null) {
                        aliasGan = "-";
                    }
                    double monto = resumen.getOfertaMaxima();

                    String msgFin = "AUCTION_ENDED " + resumen.getId() + " " + monto + " " +
                            idGan + " " + aliasGan;
                    gestorClientes.broadcast(msgFin);

                    long segundosCooldown = estadoSubasta.getCooldownSeconds();
                    gestorClientes.broadcast("NEW_AUCTION_IN " + segundosCooldown);
                }
            } catch (InterruptedException e) {
                System.out.println("Temporizador de subasta interrumpido: " + e);
                break;
            }
        }
    }
}
