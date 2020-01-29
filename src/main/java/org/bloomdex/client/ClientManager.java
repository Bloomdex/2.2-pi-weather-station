package org.bloomdex.client;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.util.Scanner;

public class ClientManager {
    private static final String[] protocols = new String[] {"TLSv1.2"};
    private static final String[] cipher_suites = new String[] {"TLS_RSA_WITH_AES_128_CBC_SHA"};
    private static ClientThread clientThread;
    private static int port;
    private static String IP;

    /**
     * Client constructor without parameters
     * @throws Exception trows an exception if:
     *      something goes wrong when the client socket is made
     *      something goes wrong when the client thread is made
     */
    public static void CreateClient() throws Exception {
        System.setProperty("javax.net.ssl.trustStore", "D:/Projects/_School/2.2-pi-weather-station/src/main/java/org/bloomdex/client/truststore");
        System.setProperty("javax.net.ssl.trustStorePassword", "passphrase");
        enterIPAddress();
        enterPort();
        createClientThread();
    }

    /**
     * Client constructor with parameters
     * @param IP the IP of the server
     * @param port the open port of the server
     * @throws Exception trows an exception if:
     *      something goes wrong when the client socket is made
     *      something goes wrong when the client thread is made
     */
    public static void CreateClient(String IP, int port) throws Exception {
        System.setProperty("javax.net.ssl.trustStore", "truststore");
        System.setProperty("javax.net.ssl.trustStorePassword", "passphrase");
        ClientManager.IP = IP;
        ClientManager.port = port;
        createClientThread();
    }

    /**
     * Function that allows the user to manually enter the IP address of the server
     */
    public static void enterIPAddress(){
        System.out.println("Enter the IP-Address off the server");
        Scanner input = new Scanner(System.in);
        String IP = input.nextLine().trim();

        while (!validateIP(IP)) {
            System.out.println("Your ip was not valid, try again");
            IP = input.nextLine().trim();
        }
        ClientManager.IP = IP;
    }

    /**
     * Function that allows the user to manually enter the open port of the server
     */
    public static void enterPort(){
        System.out.println("Enter the server port");
        Scanner input = new Scanner(System.in);
        String port = input.nextLine().trim();

        while (!validatePort(port)){
            System.out.println("Your port was not valid, try again");
            port = input.nextLine().trim();
        }
        ClientManager.port = Integer.parseInt(port);
    }

    /**
     * Validates whether the manually entered port is legal
     * @param port the port which the user manually entered
     * @return a boolean that tells whether the given port is legal
     */
    public static boolean validatePort(String port) {
        return port.matches("-?(0|[1-9]\\d*)")
                && Integer.parseInt(port) > 0 
                && Integer.parseInt(port) < 65536;
    }

    /**
     * Validates whether the manually entered IP is legal
     * @param IP the IP witch the user manually entered
     * @return a boolean that tells whether the given IP is legal
     */
    public static boolean validateIP(String IP) {
        return IP.matches(
                "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$");
    }

    /**
     * Creates a client thread
     * @throws Exception throws an exception when something goes wrong when the SSL socket is made
     */
    private static void createClientThread() throws Exception {
        SSLSocket socket = createSSLSocket();

        try {
            clientThread = new ClientThread(socket);
            clientThread.start();
        }
        catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Creates the SSL socket for the client
     * @return the SSL socket for the client
     * @throws IOException throws an exception when something goes wrong when the SSl socket is made
     */
    private static SSLSocket createSSLSocket() throws IOException {
        SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(ClientManager.IP, ClientManager.port);
        socket.setEnabledProtocols(protocols);
        socket.setEnabledCipherSuites(cipher_suites);

        return socket;
    }

    /**
     * Sets the data for the client thread to send and notifies the client threat about it
     * @param data the data which will be given to the client thread
     */
    public static void setData(byte[] data) {
        if (clientThread != null) {
            synchronized (ClientManager.clientThread) {
                clientThread.setData(data);
                clientThread.notify();
            }
        }
        else
            System.out.println("ClientManager: There is no instantiated client present. " +
                    "Remove the NC argument to instantiate a client at startup.");
    }

    public static void main(String[] args) throws Exception {
        ClientManager.CreateClient();
    }
}
