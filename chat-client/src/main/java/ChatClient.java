import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class ChatClient extends JFrame {
    private JTextArea outTextArea;
    private JTextField inTextField;
    private JButton sendButton;
    private Network network;
    private String username;

    public ChatClient(String title, Network network, String username) {
        super(title);
        this.network = network;
        this.username = username;

        setLayout(new BorderLayout());
        outTextArea = new JTextArea();
        outTextArea.setEditable(false);
        add(new JScrollPane(outTextArea), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        inTextField = new JTextField();
        sendButton = new JButton("Send");
        bottomPanel.add(inTextField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        network.setCallback(args ->
                SwingUtilities.invokeLater(() -> outTextArea.append(args[0] + "\n"))
        );

        sendButton.addActionListener(e -> sendMessage());
        inTextField.addActionListener(e -> sendMessage());

        setSize(400, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void sendMessage() {
        String text = inTextField.getText().trim();
        if (!text.isEmpty()) {
            try {
                network.sendMessage(text);
                inTextField.setText("");
            } catch (IOException e) {
                outTextArea.append("[Ошибка отправки сообщения]\n");
            }
        }
    }

    public static void main(String[] args) {
        JTextField loginField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        Object[] fields = {
                "Логин:", loginField,
                "Пароль:", passwordField
        };
        int option = JOptionPane.showConfirmDialog(null, fields, "Вход в чат", JOptionPane.OK_CANCEL_OPTION);
        if (option != JOptionPane.OK_OPTION) return;

        String login = loginField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (login.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Поля не могут быть пустыми!");
            return;
        }

        try {
            Network network = new Network();
            network.connect(8080);

            network.sendMessage(login);
            network.sendMessage(password);

            new ChatClient("Chat Client", network, login);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Не удалось подключиться к серверу!");
        }
    }
}

