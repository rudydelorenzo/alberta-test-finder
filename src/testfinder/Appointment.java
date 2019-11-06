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
    
    public String getPlace() {
        return place;
    }
    
    public String getAddress() {
        return address;
    }
    
    public String getDate() {
        return date;
    }
    
    public String getTime() {
        return time;
    }
    
    @Override
    public String toString() {
        String s = "-------------- APPOINTMENT --------------\n";
        s += ("Place: " + place + "\n");
        s += ("Address: " + address + "\n");
        s += ("Date: " + date + "\n");
        s += ("Time: " + time + "\n");
        return s;
    }
    
    public boolean equals(Appointment comp) {
        if (!place.equals(comp.getPlace())) return false;
        if (!address.equals(comp.getAddress())) return false;
        if (!date.equals(comp.getDate())) return false;
        return time.equals(comp.getTime());
    }
    
}
