import java.io.*;
import java.net.Socket;

public class ClientHandler {
    private Socket socket;
    private Server server;
    private DataInputStream in;
    private DataOutputStream out;
    private String username;
    private static int counter = 1;

    public String getUsername() {
        return username;
    }

    public ClientHandler(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        username = "User" + counter++;
        server.subscribe(this);

        new Thread(this::listen).start();
    }

    private void listen() {
        try {
            while (true) {
                String msg = in.readUTF();
                if (msg.equalsIgnoreCase("/exit")) break;
                server.broadcastMessage(username + ": " + msg);
            }
        } catch (IOException e) {
            System.out.println(username + " отключился.");
        } finally {
            disconnect();
        }
    }

    private void disconnect() {
        server.unsubscribe(this);
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            disconnect();
        }
    }
}
