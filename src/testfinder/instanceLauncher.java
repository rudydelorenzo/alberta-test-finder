package testfinder;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.internal.LinkedTreeMap;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class instanceLauncher {
    
    public static String subsFileName = "subs.json";
    public static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static ArrayList<TestFinder> threads = new ArrayList();
    
    public static void main(String[] args) {
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                kill();
            }
        });

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Edmonton"));
        ZonedDateTime nextRun = now.withHour(0).withMinute(0).withSecond(0);
        if(now.compareTo(nextRun) > 0)
            nextRun = nextRun.plusDays(1);

        Duration duration = Duration.between(now, nextRun);
        long initalDelay = duration.getSeconds();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);            
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                kill();
                threads = new ArrayList();
                launch();
            }
        }, initalDelay, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
        
        launch();
        
    }
    
    public static void launch() {
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
    
    public static void kill() {
        for (TestFinder t : threads) {
            t.endThread();
        }
        
        while (threadsRunning(threads)) {}
        
        System.out.println("All threads stopped.");
        
    }
    
    public static boolean threadsRunning(ArrayList<TestFinder> list) {
        boolean threadsRunning = false;
        
        for (Thread currentThread : list) {
            if (currentThread.isAlive()) threadsRunning = true;
        }
        return threadsRunning;
    }    
}