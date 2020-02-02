package org.bloomdex.client;

import org.json.JSONObject;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Scanner;

public class ClientManager {
    private static final String[] protocols = new String[] {"TLSv1.2"};
    private static final String[] cipher_suites = new String[] {"TLS_RSA_WITH_AES_128_CBC_SHA"};
    private static ClientThread clientThread;
    private static int port;
    private static String IP;
    private static String serverStatus;

    /**
     * Client constructor
     */
    public static void CreateClient() {
        System.setProperty("javax.net.ssl.trustStore", "D:/Projects/_School/2.2-pi-weather-station/src/main/java/org/bloomdex/client/truststore");
        System.setProperty("javax.net.ssl.trustStorePassword", "passphrase");

        boolean connectedToServer = false;
        while (!connectedToServer) {
            try {
                System.out.println("Hold on while the server IP and port is looked for by the API...");
                setServerConnFromAPI();
            }
            catch (IOException e) {
                System.out.println("Getting the server IP and port using the API did not succeed. " +
                        "Type 'retry' to retry or fill it in manually.");

                if(!handleInputIP() || !handleInputPort())
                    continue;
            }

            try {
                createClientThread();
                connectedToServer = true;
            }
            catch (Exception e) {
                System.out.println("Could not set up a connection to the server.");
            }
        }

        System.out.println("Successfully set up a connection to the server.");
    }

    /**
     * Set the IP and port of the server using the provided vegaflor bloomdex API or by manual input
     * @throws IOException throws an exception when it couldn't get or read the JSON
     */
    private static void setServerConnFromAPI() throws IOException {
        URL url = new URL ("https://api.vegaflor.bloomdex.org/api/v1/connection?type=request_connection");
        String encoding = Base64.getEncoder().encodeToString(("bloomdex-robot:W2ezqVjo5MvLesMmkfLNdzHKWC6YEjBF").getBytes());

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty  ("Authorization", "Basic " + encoding);
        InputStream content = (InputStream)connection.getInputStream();
        BufferedReader in = new BufferedReader (new InputStreamReader(content));
        String line = in.readLine();
        JSONObject json = new JSONObject(line);
        System.out.println(line);
        port = json.getInt("port");
        IP = json.getString("ipAddress");
        serverStatus = json.getString("response");
    }

    /**
     * Function that allows the user to manually enter the IP address of the server
     */
    private static boolean handleInputIP() {
        boolean ipValidated = false;
        String IP = "0.0.0.0";

        while (!ipValidated) {
            System.out.print("IP: ");
            Scanner input = new Scanner(System.in);
            IP = input.nextLine().trim();

            if (IP.equals("retry"))
                return false;
            else
                ipValidated = validateIP(IP);

            if (!ipValidated)
                System.out.println("The ip was not valid, try again.");
        }

        ClientManager.IP = IP;
        return true;
    }

    /**
     * Function that allows the user to manually enter the open port of the server
     */
    private static boolean handleInputPort() {
        boolean portValidated = false;
        String port = "00000";

        while (!portValidated){
            System.out.print("Port: ");
            Scanner input = new Scanner(System.in);
            port = input.nextLine().trim();

            portValidated = validatePort(port);

            if (!portValidated)
                System.out.println("The port was not valid, try again.");
        }

        ClientManager.port = Integer.parseInt(port);
        return true;
    }

    /**
     * Validates whether the manually entered port is legal
     * @param port the port which the user manually entered
     * @return a boolean that tells whether the given port is legal
     */
    private static boolean validatePort(String port) {
        return port.matches("-?(0|[1-9]\\d*)")
                && Integer.parseInt(port) > 0 
                && Integer.parseInt(port) < 65536;
    }

    /**
     * Validates whether the manually entered IP is legal
     * @param IP the IP witch the user manually entered
     * @return a boolean that tells whether the given IP is legal
     */
    private static boolean validateIP(String IP) {
        return IP.matches(
                "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$");
    }

    /**
     * Creates a client thread
     * @throws Exception throws an exception when something goes wrong when the SSL socket is made
     */
    private static void createClientThread() throws Exception {
        SSLSocket socket = createSSLSocket();

        clientThread = new ClientThread(socket);
        clientThread.start();
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
