package testfinder;

import java.net.ConnectException;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.Select;


public class TestFinder {
    
    public static String baseURL = "https://scheduler.itialb4dmv.com/SchAlberta/Applicant/Information";
    public static ArrayList<Appointment> previousResults = new ArrayList();
    public static int interval = 10; //in seconds
    public static boolean headless = true;
    public static boolean noImages = true;
    
    //email stuff
    public static String to = "rdelorenzo5@gmail.com";
    public static String from = "rdelorenzo5@gmail.com";
    public static final String username = "rdelorenzo5@gmail.com"; //change accordingly
    public static final String password = "Dancemusic5"; //change accordingly
    public static String host = "smtp.gmail.com";
    public static int port = 465;
    
    public static void main(String[] args) {
        
        System.setProperty("webdriver.chrome.driver","chromedriver");
            
        ChromeOptions chromeOptions = new ChromeOptions();
        if (headless) chromeOptions.addArguments("--headless");
        if (noImages) chromeOptions.addArguments("--blink-settings=imagesEnabled=false");

        WebDriver driver = new ChromeDriver(chromeOptions);
        driver.get(baseURL);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                //close driver correctlywhenever application exits
                try {
                    driver.quit();
                } catch (WebDriverException e) {}
                System.out.println("Driver stopped correctly!");
            }
        });
        
        try {
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
            testDropDown.selectByValue("7"); //class 3 is value 5, class 5 is 7
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
                        appointments.add(newAppointment);
                    }
                    //compare previous result to current result
                    boolean different = false;
                    if (appointments.size() == previousResults.size()) {
                        for (int i = 0; i<appointments.size(); i++) {
                            if (!appointments.get(i).equals(previousResults.get(i))) different = true;
                        }
                    } else {
                        different = true;
                    }
                    if (different && !appointments.isEmpty()) {
                        //compose and send mail
                        sendEmail(appointments);
                    }
                } else {
                    //there are no openings
                }
                Date date = new Date(System.currentTimeMillis());
                DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                formatter.setTimeZone(TimeZone.getTimeZone("MST"));
                String dateFormatted = formatter.format(date);
                if (appointments.isEmpty()) {
                    System.out.println(dateFormatted + ": No Openings");
                } else {
                    System.out.println(dateFormatted + ": " + appointments.size() + " Opening(s) Found");
                }
                //transfer contents from current results to previous results
                previousResults = new ArrayList();
                for (Appointment a : appointments) {
                    previousResults.add(a);
                }
                while (driver.getCurrentUrl().equals("https://scheduler.itialb4dmv.com/SchAlberta/Appointment/Search")) {
                    content.findElement(By.cssSelector("button[onclick='goBack()']")).click();
                }
                Thread.sleep(interval*1000);  // Pause before trying again
            }
        } catch (InterruptedException e) {
            System.out.println("crashed");
        }
            
    }
    
    public static void sendEmail(ArrayList<Appointment> appointments) {
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.socketFactory.port", port);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(to)
            );
            message.setSubject("New Road Test Openings!");
            String emailText = "";
            for (Appointment a : appointments) {
                emailText += a.toString();
                emailText += "\n";
            }
            message.setText(emailText);

            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
