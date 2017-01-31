package battleship.network;

/**
 * Created by Kamil on 2015-11-11.
 */
public class Settings {

    private String host = "localhost";
    private int port = 9000;

    private static Settings instance = null;

    public static Settings getInstance() {
        if (instance == null) instance = new Settings();
        return instance;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }
}
