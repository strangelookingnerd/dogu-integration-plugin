package io.dogutech.jenkins;

import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;

public class DoguOption {
    public static String DOGU_TOKEN = "";
    public static String API_URL = "https://api.dogutech.io";

    public static String getWebSocketUrl(PrintStream logger) {
        URI apiURI;
        try {
            apiURI = new URI(API_URL);
        } catch (URISyntaxException e) {
            logger.println("Invalid API URL: " + API_URL);
            return "";
        }

        String socketUrl = "";
        String protocol = apiURI.getScheme();

        if ("http".equals(protocol)) {
            socketUrl = apiURI.getPort() == -1
                    ? "ws://" + apiURI.getHost()
                    : "ws://" + apiURI.getHost() + ":" + apiURI.getPort();
        } else if ("https".equals(protocol)) {
            socketUrl = apiURI.getPort() == -1
                    ? "wss://" + apiURI.getHost()
                    : "wss://" + apiURI.getHost() + ":" + apiURI.getPort();
        } else {
            logger.println("Unsupported protocol: " + protocol);
        }

        return socketUrl;
    }
}
