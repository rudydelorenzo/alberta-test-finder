package ATFServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import java.util.HashMap;

import org.json.simple.*;

public class ClientHandler extends Thread {
    final DataInputStream in; 
    final DataOutputStream out; 
    final Socket s;
    final HashMap<String,String> data;
    
    public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos) { 
        this.s = s; 
        this.in = dis; 
        this.out = dos;
        data = new HashMap<>();
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
                for (String line : lines) {
                    String keyval[] = line.split(":");
                    data.put(keyval[0], keyval[1]);
                }
                
                if (data.get("action").equals("subscribe")) {
                    //trying to subscribe
                }
                
                // write on output stream based on the input from the client
                switch (received) { 
                    default:
                        out.writeUTF("success:success"); 
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
