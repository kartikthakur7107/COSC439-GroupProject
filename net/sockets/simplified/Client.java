package net.sockets.simplified;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
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
        String message;
        line = "";
        while(!line.equals(" ")){
            line = write.nextLine();

            sendData.writeUTF(message);
        }
    }

    private void close(){
        try {
            clientSock.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        new Client();
    }
}
