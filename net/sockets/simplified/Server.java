package net.sockets.simplified;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    public static final int PORT = 3030;
    private ServerSocket server;
    private Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();
    private int clientCount = 0;

    public Server() {
        try {
            server = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);
            while (true) {
                acceptClients();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void acceptClients() throws IOException {
        Socket clientSocket = server.accept();
        clientCount++;
        String username = "User" + clientCount;
        ClientHandler client = new ClientHandler(clientSocket, username);
        clients.add(client);
        new Thread(client).start();
        System.out.println(username + " connected.");
    }

    // Broadcast text message
    public void broadcastMessage(String from, String message) {
        for (ClientHandler client : clients) {
            client.sendText("MSG|ALL|" + from + "|" + message);
        }
    }

    // Send text message to a single user
    public void sendMessageTo(String from, String to, String message) {
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(to)) {
                client.sendText("MSG|" + to + "|" + from + "|" + message);
                break;
            }
        }
    }

    // Broadcast file to all
    public void broadcastFile(String from, String fileName, byte[] data) {
        for (ClientHandler client : clients) {
            client.sendFile("FILE|ALL|" + from + "|" + fileName, data);
        }
    }

    // Send file to a single user
    public void sendFileTo(String from, String to, String fileName, byte[] data) {
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(to)) {
                client.sendFile("FILE|" + to + "|" + from + "|" + fileName, data);
                break;
            }
        }
    }

    public static void main(String[] args) {
        new Server();
    }

    private class ClientHandler implements Runnable {
        private Socket socket;
        private DataInputStream in;
        private DataOutputStream out;
        private String username;

        public ClientHandler(Socket socket, String username) {
            this.socket = socket;
            this.username = username;
            try {
                in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                out = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public String getUsername() {
            return username;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    String header = in.readUTF(); // e.g. "MSG|target|sender|payload" or "FILE|target|sender|filename"
                    String[] parts = header.split("\\|", 4);
                    String type = parts[0];
                    String target = parts[1];
                    String from = parts[2];
                    String payload = parts[3];

                    // TEXT MESSAGE
                    if ("MSG".equals(type)) {
                        if ("ALL".equals(target)) {
                            broadcastMessage(from, payload);
                        } else {
                            sendMessageTo(from, target, payload);
                        }

                    // FILE TRANSFER
                    } else if ("FILE".equals(type)) {
                        int fileSize = in.readInt();
                        byte[] data = new byte[fileSize];
                        in.readFully(data);
                        if ("ALL".equals(target)) {
                            broadcastFile(from, payload, data);
                        } else {
                            sendFileTo(from, target, payload, data);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println(username + " disconnected.");
            } finally {
                try {
                    socket.close();
                } catch (IOException ignored) {}
                clients.remove(this);
            }
        }

        // Helper to send text
        void sendText(String textHeader) {
            try {
                out.writeUTF(textHeader);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Helper to send a file
        void sendFile(String fileHeader, byte[] data) {
            try {
                out.writeUTF(fileHeader);
                out.writeInt(data.length);
                out.write(data);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
