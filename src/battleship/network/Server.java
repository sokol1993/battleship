package battleship.network;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * Created by Kamil on 2015-11-11.
 */
public class Server extends JFrame{

    private int port;
    private ServerSocket serverSocket;
    private boolean isRunning;
    private java.util.List<GameRoom> gameRoomList = new ArrayList<>();
    private int clientCount;
    private int listCount;

    public JTextArea serverTextArea;

    public static void main(String[] args) {
        Server server = new Server(Settings.getInstance().getPort());
        try {
            server.start();
            server.serverRunning();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            server.stop();
        }
    }

    public void serverRunning(){
        while (isRunning()) {
            Socket clientSocket;
            GameRoom gameRoom;
            ClientGameRoomConnection clientGameRoomConnection;
            try {
                clientSocket = getServerSocket().accept();
                clientCount++;
                clientGameRoomConnection = new ClientGameRoomConnection(clientSocket);
                clientGameRoomConnection.start();
                if (clientCount % 2 == 1) {
                    serverTextArea.append("First player at " + (listCount + 1) + "room\n");
                    gameRoom = new GameRoom(this);
                    gameRoom.start();
                    gameRoom.getConnectionsGame().add(clientGameRoomConnection);
                    gameRoomList.add(gameRoom);
                }
                if (clientCount % 2 == 0) {
                    gameRoomList.get(listCount).getConnectionsGame().add(clientGameRoomConnection);
                    listCount++;
                    serverTextArea.append("Second player at " + listCount + "room\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public boolean isRunning(){
        return isRunning;
    }

    public Server(int port) {
        this.port = port;
        isRunning = false;
        serverTextArea = new JTextArea();

        serverTextArea.setEditable(false);
        serverTextArea.setForeground(Color.black);
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BorderLayout());

        setLayout(new BorderLayout());
        add(jPanel, BorderLayout.NORTH);
        add(new JScrollPane(serverTextArea), BorderLayout.CENTER);

        setTitle("Battleship server");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        clientCount = 0;
        listCount = 0;
    }

    public void stop() {
        isRunning = false;
        try {
            serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        isRunning = true;
        serverTextArea.append("Server started\n");
    }

}