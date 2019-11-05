package testfinder;

import java.util.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.Select;


public class TestFinder {
    
    static String baseURL = "https://scheduler.itialb4dmv.com/SchAlberta/Applicant/Information";
    static ArrayList<Appointment> previousResult = new ArrayList();
    static int interval = 10; //in seconds
    
    public static void main(String[] args) {
        try {
            System.setProperty("webdriver.chrome.driver","chromedriver");
            
            WebDriver driver = new ChromeDriver();
            driver.get(baseURL);
            while (driver.findElement(By.tagName("h1")).getText().equals("Service Unavailable")) {
                // page is out of service, wait for a minute, then refresh
                Thread.sleep(60000);
                driver.get(baseURL);
            }
            //book an appointment
            WebElement bookTestButton = driver.findElement(By.id("btnBookAppt"));
            bookTestButton.click();
            //buy an appointment
            WebElement buyRoadTestButton = driver.findElement(By.id("invalidPermit"));
            buyRoadTestButton.click();
            //now we're entering driver info
            WebElement textBox = driver.findElement(By.id("FirstName"));
            textBox.sendKeys("Rodrigo");
            textBox = driver.findElement(By.id("LastName"));
            textBox.sendKeys("De Lorenzo");
            textBox = driver.findElement(By.id("MVID"));
            textBox.sendKeys("087065926");
            textBox = driver.findElement(By.id("Birthdate"));
            textBox.sendKeys("2001/03/09");
            textBox = driver.findElement(By.id("Email"));
            textBox.sendKeys("rdelorenzo5@gmail.com");
            WebElement acceptTerms = driver.findElement(By.id("labelAcceptTerms"));
            acceptTerms.click();
            textBox.submit();
            //now we're selecting the test we're doing
            Select testDropDown = new Select(driver.findElement(By.id("serviceGroupList")));
            testDropDown.selectByValue("5"); //class 3 is value 5, class 5 is 7
            WebElement acceptTestTerms  = driver.findElement(By.id("labelAcceptTerms"));
            boolean failed = true;
            while (failed) {
                try {
                    acceptTestTerms.click();
                    failed = false;
                } catch (ElementNotInteractableException e) {}
            }
            acceptTestTerms.submit();
            while (true) {
                //now we're selecting city and distance
                WebElement cityTextBox = driver.findElement(By.id("cityNameSearch"));
                cityTextBox.sendKeys("Edmonton");
                Select distanceDropdown = new Select(driver.findElement(By.id("citySearchRadius")));
                distanceDropdown.selectByValue("50");
                cityTextBox.submit();
                //in the test booking selection page
                WebElement content = driver.findElement(By.id("renderBodyContent"));
                ArrayList<Appointment> appointments = new ArrayList();
                if (content.findElements(By.className("text-danger")).isEmpty()) {
                    //there are openings
                    ArrayList<WebElement> cards = new ArrayList(content.findElements(By.className("card")));
                    for (WebElement card : cards) {
                        Appointment newAppointment = new Appointment();
                        newAppointment.setPlace(card.findElement(By.cssSelector("[id^=locationName]")).getText());
                        newAppointment.setAddress(card.findElement(By.className("card-body")).findElement(By.tagName("p")).getText());
                        newAppointment.setDate(card.findElement(By.cssSelector("[id^=dateVal]")).getText());
                        newAppointment.setTime(card.findElement(By.cssSelector("[id^=timeVal]")).getText());
                        //newAppointment.printInfo();
                        appointments.add(newAppointment);
                    }
                    //compare previous result to current result
                    boolean different = false;
                    if (appointments.size() == previousResult.size()) {
                        for (int i = 0; i<appointments.size(); i++) {
                            if (!appointments.get(i).equals(previousResult.get(i))) different = true;
                        }
                    } else {
                        different = true;
                    }
                    if (different) {
                        //compose and send mail
                    }
                } else {
                    //there are no openings
                    System.out.println("Veredict: No Openings");
                }
                //transfer contents from current results to previous results
                //TODO
                Thread.sleep(interval*1000);  // Pause before trying again
                content.findElement(By.cssSelector("button[onclick='goBack()']")).click();
                //flip two lines above for slightly better performance
            }
            //driver.quit(); //Shut down the program
        } catch (InterruptedException e) {
            System.out.println("crashed");
        }
    }
    
}
