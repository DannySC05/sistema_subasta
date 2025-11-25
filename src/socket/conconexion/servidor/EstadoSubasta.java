package socket.conconexion.servidor;

import java.util.ArrayList;
import java.util.List;

/**
 * Mantiene el estado global de las subastas.
 * Gestiona:
 *  - Subasta actual (id, oferta, ganador)
 *  - Cooldown entre subastas
 *  - Número mínimo de participantes
 *  - Historial de subastas finalizadas
 */
public class EstadoSubasta {

    /**
     * Estructura para guardar el resumen de cada subasta finalizada.
     */
    public static class ResumenSubasta {
        private final int id;
        private final double ofertaMaxima;
        private final String idGanador;
        private final String aliasGanador;

        public ResumenSubasta(int id, double ofertaMaxima, String idGanador, String aliasGanador) {
            this.id = id;
            this.ofertaMaxima = ofertaMaxima;
            this.idGanador = idGanador;
            this.aliasGanador = aliasGanador;
        }

        public int getId() {
            return id;
        }

        public double getOfertaMaxima() {
            return ofertaMaxima;
        }

        public String getIdGanador() {
            return idGanador;
        }

        public String getAliasGanador() {
            return aliasGanador;
        }
    }

    private double ofertaMaxima;
    private String idClienteGanador;
    private String aliasGanador;

    private int idSubastaActual;
    private int idSiguienteSubasta;

    private final long duracionSubastaMillis;
    private final long cooldownMillis;

    private long tiempoFinSubasta;
    private long tiempoFinCooldown;

    private boolean subastaEnCurso;

    private final int minParticipantes;
    private int participantesPreparados; // clientes que hicieron JOIN para la próxima subasta

    private final List<ResumenSubasta> historial = new ArrayList<ResumenSubasta>();

    public EstadoSubasta(long duracionSubastaMillis, long cooldownMillis, int minParticipantes) {
        this.duracionSubastaMillis = duracionSubastaMillis;
        this.cooldownMillis = cooldownMillis;
        this.minParticipantes = minParticipantes;
        this.idSubastaActual = 0;
        this.idSiguienteSubasta = 1;
        this.subastaEnCurso = false;
        this.tiempoFinSubasta = 0L;
        this.tiempoFinCooldown = 0L;
        this.participantesPreparados = 0;
        this.ofertaMaxima = 0.0;
    }

    /**
     * Registra que un cliente se apunta para la siguiente subasta.
     * Cada cliente cuenta solo una vez por subasta.
     */
    public synchronized void registrarParticipanteParaSiguienteSubasta(ClienteConectado cliente) {
        if (cliente.getUltimaSubastaRegistrada() < idSiguienteSubasta) {
            cliente.setUltimaSubastaRegistrada(idSiguienteSubasta);
            participantesPreparados++;
            System.out.println("Cliente " + cliente.getIdCliente()
                    + " registrado para subasta " + idSiguienteSubasta +
                    ". Total participantes listos: " + participantesPreparados);
        }
    }

    /**
     * ¿Se cumplen las condiciones para iniciar una nueva subasta?
     * - No hay subasta en curso
     * - El cooldown ha terminado (o nunca ha habido)
     * - Hay al menos minParticipantes registrados para la siguiente subasta
     */
    public synchronized boolean condicionesParaIniciar() {
        long ahora = System.currentTimeMillis();
        boolean cooldownTerminado = (tiempoFinCooldown == 0L) || (ahora >= tiempoFinCooldown);
        return !subastaEnCurso && cooldownTerminado && participantesPreparados >= minParticipantes;
    }

    /**
     * Inicia una nueva subasta si las condiciones se cumplen.
     * Devuelve el id de la subasta o -1 si no se inició.
     */
    public synchronized int iniciarSubasta() {
        if (!condicionesParaIniciar()) {
            return -1;
        }
        subastaEnCurso = true;
        idSubastaActual = idSiguienteSubasta;
        idSiguienteSubasta++;

        ofertaMaxima = 0.0;
        idClienteGanador = null;
        aliasGanador = null;

        tiempoFinSubasta = System.currentTimeMillis() + duracionSubastaMillis;

        System.out.println(">>> Subasta " + idSubastaActual + " iniciada.");
        return idSubastaActual;
    }

    /**
     * Finaliza la subasta actual, inicia el cooldown y guarda el resumen.
     */
    public synchronized ResumenSubasta finalizarSubasta() {
        if (!subastaEnCurso) {
            return null;
        }
        subastaEnCurso = false;
        tiempoFinCooldown = System.currentTimeMillis() + cooldownMillis;

        ResumenSubasta resumen = new ResumenSubasta(
                idSubastaActual, ofertaMaxima, idClienteGanador, aliasGanador);
        historial.add(resumen);

        participantesPreparados = 0; // Para la próxima subasta
        System.out.println(">>> Subasta " + idSubastaActual + " finalizada.");
        return resumen;
    }

    public synchronized boolean isSubastaEnCurso() {
        return subastaEnCurso;
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

    public synchronized int getIdSubastaActual() {
        return idSubastaActual;
    }

    public synchronized long getDuracionSubastaMillis() {
        return duracionSubastaMillis;
    }

    public synchronized long getCooldownSeconds() {
        return cooldownMillis / 1000L;
    }

    /**
     * Segundos restantes:
     *  - Si hay subasta en curso: hasta el final de la subasta.
     *  - Si no hay subasta y está en cooldown: hasta el fin del cooldown.
     *  - En otros casos: 0.
     */
    public synchronized long getSegundosRestantes() {
        long ahora = System.currentTimeMillis();
        if (subastaEnCurso && tiempoFinSubasta > 0L) {
            long diff = tiempoFinSubasta - ahora;
            return (diff > 0L) ? (diff / 1000L) : 0L;
        }
        if (!subastaEnCurso && tiempoFinCooldown > 0L && ahora < tiempoFinCooldown) {
            long diff = tiempoFinCooldown - ahora;
            return (diff > 0L) ? (diff / 1000L) : 0L;
        }
        return 0L;
    }

    /**
     * Texto del estado de la subasta:
     *  - EN_CURSO
     *  - COOLDOWN
     *  - ESPERANDO_PARA_INICIAR
     */
    public synchronized String getEstadoTexto() {
        long ahora = System.currentTimeMillis();
        if (subastaEnCurso) {
            return "EN_CURSO";
        }
        if (tiempoFinCooldown > 0L && ahora < tiempoFinCooldown) {
            return "COOLDOWN";
        }
        return "ESPERANDO_PARA_INICIAR";
    }

    /**
     * Intenta registrar una oferta. Solo tiene efecto si la subasta está en curso.
     */
    public synchronized boolean registrarOferta(double monto, String idCliente, String alias) {
        if (!subastaEnCurso) {
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
     * Devuelve una copia del historial de subastas finalizadas.
     */
    public synchronized List<ResumenSubasta> getHistorial() {
        return new ArrayList<ResumenSubasta>(historial);
    }
}
