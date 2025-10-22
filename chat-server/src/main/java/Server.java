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

    public synchronized Role registerClient(ClientHandler client) {
        if (!hasAdmin()) {
            return Role.ADMIN;
        } else {
            return Role.USER;
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
        if (!hasAdmin() && !clients.isEmpty()) {
            ClientHandler promoted = clients.get(0);
            promoted.setRole(Role.ADMIN);
            broadcastMessage("[Система] " + promoted.getUsername() + " назначен(а) ADMINOM (по умолчанию)");

            promoted.sendMessage("[Система] Вы теперь ADMIN.");
        }
    }

    public boolean sendPrivateMessage(String username, String targetName, String privateMsg) {
        for(ClientHandler c: clients) {
            if (c.getUsername().equalsIgnoreCase(targetName)) {
                c.sendMessage("[ЛС от " + username + "]: " + privateMsg);
                for (ClientHandler sender : clients) {
                    if (sender.getUsername().equalsIgnoreCase(username)) {
                        sender.sendMessage("[ЛС для " + targetName + "]: " + privateMsg);
                        break;
                    }
                }
                return true;
            }
        }
        return false;
    }
    public synchronized boolean kickUser(ClientHandler requester, String targetName) {
        if (requester.getRole() != Role.ADMIN) {
            requester.sendMessage("[Система] У вас нет прав для выполнения /kick. Только ADMIN может кикать.");
            return false;
        }

        for (ClientHandler c : new ArrayList<>(clients)) {
            if (c.getUsername().equalsIgnoreCase(targetName)) {
                if (c == requester) {
                    requester.sendMessage("[Система] Нельзя кикнуть самого себя.");
                    return false;
                }

                broadcastMessage("[Система] " + c.getUsername() + " был(а) исключен(а) администратором " + requester.getUsername());

                c.forceDisconnectWithMessage("[Система] Вы были исключены администратором " + requester.getUsername());
                return true;
            }
        }

        requester.sendMessage("[Система] Пользователь '" + targetName + "' не найден.");
        return false;
    }
    private boolean hasAdmin() {
        for (ClientHandler c : clients) {
            if (c.getRole() == Role.ADMIN) return true;
        }
        return false;
    }

}
