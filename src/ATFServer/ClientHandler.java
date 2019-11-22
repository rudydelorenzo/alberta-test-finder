package ATFServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import java.util.ArrayList;

import static ATFServer.atfServerMain.gsonContent;

import com.google.gson.internal.LinkedTreeMap;

public class ClientHandler extends Thread {
    final DataInputStream in; 
    final DataOutputStream out; 
    final Socket s;
    
    public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos) { 
        this.s = s; 
        this.in = dis; 
        this.out = dos;
    } 
  
    @Override
    public void run() { 
        String received; 
        while (true) { 
            try {
                // receive the answer from client 
                received = in.readUTF(); 
                
                System.out.println(received);
                
                String lines[] = received.split("\n");
                
                if (lines[0].split(":")[1].equals("subscribe")) {
                    LinkedTreeMap newSub = new LinkedTreeMap();
                    for (int i = 1; i < lines.length-1; i++) {
                        String line = lines[i];
                        String keyval[] = line.split(":");
                        newSub.put(keyval[0], keyval[1]);
                    }
                    ArrayList a = gsonContent.get(lines[lines.length-1].split(":")[1]);
                    a.add(newSub);
                }
                
                // write on output stream based on the input from the client
                switch (received) { 
                    default:
                        out.writeUTF("success:Success"); 
                        this.in.close();
                        this.out.close();
                        break;
                } 
            } catch (SocketException e) {
                System.out.println("Socket Closed");
                break;
            } catch (IOException e) { 
                e.printStackTrace(); 
            }
        } 
    }
}
