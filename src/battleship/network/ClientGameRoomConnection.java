package battleship.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;

/**
 * Created by Kamil on 2015-11-11.
 */
public class ClientGameRoomConnection extends Thread {

    private String nick = "";
    private boolean isJoined = false;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    public LinkedList<String> messages;

    public ClientGameRoomConnection(Socket socket) {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        messages = new LinkedList<>();
        this.socket = socket;
    }

    public void sendMessage(String s) {
        out.println(s);
    }

    public void run() {
        String string;
        try {
            while ((string = in.readLine()) != null) {
                messages.add(string);
            }
            out.close();
            in.close();
        } catch (IOException e) {

        }
        try {
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getNick() {
        return nick;
    }

    public void setJoined(boolean b) {
        isJoined = b;
    }

    public boolean isJoined() {
        return isJoined;
    }
}
