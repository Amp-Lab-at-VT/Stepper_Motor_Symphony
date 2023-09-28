package sms;

import java.io.IOException;

/**
 * Starts the program
 * 
 * @author Dylan Green
 * @version 2021.07.21
 */
public class Main {
  
    public static void main(String[] args) {

        CommandPrompt c = new CommandPrompt();
        try {
            c.run();
        } catch (IOException e) {
            System.err.println("The console could not be read from. Exiting...");
        }

    }
}
 

