package main;

public class main {
    public static void main(String[] args) {
        if (args.length > 0) {
            String noArgs[] = {""};
            switch(args[0]) {
                case "-h":
                    printHelp();
                    break;
                case "-help":
                    printHelp() ;
                    break;
                case "-r":
                    testfinder.instanceLauncher.main(noArgs);
                    break;
                case "-s":
                    ATFServer.atfServerMain.main(noArgs);
                    break;
                default:
                    printHelp();
                    break;
            }
        } else {
            printHelp();
        }
    }
    
    public static void printHelp() {
        String helpText = 
                "Welcome to the Alberta Test Finder launcher!\n"
                + "Here are the supported flags:\n"
                + "\t\"-r\"\tlaunches the test finder application.\n"
                + "\t\"-s\"\tlaunches the ATF server to accept new subscribers.\n"
                + "\t\"-h\"\tshows accepted flags\n"
                + "\t\"-help\"";
        System.out.println(helpText);
    }
    
}
