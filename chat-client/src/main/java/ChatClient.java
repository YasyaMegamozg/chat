import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class ChatClient extends JFrame {
    private JTextArea outTextArea;
    private JTextField inTextField;
    private JButton sendButton;
    private Network network;

    public ChatClient(String title, Network network) {
        super(title);
        this.network = network;

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

        // Важно! Обновляем GUI через Swing-поток
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
        try {
            Network network = new Network();
            network.connect(8080);
            new ChatClient("Chat Client", network);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Не удалось подключиться к серверу!");
        }
    }
}

