import java.io.*;
import java.net.Socket;

public class ClientHandler {
    private Socket socket;
    private Server server;
    private DataInputStream in;
    private DataOutputStream out;
    private String username;
    private Role role;
    private static int counter = 1;

    public String getUsername() {
        return username;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public ClientHandler(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        this.username =in.readUTF();
        this.role = server.registerClient(this);
        server.subscribe(this);

        new Thread(() -> listen()).start();
    }

    private void listen() {
        try {
            while (true) {
                String msg = in.readUTF();
                if (msg.equalsIgnoreCase("/exit")) break;

                if (msg.startsWith("/w ")) {
                    String[] parts = msg.split(" ", 3);
                    if (parts.length < 3) {
                        sendMessage("[Система] Использование: /w <имя> <сообщение>");
                        continue;
                    }

                    String targetName = parts[1];
                    String privateMsg = parts[2];

                    boolean sent = server.sendPrivateMessage(username, targetName, privateMsg);
                    if (!sent) {
                        sendMessage("[Система] Пользователь '" + targetName + "' не найден или не в сети.");
                    }
                    continue;
                }

                if(msg.startsWith("/kick")){
                    String[] parts = msg.split(" ", 2);
                    if (parts.length < 2) {
                        sendMessage("[Система] Использование: /kick <имя>");
                        continue;
                    }
                    String targetName = parts[1];
                    boolean ok = server.kickUser(this, targetName);
                    if (!ok) {
                    }
                    continue;
                }
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

    public void forceDisconnectWithMessage(String reason) {
        try {
            sendMessage(reason);
        } catch (Exception ignored) {
        }
        disconnect();
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            disconnect();
        }
    }
}
