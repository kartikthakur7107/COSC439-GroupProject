import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import java.awt.Image;

public class Client {
    public static final String FILES_PATH = "./src/files";
    private Socket clientSock;
    private DataInputStream receiveData;
    private DataOutputStream sendData;
    private Scanner write;
    private String line;

    public Client() {
        try{
            clientSock = new Socket("127.0.0.1",Server.PORT);
            receiveData = new DataInputStream(new BufferedInputStream(clientSock.getInputStream()));
            sendData = new DataOutputStream(clientSock.getOutputStream());
            write = new Scanner(System.in);
            while(true){
                clientMessages();
            }
            //close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    //Creates messages that the clients sends to the handler to be processed to the server
    private void clientMessages() throws IOException{
        //We want to use this String as the data we send to the server
        String message = null;

        //This is an intermediary variable that will build message
        line = "";

        Menu();
        while(!line.equals(" ")){
            line = write.nextLine();
            sendData.writeUTF(message);
        }

    }

    //Displays all files within the given folder
    private void Menu() throws IOException{
        StringBuilder menu = new StringBuilder("--Files--\n");
        File[] fileList = new File(FILES_PATH).listFiles();

        for(int i = 0; i< fileList.length;i++)
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