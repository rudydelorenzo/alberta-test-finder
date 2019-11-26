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
//make first github release7 

public class TestFinder extends Thread implements EndThreadInterface {
    
    public String baseURL = "https://scheduler.itialb4dmv.com/SchAlberta/Applicant/Information";
    public ArrayList<Appointment> previousResults = new ArrayList();
    public int interval = 10; //in seconds
    public WebDriver driver;
    public boolean headless = true;
    public boolean noImages = true;
    public String test;
    public ArrayList<String> emails = new ArrayList();
    
    //email stuff
    public String from = "artnotifications@gmail.com";
    public final String username = "artnotifications@gmail.com"; //change accordingly
    public final String password = "ilovealberta!12"; //change accordingly
    public String host = "smtp.gmail.com";
    public int port = 465;
    public Session session;
    
    public TestFinder(String test, ArrayList<String> emails) {
        this.test = test;
        //next two lines only for testing
        this.emails = emails;
        //log into gmail
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.socketFactory.port", port);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        
        session = Session.getInstance(props,
            new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
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
    }
    
    @Override
    public void run() throws WebDriverException {
        System.out.println("STARTED INSTANCE: " + test);
        
        while (true) {
            try {
                startTesting();
            } catch (org.openqa.selenium.NoSuchElementException e) {
                println("Crash in main loop (NSEException thrown), restarting..." + e.getStackTrace());
            }
        }
    }
    
    public void startTesting() throws org.openqa.selenium.NoSuchElementException {
        driver.get(baseURL);
        
        try {
            
            while (!isReachable("www.google.com")) {
                //computer is offline
                println("No internet connection, retrying in 10 seconds...");
                Thread.sleep(10000);
            }
            
            while (driver.findElement(By.tagName("h1")).getText().equals("Service Unavailable")) {
                // page is out of service, wait for a minute, then refresh
                println("Page in maintainance or offline, waiting for a minute...");
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
            testDropDown.selectByValue(Test.translateClassToValue(test));
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
                            println("Email sent");
                        } else {
                            println("Email failed to send");
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
                    println(dateFormatted + ": No Openings");
                } else {
                    println(dateFormatted + ": " + appointments.size() + " Opening(s) Found");
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
            println("InterruptedException thrown");
        }
    }
    
    public boolean sendEmail(ArrayList<Appointment> appointments, ArrayList<String> emails) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            for (String emailString : emails) {
                message.addRecipient(
                    Message.RecipientType.BCC,
                    InternetAddress.parse(emailString)[0]
                );
            }
            message.setSubject("New Road Test Openings! (" + test.replace("_", " ") + ")");
            String emailText = "To book your appointment go to https://scheduler.itialb4dmv.com/SchAlberta/Applicant/Information\n\n";
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
            println("NoClassDef while trying to send email, probably an error with activation.jar");
            return false;
        }
    }
    
    private boolean isReachable(String addr) {
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
    
    private void println(String toPrint) {
        System.out.println("Thread " + test.toUpperCase() + ": " + toPrint);
    }

    @Override
    public void endThread() {
        driver.quit();
        println("ENDED THREAD");
    }

}