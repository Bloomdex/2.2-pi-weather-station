package org.bloomdex.client;

import org.bloomdex.helpers.ResourceHelper;
import org.json.JSONObject;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Scanner;

public class ClientManager {
    private static final String[] protocols = new String[] {"TLSv1.2"};
    private static final String[] cipher_suites = new String[] {"TLS_RSA_WITH_AES_128_CBC_SHA"};
    private static ClientThread clientThread;

    private static String apiUrl;
    private static String username;
    private static String password;

    private static String hostname;
    private static int port;

    /**
     * Client constructor
     */
    public static void CreateClient() {
        apiUrl = ResourceHelper.getConfigProperties().getProperty("server.api_url");
        username = ResourceHelper.getConfigProperties().getProperty("server.username");
        password = ResourceHelper.getConfigProperties().getProperty("server.password");

        boolean connectedToServer = false;
        while (!connectedToServer) {
            try {
                System.out.println("Hold on while the server IP and port is looked for by the API...");
                setServerConnFromAPI();
            }
            catch (IOException e) {
                System.out.println("Getting the server IP and port using the API did not succeed. " +
                        "Type 'retry' to retry or fill in server information manually.");

                if(!handleInputHostname() || !handleInputPort())
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
        URL url = new URL (apiUrl + "type=request_connection");
        String encoding = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());

        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", "Basic " + encoding);
        connection.setRequestProperty("Accept", "application/json");

        InputStream content = connection.getInputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(content));
        String line = in.readLine();
        System.out.println("Got the following server information: " + line);

        JSONObject json = new JSONObject(line);
        port = json.getInt("port");
    }

    /**
     * Stop the server the application is connected to
     * @throws IOException throws an exception when it couldn't get or read the JSON
     */
    public static void stopServerFromAPI() throws IOException {
        URL url = new URL ( apiUrl + "type=stop_server");
        String encoding = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", "Basic " + encoding);
        connection.setRequestProperty("Accept", "application/json");

        InputStream content = connection.getInputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(content));
        String line = in.readLine();

        JSONObject json = new JSONObject(line);
        String response = json.getString("response");

        if (response.equals("SUCCESS"))
            System.out.println("Stopped the server successfully.");
        if (response.equals("FAILED"))
            System.out.println("Could not stop the server: " + json.getString("message"));
    }

    /**
     * Function that allows the user to manually enter the hostname of the server
     */
    private static boolean handleInputHostname() {
        boolean hostnameValidated = false;
        String hostname = "api.x.x.org";

        while (!hostnameValidated) {
            System.out.print("Hostname: ");
            Scanner input = new Scanner(System.in);
            hostname = input.nextLine().trim();

            if (hostname.equals("retry"))
                return false;
            else
                hostnameValidated = true;
        }

        ClientManager.hostname = hostname;
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
        SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket("api.vegaflor.bloomdex.org", ClientManager.port);
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
}
