package ATFServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.Gson;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class atfServerMain {
    
    public static ServerSocket serverSocket;
    public static Socket clientSocket;
    public static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static LinkedHashMap<String,ArrayList> gsonContent;
    
    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(4444);
            
            JsonReader reader = new JsonReader(new FileReader("subs.json"));
            gsonContent = gson.fromJson(reader, LinkedHashMap.class);
            
            //if document was read correctly, schedule minute autosaves
            Timer autoSave = new Timer();
            autoSave.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        FileWriter file = new FileWriter("subs.json");
                        file.write(gson.toJson(gsonContent));
                        file.close();
                        
                        Date date = new Date(System.currentTimeMillis());
                        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                        formatter.setTimeZone(TimeZone.getTimeZone("MST"));
                        String dateFormatted = formatter.format(date);
                        System.out.println(dateFormatted + ": " + "Saved \"subs.json\"");
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
