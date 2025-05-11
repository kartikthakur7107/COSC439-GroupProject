//import java.io.BufferedInputStream;
//import java.io.DataInputStream;
//import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

//import javax.imageio.ImageIO;
//import javax.swing.ImageIcon;
//import javax.swing.JLabel;

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
        ClientHandler type = new ClientHandler(this, var1, null); // Username will be set later
        this.clients.add(type);
        (new Thread(type)).start();
        System.out.println("New client connected. Waiting for username...");   
    }

    public void broadcastMessage(String sender, String message) {
        for (ClientHandler recipient : this.clients) {
        recipient.sendText("MSG|ALL|" + sender + "|" + message);
        }
    }


    public void sendMessageTo(String sender, String recipient, String message) {
        for (ClientHandler client : this.clients) {
            if (client.getUsername().equalsIgnoreCase(recipient)) {
                client.sendText("MSG|" + recipient + "|" + sender + "|" + message);
                break;
            }
        }
    }


    public void broadcastFile(String sender, String filename, byte[] data) {
        for (ClientHandler client : this.clients) {
            client.sendFile("FILE|ALL|" + sender + "|" + filename, data);
        }
    }


    public void sendFileTo(String sender, String recipient, String filename, byte[] data) {
        for (ClientHandler client : this.clients) {
            if (client.getUsername().equalsIgnoreCase(recipient)) {
                client.sendFile("FILE|" + recipient + "|" + sender + "|" + filename, data);
                break;
            }
        }
    }


    public void removeClient(ClientHandler client){
        clients.remove(client);
    }

    public static void main(String[] var0) {
        new Server();
    }
   
}
