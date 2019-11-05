package testfinder;

import java.text.DateFormat;
import java.util.Date;

public class Appointment {
    
    private String place;
    //private int distance;
    private String address;
    private String date;
    private String time;
    
    public Appointment() {
        
    }
    
    public void setPlace(String input) {
        place = input;
    }
    
    public void setAddress(String input) {
        address = input;
    }
    
    public void setDate(String input) {
        date = input;
    }
    
    public void setTime(String input) {
        time = input;
    }
    
    public void printInfo() {
        System.out.println("-------------- APPOINTMENT --------------");
        System.out.println("Place: " + place);
        System.out.println("Address: " + address);
        System.out.println("Date: " + date);
        System.out.println("Time: " + time);
        System.out.println();
    }
    
    public boolean equals(Appointment comp) {
        return true;
    }
    
}
