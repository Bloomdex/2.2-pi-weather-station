package org.bloomdex.client;

import java.util.Scanner;

public class Test {
    private Client client;

    /**
     * The constructor of the Test class
     *
     * @throws Exception when something goes wrong with making the client
     */
    Test() throws Exception {
        // Sets the global variable client
        client = new Client();
        // Calls the method sendData witch will handel generate data for the client thread
        sendData();
    }

    /**
     * Sends the incoming data from the user to the client
     */
    public void sendData() {
        // A new user input scanner will be made
        Scanner in = new Scanner(System.in);
        while (true) {
            // Reads the user input when the user has pressed enter
            String s = in. nextLine();
            // Makes from the user input a byte[]
            byte[] byteArray = s.getBytes();
            // Gives the data to the client who will send this data to te server
            // The client will be notified that there is new data witch can be send
            client.setData(byteArray);
        }
    }

    /**
     * The main function of the test class
     *
     * @param args there are no arguments that can be given
     * @throws Exception when somethings goes wrong with making a new test class
     */
    public static void main(String[] args) throws Exception {
        // Builds a new test class
        new Test();
    }
}
