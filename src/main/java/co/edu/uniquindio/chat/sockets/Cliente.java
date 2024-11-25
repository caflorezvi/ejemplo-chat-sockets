package co.edu.uniquindio.chat.sockets;

import co.edu.uniquindio.chat.modelo.Usuario;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {

    private Socket socket;
    private ObjectOutputStream os;
    private ObjectInputStream ois;
    private final Scanner scanner;
    private Usuario usuario;

    public Cliente(String host, int port) {
        this.scanner = new Scanner(System.in);

        try {
            this.socket = new Socket(host, port);
            this.os = new ObjectOutputStream(socket.getOutputStream());
            this.ois = new ObjectInputStream(socket.getInputStream());
            System.out.println("Conectado al servidor.");
        } catch (IOException e) {
            System.out.println("Error al conectar con el servidor: " + e.getMessage());
        }
    }

    public void iniciar() {
        try {

            System.out.print("Ingrese su id: ");
            String id = scanner.nextLine();

            System.out.print("Ingrese su nombre de usuario: ");
            String nombre = scanner.nextLine();

            usuario = new Usuario(id, nombre);

            // Enviar comando AGREGAR y registrar usuario
            os.writeObject("AGREGAR");
            os.writeObject(usuario);
            System.out.println((String) ois.readObject()); // Confirmación del servidor


            boolean continuar = true;
            while (continuar) {
                System.out.println("\nMenú:");
                System.out.println("1 - Consultar usuarios conectados");
                System.out.println("2 - Crear una sala de chat");
                System.out.println("3 - Consultar salas de chat");
                System.out.println("4 - Empezar a chatear");
                System.out.println("5 - Salir");
                System.out.print("Seleccione una opción: ");
                String opcion = scanner.nextLine();

                switch (opcion) {
                    case "1": // Consultar usuarios conectados
                        os.writeObject("CONSULTA");
                        System.out.println("Usuarios conectados: " + ois.readObject());
                        break;

                    case "2": // Iniciar sala de chat
                        os.writeObject("INICIAR_CHAT");
                        System.out.print("Ingrese el nombre del usuario con quien desea chatear: ");
                        String destinatario = scanner.nextLine();
                        os.writeObject(destinatario);

                        String respuesta = (String) ois.readObject();
                        System.out.println(respuesta);
                        break;

                    case "3": // Consultar salas de chat
                        os.writeObject("CHATS");
                        String chats = (String) ois.readObject();
                        System.out.println(chats);
                        break;

                    case "4": // Empezar a chatear
                        os.writeObject("ENVIAR_MENSAJE");

                        System.out.print("Ingrese el ID del chat: ");
                        String chatID = scanner.nextLine();
                        os.writeObject(chatID);

                        iniciarChat();

                        break;

                    case "5": // Salir
                        os.writeObject("SALIR");
                        continuar = false;
                        break;

                    default:
                        System.out.println("Opción no válida.");
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            cerrarConexion();
        }

    }

    private void iniciarChat() {
        try {
            System.out.println("Escriba sus mensajes. Escriba 'SALIR_CHAT' para terminar.");

            // Separate thread for receiving messages
            new Thread(() -> {
                try {
                    while (true) {
                        String mensajeRecibido = (String) ois.readObject();
                        System.out.println(mensajeRecibido);

                        if (mensajeRecibido.equals("Chat terminado.")) {
                            break;
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("Error al recibir mensajes: " + e.getMessage());
                }
            }).start();

            // Main thread for sending messages
            while (true) {
                String mensaje = scanner.nextLine();
                os.writeObject(mensaje);

                if ("SALIR_CHAT".equals(mensaje)) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cerrarConexion() {
        try {
            if (socket != null) socket.close();
            if (os != null) os.close();
            if (ois != null) ois.close();
            System.out.println("Conexión cerrada.");
        } catch (IOException e) {
            System.out.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Cliente cliente = new Cliente("localhost", 12345);
        cliente.iniciar();
    }

}
