package testfinder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Appointment {
    
    private String place;
    //private int distance;
    private String address;
    private LocalDate date;
    private LocalTime time;
    private static DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static DateTimeFormatter tf = DateTimeFormatter.ofPattern("h:mm a");
    
    public Appointment() {}
    
    public void setPlace(String input) {
        place = input;
    }
    
    public void setAddress(String input) {
        address = input;
    }
    
    public void setDate(String input) {
        date = LocalDate.parse(input, df);
    }
    
    public void setTime(String input) {
        time = LocalTime.parse(input, tf);
    }
    
    public String getPlace() {
        return place;
    }
    
    public String getAddress() {
        return address;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public LocalTime getTime() {
        return time;
    }
    
    public String getDateString() {
        return date.format(df);
    }
    
    public String getTimeString() {
        return time.format(tf);
    }
    
    @Override
    public String toString() {
        String s = "-------------- OPENING FOUND --------------\n";
        s += ("Place: " + place + "\n");
        s += ("Address: " + address + "\n");
        s += ("Date: " + getDateString() + "\n");
        s += ("Time: " + getTimeString() + "\n");
        return s;
    }
    
    public boolean equals(Appointment comp) {
        if (!place.equals(comp.getPlace())) return false;
        if (!address.equals(comp.getAddress())) return false;
        if (!date.equals(comp.getDate())) return false;
        return time.equals(comp.getTime());
    }
    
}