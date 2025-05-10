import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class Server {
    public static final int PORT = 3030;
    private ServerSocket server;
    private Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();
    private int clientCount = 0;

    public Server() {
        try {
            this.server = new ServerSocket(3030);
            System.out.println("Server started on port 3030");

            while(true) {
                this.acceptClients();
            }
        } catch (IOException var2) {
            var2.printStackTrace();
        }
    }

    private void acceptClients() throws IOException {
        Socket var1 = this.server.accept();
        ++this.clientCount;
        String var2 = "User" + this.clientCount;
        ClientHandler type = new ClientHandler(var1, var2);
        this.clients.add(type);
        (new Thread(type)).start();
        System.out.println(var2 + " connected.");
    }

    public void broadcastMessage(String var1, String var2) {
        for(ClientHandler recipients : this.clients) {
            recipients.sendText("MSG|ALL|" + var1 + "|" + var2);
        }

    }

    public void sendMessageTo(String var1, String var2, String type) {
        for(ClientHandler sender : this.clients) {
            if (sender.getUsername().equals(var2)) {
                sender.sendText("MSG|" + var2 + "|" + var1 + "|" + type);
                break;
            }
        }

    }

    public void broadcastFile(String var1, String var2, byte[] type) {
        for(ClientHandler sender : this.clients) {
            sender.sendFile("FILE|ALL|" + var1 + "|" + var2, type);
        }

    }

    public void sendFileTo(String var1, String var2, String type, byte[] recipients) {
        for(ClientHandler content : this.clients) {
            if (content.getUsername().equals(var2)) {
                content.sendFile("FILE|" + var2 + "|" + var1 + "|" + type, recipients);
                break;
            }
        }

    }

    public static void main(String[] var0) {
        new Server();
    }

    private class ClientHandler implements Runnable {
        private Socket socket;
        private DataInputStream in;
        private DataOutputStream out;
        private String username;

        public ClientHandler(Socket var2, String type) {
            this.socket = var2;
            this.username = type;

            try {
                this.in = new DataInputStream(new BufferedInputStream(var2.getInputStream()));
                this.out = new DataOutputStream(var2.getOutputStream());
            } catch (IOException sender) {
                sender.printStackTrace();
            }

        }

        public String getUsername() {
            return this.username;
        }

        public void run() {
            try {
                while(true) {
                    String clientMessage = this.in.readUTF();
                    String[] headers = clientMessage.split("\\|", 4);
                    String type = headers[0];
                    String recipients = headers[1];
                    String sender = headers[2];
                    String content = headers[3];
                    if ("MSG".equals(type)) {
                        if ("ALL".equals(recipients)) {
                            Server.this.broadcastMessage(sender, content);
                        } else {
                            Server.this.sendMessageTo(sender, recipients, content);
                        }
                    } else if ("FILE".equals(type)) {
                        int var7 = this.in.readInt();
                        byte[] var8 = new byte[var7];
                        this.in.readFully(var8);
                        if ("ALL".equals(recipients)) {
                            Server.this.broadcastFile(sender, content, var8);
                        } else {
                            Server.this.sendFileTo(sender, recipients, content, var8);
                        }
                    }
                }
            } catch (IOException var16) {
                System.out.println(this.username + " disconnected.");
            } finally {
                try {
                    this.socket.close();
                } catch (IOException var15) {
                }

                Server.this.clients.remove(this);
            }

        }

        void sendText(String var1) {
            try {
                this.out.writeUTF(var1);
                this.out.flush();
            } catch (IOException type) {
                type.printStackTrace();
            }

        }

        void sendFile(String var1, byte[] var2) {
            try {
                this.out.writeUTF(var1);
                this.out.writeInt(var2.length);
                this.out.write(var2);
                this.out.flush();
            } catch (IOException recipients) {
                recipients.printStackTrace();
            }

        }
    }
}
