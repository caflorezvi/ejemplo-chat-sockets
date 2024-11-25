package co.edu.uniquindio.chat.sockets;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Chat implements Serializable {

    private HiloCliente usuario1;
    private HiloCliente usuario2;
    private List<String> mensajes;

    public Chat(HiloCliente usuario1, HiloCliente usuario2) {
        this.usuario1 = usuario1;
        this.usuario2 = usuario2;
        this.mensajes = new ArrayList<>();
    }

    public HiloCliente getUsuario1() {
        return usuario1;
    }

    public HiloCliente getUsuario2() {
        return usuario2;
    }

    public synchronized void enviarMensaje(String mensaje, Socket remitente) throws IOException {
        // Store the message in the chat history
        mensajes.add(mensaje);

        // Determine which user sent the message
        HiloCliente remitenteCli = (remitente == usuario1.getSocket()) ? usuario1 : usuario2;
        HiloCliente destinatario = (remitenteCli == usuario1) ? usuario2 : usuario1;

        // Send message to the other user
        ObjectOutputStream destinatarioStream = destinatario.getOos();
        destinatarioStream.writeObject(remitenteCli.getUsuario().getNombre() + ": " + mensaje);
        destinatarioStream.flush();
    }

}
