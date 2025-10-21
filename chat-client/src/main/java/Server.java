import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private int port;
    private List<ClientHandler> clients = new ArrayList<>();

    public Server(int port) {
        this.port = port;
    }

    public void start() {
        System.out.println("Сервер запущен на порту " + port);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(socket, this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void subscribe(ClientHandler client) {
        clients.add(client);
        broadcastMessage("[" + client.getUsername() + "] подключился к чату");
    }

    public synchronized void unsubscribe(ClientHandler client) {
        clients.remove(client);
        broadcastMessage("[" + client.getUsername() + "] вышел из чата");
    }

    public synchronized void broadcastMessage(String message) {
        for (ClientHandler c : clients) {
            c.sendMessage(message);
        }
    }

    public static void main(String[] args) {
        new Server(8080).start();
    }
}
