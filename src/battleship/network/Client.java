package battleship.network;

import java.net.Socket;

/**
 * Created by Kamil on 2015-11-11.
 */
public class Client {
    private String host;
    private int port;
    private Socket socket;
    private ClientGameRoomConnection clientGameRoomConnection;

    public Client(String host, int port){
        this.host = host;
        this.port = port;
    }

    public boolean start() {
        try {
            socket = new Socket(host, port);
        } catch (Exception ex) {
            return false;
        }

        clientGameRoomConnection = new ClientGameRoomConnection(socket);
        clientGameRoomConnection.start();

        return true;
    }

    public void stop() {
        if (clientGameRoomConnection != null)
            clientGameRoomConnection.close();
    }

    public void sendMessage(GameEvent ge) {
        clientGameRoomConnection.sendMessage(ge.toSend());
    }

    public static GameEvent receiveMessage(ClientGameRoomConnection clientGameRoomConnection) {
        if (clientGameRoomConnection.messages.isEmpty()) {
            return null;
        } else {
            GameEvent gameEvent = new GameEvent(clientGameRoomConnection.messages.getFirst());
            clientGameRoomConnection.messages.removeFirst();
            return gameEvent;
        }
    }

    public ClientGameRoomConnection getClientGameRoomConnection() {
        return clientGameRoomConnection;
    }
    public boolean isAlive() {
        return (clientGameRoomConnection != null && clientGameRoomConnection.isAlive());
    }
}
