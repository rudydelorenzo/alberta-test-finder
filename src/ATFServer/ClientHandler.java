package ATFServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import static ATFServer.atfServerMain.json;

import org.json.simple.*;

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
                    JSONObject newSub = new JSONObject();
                    for (int i = 1; i < lines.length-1; i++) {
                        String line = lines[i];
                        String keyval[] = line.split(":");
                        newSub.put(keyval[0], keyval[1]);
                    }
                    JSONArray a = (JSONArray) json.get(lines[lines.length-1]);
                    a.add(newSub);
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
