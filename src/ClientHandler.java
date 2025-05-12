import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {
   private Server server;
   private Socket socket;
   private DataInputStream in;
   private DataOutputStream out;
   private String username;

   public ClientHandler(Server var1, Socket var2, String var3) {
      this.server = var1;
      this.socket = var2;
      this.username = var3;

      try {
         this.in = new DataInputStream(new BufferedInputStream(var2.getInputStream()));
         this.out = new DataOutputStream(var2.getOutputStream());
      } catch (IOException var5) {
         var5.printStackTrace();
      }

   }

   public String getUsername() {
      return this.username;
   }

   public void run() {
      try {
         // Step 1: Receive the real username from the client
         String firstMessage = this.in.readUTF();
         if (firstMessage.startsWith("USER|")) {
            this.username = firstMessage.substring(5).trim();
            System.out.println("[Server] Username received: " + username);
         } else {
            System.out.println("[Server] Invalid first message from client: " + firstMessage);
            return;
         }

         // Step 2: Begin normal message loop
         while (true) {
            String var1 = this.in.readUTF();
            String[] var2 = var1.split("\\|", 4);

               if (var2.length < 4) {
                System.out.println("Invalid Message format from " + username + ": " + var1);
                continue;
               }

               String var3 = var2[0]; // MSG or FILE
               String var4 = var2[1]; // recipient
               String var5 = var2[2]; // sender
               String var6 = var2[3]; // message or filename

               if ("MSG".equals(var3)) {
                   if ("ALL".equalsIgnoreCase(var4)) {
                    this.server.broadcastMessage(var5, var6);
                  } else {
                   this.server.sendMessageTo(var5, var4, var6);
                   }
               } else if ("FILE".equals(var3)) {
                  int length = this.in.readInt();
                  byte[] fileBytes = new byte[length];
                  this.in.readFully(fileBytes);

                  if ("ALL".equalsIgnoreCase(var4)) {
                    this.server.broadcastFile(var5, var6, fileBytes);
                  } else {
                    this.server.sendFileTo(var5, var4, var6, fileBytes);
                  }
               }
         }
      } catch (IOException e) {
        System.out.println(this.username + " disconnected.");
      } finally {
          try {
            this.socket.close();
         } catch (IOException ignored) {}
          this.server.removeClient(this);
      }
   }


   void sendText(String var1) {
      try {
         this.out.writeUTF(var1);
         this.out.flush();
      } catch (IOException var3) {
         var3.printStackTrace();
      }

   }

   void sendFile(String var1, byte[] var2) {
      try {
         this.out.writeUTF(var1);
         this.out.writeInt(var2.length);
         this.out.write(var2);
         this.out.flush();
      } catch (IOException var4) {
         var4.printStackTrace();
      }

   }
}