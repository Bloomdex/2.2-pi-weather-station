package org.bloomdex.client;

import java.util.Scanner;

public class Test {
    /**
     * The constructor of the Test class
     * @throws Exception when something goes wrong with making the client
     */
    Test() throws Exception {
        ClientManager.CreateClient();

        // A new user input scanner will be made
        Scanner in = new Scanner(System.in);

        while (true) {
            // Reads the user input
            String s = in.nextLine();
            // Translates user input to a byte array
            byte[] byteArray = s.getBytes();
            // Gives the data to the client who'll send this data to the server
            // The client will be notified that there is new data which can be send
            ClientManager.setData(byteArray);
        }
    }
    /**
     * The main function of the test class
     * @param args there are no arguments that can be given
     * @throws Exception when somethings goes wrong with making a new test class
     */
    public static void main(String[] args) throws Exception {
        // Builds a new test class
        new Test();
    }
}
