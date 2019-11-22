package testfinder;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.internal.LinkedTreeMap;

import java.io.FileNotFoundException;
import java.io.FileReader;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class instanceLauncher {
    
    public static String subsFileName = "subs.json";
    public static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    public static void main(String[] args) {
        
        ArrayList<Thread> threads = new ArrayList();
        
        try {
            JsonReader reader = new JsonReader(new FileReader(subsFileName));
            LinkedHashMap<String,ArrayList> gsonContent = gson.fromJson(reader, LinkedHashMap.class);

            for (String test : gsonContent.keySet()) {
                ArrayList<String> a = new ArrayList();
                for (Object tmpSub : gsonContent.get(test)) {
                    LinkedTreeMap sub = (LinkedTreeMap) tmpSub;
                    a.add(sub.get("email").toString());
                }
                //if list of emails isn't empty, start the testFinder and pass it in
                if (!a.isEmpty()) {
                    threads.add(new TestFinder(test, a));
                    threads.get(threads.size()-1).start();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    
}
