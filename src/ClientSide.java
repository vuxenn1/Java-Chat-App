import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ClientSide
{
    private static Socket socket;
    private static JTextArea txtMessage;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            handleClientGUI();
        });
    }

    private static void handleClientGUI() {
        JPanel panel = new JPanel();
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        panel.setBackground(new Color(172, 189, 186));

        JLabel lblIP = new JLabel("Server IP:");
        JLabel lblPort = new JLabel("Port:");

        JTextField txtIP = new JTextField();
        JTextField txtPort = new JTextField();

        JButton btnConnect = new JButton("Connect");
        btnConnect.setBackground(new Color(98, 185, 119));
        btnConnect.setForeground(new Color(255, 255, 255));
        btnConnect.setFocusPainted(false); // Remove focus border
        btnConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String serverIP = txtIP.getText();
                int port = Integer.parseInt(txtPort.getText());
                connectToServer(serverIP, port);

                // Disable the components after connecting
                txtIP.setEnabled(false);
                txtPort.setEnabled(false);
                btnConnect.setEnabled(false);
            }
        });

        txtMessage = new JTextArea();
        txtMessage.setEditable(false);
        txtMessage.setBackground(new Color(205, 221, 221));
        JScrollPane scrollPane = new JScrollPane(txtMessage, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JTextField txtSend = new JTextField();
        txtSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (!txtSend.getText().isEmpty()) {
                    String message = txtSend.getText();
                    int[] cryptedMessage = Crypting.Encrypt(message);

                    sendMessage(cryptedMessage);
                    appendMessage("Client", message);
                    txtSend.setText("");
                }
            }
        });
        txtSend.setBackground(new Color(205, 221, 221));

        JButton btnSend = new JButton("Send");
        btnSend.setBackground(new Color(23, 195, 178));
        btnSend.setForeground(new Color(255, 255, 255));
        btnSend.setFocusPainted(false); // Remove focus border
        btnSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (!txtSend.getText().isEmpty()) {
                    String message = txtSend.getText();
                    int[] cryptedMessage = Crypting.Encrypt(message);

                    sendMessage(cryptedMessage);
                    appendMessage("Client", message);
                    txtSend.setText("");
                }
            }
        });

        // Set GroupLayout
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(lblIP)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtIP, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblPort)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtPort, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnConnect, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addComponent(scrollPane)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(txtSend, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSend, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)));

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(lblIP)
                        .addComponent(txtIP, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblPort)
                        .addComponent(txtPort, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnConnect, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addComponent(scrollPane)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(txtSend, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnSend, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)));

        JFrame frame = new JFrame("ClientSide");
        frame.setLocation(300, 300);
        frame.setSize(400, 400);
        frame.add(panel);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static void connectToServer(String serverIP, int port) {
        try {
            socket = new Socket(serverIP, port);
            System.out.println("Connected to the server.");
            new Thread(() -> handleServerInput(socket)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendMessage(int[] message) {
        try {
            OutputStream socketOutput = socket.getOutputStream();
            DataOutputStream dos = new DataOutputStream(socketOutput);

            dos.writeInt(message.length);

            for(int value : message)
            {
                dos.writeInt(value);
            }
            dos.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleServerInput(Socket socket) {
        try {
            InputStream socketInput = socket.getInputStream();
            DataInputStream dis = new DataInputStream(socketInput);

            while (socket.isConnected()) {
                int len = dis.readInt();
                int[] encryptedMessages = new int[len];

                for (int i = 0; i < len; i++)
                {
                    encryptedMessages[i] = dis.readInt();
                }

                String str = Crypting.Decrypt(encryptedMessages);
                appendMessage("Server", str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void appendMessage(String user, String message) {
        SwingUtilities.invokeLater(() -> {
            txtMessage.append(user + ": " + message + "\n");
        });
    }
}