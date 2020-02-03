package org.bloomdex.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ResourceHelper {
    private static Properties configProperties;

    public static void loadConfigProperties() {
        InputStream inputStream = null;

        try {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            inputStream = classloader.getResourceAsStream("config.properties");

            configProperties = new Properties();
            configProperties.load(inputStream); // load the properties file

            System.out.println("Config properties file is found");
        }
        catch (NullPointerException | IOException e) {
            System.out.println("Config properties file could not be found. Make sure there is a config file present");
        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Properties getConfigProperties() { return configProperties; }
}
