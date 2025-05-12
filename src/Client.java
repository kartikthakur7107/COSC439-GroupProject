import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Scanner;
//import java.awt.Image;

public class Client {
    public static final String FILES_PATH = "./src/files";
    private Socket clientSock;
    private DataInputStream receiveData;
    private DataOutputStream sendData;
    private Scanner write;
    private String username;

    public Client() {
    try {
        write = new Scanner(System.in);
        System.out.print("Enter your Username: ");
        username = write.nextLine();

        //You can replace the IP address with the address of the host device of the server if you know it
        clientSock = new Socket("10.128.159.30", 3030);

        receiveData = new DataInputStream(new BufferedInputStream(clientSock.getInputStream()));
        sendData = new DataOutputStream(clientSock.getOutputStream());

        sendData.writeUTF("USER|" + username);
        sendData.flush();

        new Thread(this::listenForMessages).start();

        while (true) {
            clientMessages();
        }

    } catch (IOException e) {
        e.printStackTrace();
    }
}



    private void listenForMessages() {
        try {
             while (true) {
                String incoming = receiveData.readUTF();
                String[] parts = incoming.split("\\|", 4);

                 if (parts.length == 4 && parts[0].equals("MSG")) {
                    String to = parts[1];
                    String from = parts[2];
                    String content = parts[3];

                    if (to.equalsIgnoreCase("ALL")) {
                        System.out.println("\n[" + from + " → Everyone]: " + content);
                    } else if (to.equalsIgnoreCase(username)) {
                        System.out.println("\n[" + from + " → You]: " + content);
                    } else {
                        System.out.println("\n[" + from + " → " + to + "]: " + content);
                    }

                    } else if (parts.length == 4 && parts[0].equals("FILE")) {
                        String to = parts[1];
                        String from = parts[2];
                        String filename = parts[3];

                    int length = receiveData.readInt();
                    byte[] fileBytes = new byte[length];
                    receiveData.readFully(fileBytes);

                    File folder = new File("received");
                    if (!folder.exists()) {
                        folder.mkdir();
                    }

                    File outFile = new File(folder, filename);
                    Files.write(outFile.toPath(), fileBytes);

                    System.out.println("\n[File Received from " + from + "] Saved: received/" + filename);
                } else {
                    System.out.println("\n[Received]: " + incoming);
                }
            }
        } catch (IOException e) {
            System.out.println("Disconnected from server.");
        }
    }


    private void sendFile(String recipient) throws IOException {
        Menu(); // show file options
        System.out.print("Enter the number of the file to send: ");
        int fileIndex = Integer.parseInt(write.nextLine()) - 1;

        File[] files = new File(FILES_PATH).listFiles();
        if (fileIndex < 0 || fileIndex >= files.length) {
            System.out.println("Invalid file number.");
            return;
        }

        File selectedFile = files[fileIndex];
        byte[] fileBytes = Files.readAllBytes(selectedFile.toPath());

        String header = "FILE|" + recipient + "|" + username + "|" + selectedFile.getName();
        sendData.writeUTF(header);
        sendData.writeInt(fileBytes.length);
        sendData.write(fileBytes);
        sendData.flush();

        System.out.println("Sent file: " + selectedFile.getName() + " to " + recipient);
    }


    //Creates messages that the clients sends to the handler to be processed to the server
    private void clientMessages() throws IOException {
        while (true) {
            System.out.println("Send to (type username or 'ALL' for broadcast): ");
            String recipient = write.nextLine().trim();

            System.out.println("Choose action:");
            System.out.println("[1] Send Message");
            System.out.println("[2] Send File");
            System.out.println("[3] Quit");

            String choice = write.nextLine().trim();

            if (choice.equals("3") || choice.equalsIgnoreCase("quit")) {
                break;
            }

            switch (choice) {
                case "1":
                    System.out.print("Enter your message: ");
                    String message = write.nextLine();
                    String msg = "MSG|" + recipient + "|" + username + "|" + message;
                    sendData.writeUTF(msg);
                    sendData.flush();
                break;

                case "2":
                    sendFile(recipient);
                    break;

                 default:
                     System.out.println("Invalid choice. Please enter 1, 2, or 3.");
            }
        }   
    }


    //Displays all files within the given folder
    private void Menu() throws IOException{
        StringBuilder menu = new StringBuilder("--Files--\n");
        File[] fileList = new File(FILES_PATH).listFiles();

        for(int i = 0; i < fileList.length;i++)
            menu.append(String.format("* %d: %s\n", i + 1, fileList[i].getName()));
        System.out.print(menu);
    }

    private void close(){
        try {
            clientSock.close();
            sendData.close();
            receiveData.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        new Client();
    }
}