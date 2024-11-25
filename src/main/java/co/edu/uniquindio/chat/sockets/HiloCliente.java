package co.edu.uniquindio.chat.sockets;

import co.edu.uniquindio.chat.modelo.Usuario;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HiloCliente implements Runnable{

    private Socket socket;
    private Map<String, HiloCliente> usuariosConectados;
    private Map<String, Chat> chatsActivos;
    private Usuario usuario;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    public HiloCliente(Socket socket, Map<String, HiloCliente> usuariosConectados, Map<String, Chat> chatsActivos){
        try{
            this.socket = socket;
            this.usuariosConectados = usuariosConectados;
            this.chatsActivos = chatsActivos;
            this.ois = new ObjectInputStream((socket.getInputStream()));
            this.oos = new ObjectOutputStream(socket.getOutputStream());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {

            String opcion = (String) ois.readObject();

            if ("AGREGAR".equals(opcion)) {
                this.usuario = (Usuario) ois.readObject();
                usuariosConectados.put(this.usuario.getIdUsuario(), this);
                oos.writeObject("Conectado como: " + this.getUsuario().getNombre());
                System.out.println(this.getUsuario().getNombre() + " se conectó.");
            }

            while(true) {

                opcion = (String) ois.readObject();

                switch (opcion) {

                    case "CONSULTA":
                        oos.writeObject(getUsuariosConectados());
                        oos.flush();
                        break;

                    case "INICIAR_CHAT":
                        String destinatarioID = (String) ois.readObject();

                        if (usuariosConectados.containsKey(destinatarioID)) {
                            HiloCliente destinatario = usuariosConectados.get(destinatarioID);
                            Chat nuevoChat = new Chat(this, destinatario);
                            chatsActivos.put(UUID.randomUUID().toString(), nuevoChat);
                            oos.writeObject("Chat iniciado con " + destinatario.getUsuario().getNombre());
                            oos.flush();
                        } else {
                            oos.writeObject("El usuario no está conectado.");
                            oos.flush();
                        }

                        break;

                    case "CHATS":

                        List<String> chats = new ArrayList<>();
                        for (Map.Entry<String, Chat> chat : chatsActivos.entrySet()) {
                            if (chat.getValue().getUsuario1().equals(this) || chat.getValue().getUsuario2().equals(this)) {
                                chats.add(chat.getKey());
                            }
                        }
                        oos.writeObject(chats.toString());
                        oos.flush();
                        break;

                    case "ENVIAR_MENSAJE":
                        String chatID = (String) ois.readObject();
                        Chat chat = chatsActivos.get(chatID);
                        if (chat != null) {
                            while (true) {
                                System.out.println("Preparado para enviar mensajes en el chat: " + chatID);
                                String mensaje = (String) ois.readObject();

                                // Check for exit condition
                                if ("SALIR_CHAT".equals(mensaje)) {
                                    chatsActivos.remove(chatID);
                                    oos.writeObject("Chat terminado.");
                                    break;
                                }

                                // Use the Chat class's enviarMensaje method
                                chat.enviarMensaje(mensaje, this.socket);
                            }
                        } else {
                            oos.writeObject("Chat no encontrado.");
                            oos.flush();
                        }

                        break;

                    case "SALIR":
                        usuariosConectados.remove(this.usuario.getIdUsuario());
                        System.out.println(this.usuario.getNombre() + " se ha desconectado.");
                        break;

                    default:
                        oos.writeObject("Comando no válido.");
                        break;

                }

            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public List<String> getUsuariosConectados(){
        List<String> usuarios = new ArrayList<>();

        for(HiloCliente usuario : usuariosConectados.values()){
            usuarios.add(usuario.getUsuario().getIdUsuario()+" "+usuario.getUsuario().getNombre());
        }

        return usuarios;
    }

    public Socket getSocket() {
        return socket;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public ObjectInputStream getOis() {
        return ois;
    }

    public ObjectOutputStream getOos() {
        return oos;
    }
}
