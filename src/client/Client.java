package client;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.util.Scanner;

public class Client {
    private static final String[] protocols = new String[]{"TLSv1.3"};
    private static final String[] cipher_suites = new String[]{"TLS_AES_128_GCM_SHA256"};
    private static ClientThread clientThread;
    private static byte[] data;
    private int port;
    private String IP = "192.168.178.193";

    /**
     * Constructor without parameters
     *
     * @throws Exception trows an exception if:
     *      something goes wrong when the client socket is made
     *      something goes wrong when the client thread is made
     */
    Client() throws Exception {
        System.setProperty("javax.net.ssl.trustStore", "truststore");
        System.setProperty("javax.net.ssl.trustStorePassword", "passphrase");
        enterIPAddress();
        enterPort();
        createClientThread();
    }

    /**
     * Constructor with parameters
     *
     * @param IP the IP of the server to create a client socket
     * @param port the open port of the server to create a client socket
     * @throws Exception trows an exception if:
     *      something goes wrong when the client socket is made
     *      something goes wrong when the client thread is made
     */
     Client(String IP, int port) throws Exception {
        System.setProperty("javax.net.ssl.trustStore", "truststore");
        System.setProperty("javax.net.ssl.trustStorePassword", "passphrase");
        this.IP = IP;
        this.port = port;
        createClientThread();
    }

    /**
     * Function that allow the user manually enter the IP address of the server (for test prepuces)
     */
    public void enterIPAddress(){
        String IP;
        System.out.println("Enter the IP-Address off the server");
        Scanner input = new Scanner(System.in);
        IP = input.nextLine().trim();
        while(!validateIP(IP)) {
            System.out.println("Your ip was not valid, try again");
            IP = input.nextLine().trim();
        }
        this.IP = IP;
    }

    /**
     * Function that allow the user manually enter the open port of the server (for test prepuces)
     */
    public void enterPort(){
        String port;
        System.out.println("Enter the port off the server");
        Scanner input = new Scanner(System.in);
        port = input.nextLine().trim();
        while(!validatePort(port)){
            System.out.println("Your port was not valid, try again");
            port = input.nextLine().trim();
        }
        this.port = Integer.parseInt(port);
    }

    /**
     * validates if the manually entered port can be legal
     *
     * @param port the port witch the user manually entered
     * @return if the manually entered port can be legal
     */
    public static boolean validatePort(String port) {
        String PATTERN = "-?(0|[1-9]\\d*)";
        if(port.matches(PATTERN)){
            if(Integer.parseInt(port) < 65536 && Integer.parseInt(port) > 0){
                return true;
            }else {
                return false;
            }
        }else{
            return false;
        }
    }

    /**
     * Validates if the manually entered IP can be legal
     *
     * @param IP the IP witch the user manually entered
     * @return if the manually entered port can be legal
     */
    public static boolean validateIP(String IP) {
        String PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
        return IP.matches(PATTERN);
    }

    /**
     * Creates a client thead
     * @throws Exception throws an exception when something goes wrong when the SSL socket is made
     */
    private void createClientThread() throws Exception {
        SSLSocket socket = createSSLSocket();
        try {
            Client.clientThread = new ClientThread(socket);
            clientThread.start();
        } catch (Exception e) {
            System.out.printf("exception: %s%n", e.getMessage());
        }
    }

    /**
     * Creates the SSL socket for the client
     *
     * @return the SSL socket for the client
     * @throws IOException throws an exception when something goes wrong when the SSl socket is made
     */
    private SSLSocket createSSLSocket() throws IOException {
        SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(this.IP, this.port);
        socket.setEnabledProtocols(protocols);
        socket.setEnabledCipherSuites(cipher_suites);
        return socket;
    }

    /**
     * Sets the data for the client thread and notifies the client threat about it
     *
     * @param data the data witch will be given to the client threat
     */
    public void setData(byte[] data) {
        synchronized (Client.clientThread) {
            ClientThread.setData(data);
            Client.clientThread.notify();
        }
    }

    /**
     * The main function from the server
     *
     * @param args there are no arguments that can me given
     * @throws Exception throws an exception when something goes wrong with creating the client
     */
    public static void main(String[] args) throws Exception {
        new Client();
    }
}
