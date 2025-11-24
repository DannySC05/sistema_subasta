package socket.conconexion.servidor;

/**
 * Mantiene el estado global de la subasta:
 * - oferta máxima
 * - cliente ganador
 * - alias del ganador
 * - tiempo de finalización
 * - estado (finalizada o no)
 */
public class EstadoSubasta {

    private double ofertaMaxima;
    private String idClienteGanador;
    private String aliasGanador;
    private long tiempoFinMillis;
    private boolean finalizada;

    /**
     * Inicializa la subasta con una duración determinada.
     * @param duracionMillis Duración de la subasta en milisegundos.
     */
    public synchronized void iniciar(long duracionMillis) {
        this.ofertaMaxima = 0.0;
        this.idClienteGanador = null;
        this.aliasGanador = null;
        this.tiempoFinMillis = System.currentTimeMillis() + duracionMillis;
        this.finalizada = false;
    }

    /**
     * Intenta registrar una nueva oferta.
     * @return true si la oferta se convierte en la nueva máxima.
     */
    public synchronized boolean registrarOferta(double monto, String idCliente, String alias) {
        if (estaFinalizada()) {
            return false;
        }
        if (monto > ofertaMaxima) {
            ofertaMaxima = monto;
            idClienteGanador = idCliente;
            aliasGanador = alias;
            return true;
        }
        return false;
    }

    /**
     * Marca la subasta como finalizada de forma explícita.
     */
    public synchronized void marcarFinalizada() {
        finalizada = true;
    }

    /**
     * Indica si la subasta ya ha finalizado (por tiempo o explícitamente).
     */
    public synchronized boolean estaFinalizada() {
        if (!finalizada && tiempoFinMillis > 0 &&
                System.currentTimeMillis() > tiempoFinMillis) {
            finalizada = true;
        }
        return finalizada;
    }

    public synchronized double getOfertaMaxima() {
        return ofertaMaxima;
    }

    public synchronized String getIdClienteGanador() {
        return idClienteGanador;
    }

    public synchronized String getAliasGanador() {
        return aliasGanador;
    }

    /**
     * Devuelve los segundos restantes de subasta (0 si ya terminó).
     */
    public synchronized long getSegundosRestantes() {
        if (estaFinalizada()) {
            return 0;
        }
        long diff = tiempoFinMillis - System.currentTimeMillis();
        if (diff <= 0) {
            return 0;
        }
        return diff / 1000;
    }
}
