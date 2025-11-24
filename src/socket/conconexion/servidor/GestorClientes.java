package socket.conconexion.servidor;

import socket.conconexion.stream.MiSocketStream;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Gestiona la lista de clientes conectados y permite
 * hacer broadcast de mensajes a todos ellos.
 */
public class GestorClientes {

    private final List<ClienteConectado> clientes = new ArrayList<ClienteConectado>();
    private final AtomicInteger contadorClientes = new AtomicInteger(0);

    /**
     * Registra un nuevo cliente conectado creando un identificador C1, C2, etc.
     */
    public synchronized ClienteConectado registrarCliente(MiSocketStream socket) {
        String id = "C" + contadorClientes.incrementAndGet();
        ClienteConectado cliente = new ClienteConectado(id, socket);
        clientes.add(cliente);
        System.out.println("Cliente registrado: " + id +
                " desde " + cliente.getIp() + ":" + cliente.getPuerto());
        return cliente;
    }

    /**
     * Elimina un cliente de la lista de conectados.
     */
    public synchronized void eliminarCliente(ClienteConectado cliente) {
        clientes.remove(cliente);
        System.out.println("Cliente eliminado: " + cliente.getIdCliente());
    }

    /**
     * Envia el mensaje a todos los clientes conectados.
     * Si alguno falla, se elimina de la lista.
     */
    public synchronized void broadcast(String mensaje) {
        System.out.println("Broadcast: " + mensaje);
        Iterator<ClienteConectado> it = clientes.iterator();
        while (it.hasNext()) {
            ClienteConectado c = it.next();
            MiSocketStream s = c.getSocket();
            try {
                s.enviaMensaje(mensaje);
            } catch (Exception ex) {
                System.out.println("Error enviando a " + c.getIdCliente() +
                        ". Se eliminará el cliente. Excepción: " + ex);
                try {
                    s.close();
                } catch (Exception e2) {
                    // ignorar
                }
                it.remove();
            }
        }
    }

    public synchronized void broadcastExcept(String mensaje, ClienteConectado excluir) {
    System.out.println("Broadcast (excepto " + excluir.getIdCliente() + "): " + mensaje);
    Iterator<ClienteConectado> it = clientes.iterator();
    while (it.hasNext()) {
        ClienteConectado c = it.next();
        if (c == excluir) {
            continue; // no mandar al que queremos excluir
        }
        MiSocketStream s = c.getSocket();
        try {
            s.enviaMensaje(mensaje);
        } catch (Exception ex) {
            System.out.println("Error enviando a " + c.getIdCliente() +
                    ". Se eliminará el cliente. Excepción: " + ex);
            try {
                s.close();
            } catch (Exception e2) {
                // ignorar
            }
            it.remove();
        }
    }
}
}
