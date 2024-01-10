import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ServerSide
{
    private static volatile boolean serverOpen = true;
    private static List<DataOutputStream> clientOutputStreams = new ArrayList<>();

    private static JTextField txtPort;
    private static JButton btnStartServer;
    private static JTextArea txtServerMessages;
    private static JTextField txtServerSend;
    private static LogWriter logger = new LogWriter();

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> {
            handleServerGUI();
        });
    }

    private static void handleServerGUI() {
        JPanel panel = new JPanel();
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        panel.setBackground(new Color(178, 200, 186)); // Light green background

        JLabel lblPort = new JLabel("Port:");
        lblPort.setForeground(new Color(64, 79, 64)); // Green text color

        txtPort = new JTextField();

        txtPort.setBackground(new Color(210, 227, 200)); // Light green background
        txtPort.setForeground(new Color(64, 79, 64)); // Green text color

        btnStartServer = new JButton("Start Server");
        btnStartServer.setBackground(new Color(235, 243, 232)); // Ivory color
        btnStartServer.setForeground(new Color(64, 79, 64)); // Green text color
        btnStartServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String portText = txtPort.getText();
                int port = Integer.parseInt(portText);
                startServer(port);
            }
        });

        txtServerMessages = new JTextArea();
        txtServerMessages.setEditable(false);
        txtServerMessages.setBackground(new Color(235, 243, 232)); // Ivory background
        txtServerMessages.setForeground(new Color(64, 79, 64)); // Green text color
        JScrollPane scrollPane = new JScrollPane(txtServerMessages, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        txtServerSend = new JTextField();
        txtServerSend.setBackground(new Color(235, 243, 232)); // Ivory background
        txtServerSend.setForeground(new Color(64, 79, 64)); // Green text color
        txtServerSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!txtServerSend.getText().isEmpty())
                {
                    String message = txtServerSend.getText();
                    int[] cryptedMessage = Crypting.Encrypt(message);

                    serverSendMessage(cryptedMessage);
                    appendServerMessage("Server", message);
                    txtServerSend.setText("");
                }
            }
        });

        JButton btnServerSend = new JButton("Send");
        btnServerSend.setBackground(new Color(178, 200, 186)); // Light green background
        btnServerSend.setForeground(new Color(64, 79, 64)); // Green text color
        btnServerSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!txtServerSend.getText().isEmpty())
                {
                    String message = txtServerSend.getText();
                    int[] cryptedMessage = Crypting.Encrypt(message);

                    serverSendMessage(cryptedMessage);
                    appendServerMessage("Server", message);
                    txtServerSend.setText("");
                }
            }
        });

        // Set GroupLayout
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(lblPort)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtPort, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE) // Adjusted size
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnStartServer))
                .addComponent(scrollPane)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(txtServerSend)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnServerSend)));

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(lblPort)
                        .addComponent(txtPort)
                        .addComponent(btnStartServer))
                .addComponent(scrollPane)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(txtServerSend)
                        .addComponent(btnServerSend)));

        JFrame frame = new JFrame("ServerSide");
        frame.setLocation(300, 300);
        frame.setSize(400, 400);
        frame.add(panel);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static void startServer(int port) {
        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(port);
                String text1 = "Server is waiting for a connection...";
                System.out.println("Server is waiting for a connection...");
                logger.logTextGenerator(text1);

                // Add a shutdown hook to handle server closure
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    closeServer();
                }));

                // Disable GUI components after starting the server
                SwingUtilities.invokeLater(() -> {
                    txtPort.setEnabled(false);
                    btnStartServer.setEnabled(false);
                });

                while (serverOpen) {
                    Socket socket = serverSocket.accept();
                    String text2 = "Client connected: " + socket.getInetAddress();
                    System.out.println(text2);
                    logger.logTextGenerator(text2);

                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    clientOutputStreams.add(dos);

                    new Thread(() -> handleClientInput(socket)).start();
                }

                // Server is closed, perform cleanup
                serverSocket.close();
                String text3 = "Server Closed";
                System.out.println(text3);
                logger.logTextGenerator(text3);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void handleClientInput(Socket socket) {
        try {
            InputStream s1In = socket.getInputStream();
            DataInputStream dis = new DataInputStream(s1In);

            while (serverOpen)
            {
                int len = dis.readInt();
                int[] encryptedMessages = new int[len];

                for (int i = 0; i < len; i++) {
                    encryptedMessages[i] = dis.readInt();
                }

                String str = Crypting.Decrypt(encryptedMessages);

                appendServerMessage("Client", str);
                logger.logTextGenerator("Client : " + str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void serverSendMessage(int[] message) {
        for (DataOutputStream clientOutputStream : clientOutputStreams) {
            try
            {
                clientOutputStream.writeInt(message.length);

                for (int value : message)
                {
                    clientOutputStream.writeInt(value);
                }

                logger.logTextGenerator("Server : "+Crypting.Decrypt(message));

                clientOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private static void appendServerMessage(String user, String message) {
        SwingUtilities.invokeLater(() -> {
            txtServerMessages.append(user + ": " + message + "\n");
        });
    }

    private static void closeServer() {
        logger.log(logger.getLogTxt());
        serverOpen = false;
    }
}