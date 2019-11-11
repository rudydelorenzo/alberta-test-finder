package testfinder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
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

//todo: add author info, add support for multiple platforms (different chromedrivers),
//make way to launch easily, add support for firefox (not necessary)
//make first github release

public class TestFinder {
    
    public static String baseURL = "https://scheduler.itialb4dmv.com/SchAlberta/Applicant/Information";
    public static ArrayList<Appointment> previousResults = new ArrayList();
    public static int interval = 10; //in seconds
    public static WebDriver driver;
    public static boolean headless = true;
    public static boolean noImages = true;
    
    //email stuff
    public static String from = "rdelorenzo5@gmail.com";
    public static final String username = "rdelorenzo5@gmail.com"; //change accordingly
    public static final String password = "Dancemusic5"; //change accordingly
    public static String host = "smtp.gmail.com";
    public static int port = 465;
    
    public static void main(String[] args) {
        
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
        
        //use chrome
        String os = System.getProperty("os.name");
        if (os.toLowerCase().contains("mac")) {
            System.setProperty("webdriver.chrome.driver","chromedriver");
        } else if (os.toLowerCase().contains("linux")) {
            System.setProperty("webdriver.chrome.driver","/usr/bin/chromedriver");
        }
        
        ChromeOptions chromeOptions = new ChromeOptions();
        if (headless) chromeOptions.addArguments("--headless");
        if (noImages) chromeOptions.addArguments("--blink-settings=imagesEnabled=false");
        driver = new ChromeDriver(chromeOptions);
        
        while (true) {
            //read JSON file, this'll get refreshed daily when the website goes
            //down for maintainance since it'll throw a NoSuchElementFound ex
            ArrayList<String> emailList = new ArrayList();
            emailList.add("rdelorenzo5@gmail.com");
            ArrayList<Thread> threads = new ArrayList();
            try {
                startTesting(Test.CLASS_5_BASIC, emailList);
            } catch (org.openqa.selenium.NoSuchElementException e) {
                System.out.println("Crash in main loop (NSEException thrown), restarting...");
            }
        }
    }
    
    public static void startTesting(String test, ArrayList<String> emails) throws org.openqa.selenium.NoSuchElementException {
        //params: test=test option to select (what class?)
        driver.get(baseURL);
        
        try {
            
            while (!isReachable("www.google.com")) {
                //computer is offline
                System.out.println("No internet connection, retrying in 10 seconds...");
                Thread.sleep(10000);
            }
            
            while (driver.findElement(By.tagName("h1")).getText().equals("Service Unavailable")) {
                // page is out of service, wait for a minute, then refresh
                System.out.println("Page in maintainance or offline, waiting for a minute...");
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
            testDropDown.selectByValue(test);
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
                        boolean returnValEmail = sendEmail(appointments, emails);
                        if (returnValEmail) {
                            System.out.println("Email sent");
                        } else {
                            System.out.println("Email failed to send");
                        }
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
            System.out.println("InterruptedException thrown");
        }
    }
    
    public static boolean sendEmail(ArrayList<Appointment> appointments, ArrayList<String> emails) {
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
            for (String emailString : emails) {
                message.addRecipient(
                    Message.RecipientType.CC,
                    InternetAddress.parse(emailString)[0]
                );
            }
            message.setSubject("New Road Test Openings!");
            String emailText = "";
            for (Appointment a : appointments) {
                emailText += a.toString();
                emailText += "\n";
            }
            message.setText(emailText);

            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        } catch (NoClassDefFoundError f) {
            System.out.println("Top of call stack reached, no class def existed.");
            return false;
        }
    }
    
    private static boolean isReachable(String addr) {
        // Any Open port on other machine
        // openPort =  22 - ssh, 80 or 443 - webserver, 25 - mailserver etc.
        try {
            try (Socket soc = new Socket()) {
                soc.connect(new InetSocketAddress(addr, 443), 20000);
            }
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

}
