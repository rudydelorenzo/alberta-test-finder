package ATFServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class atfServerMain {
    
    public static ServerSocket serverSocket;
    public static Socket clientSocket;
    public static JSONObject json;
    
    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(4444);
            
            Object obj = new JSONParser().parse(new FileReader("subs.json"));
            json = (JSONObject) obj;
            
            //if document was read correctly, schedule minute autosaves
            Timer autoSave = new Timer();
            autoSave.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        FileWriter file = new FileWriter("subs.json");
                        file.write(json.toJSONString());
                        file.close();
                        
                        System.out.println("Saved \"subs.json\"");
                    } catch (IOException e) {
                        System.out.println("IOException while saving file...");
                        e.printStackTrace();
                    }
                }
                
            }, 60000, 60000); //first runs 60 seconds in, then runs every 60 seconds
            
            while (true) {
                clientSocket = null;
                clientSocket = serverSocket.accept();
                System.out.println("Connected to " + clientSocket.getInetAddress());
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                
                Thread t = new ClientHandler(clientSocket, in, out);
                t.start();
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
    
}
