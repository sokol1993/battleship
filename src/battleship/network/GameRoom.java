package battleship.network;

import java.util.LinkedList;

/**
 * Created by Kamil on 2015-11-11.
 */
public class GameRoom extends Thread {

    private static final int MAX_PLAYERS = 2;
    private Server server;
    private boolean finishGame;
    private boolean playerOut;
    private LinkedList<ClientGameRoomConnection> connectionsGame;
    private int clientsCount;
    private int joinedClientsCount;

    public GameRoom(Server server) {
        this.server = server;
        connectionsGame = new LinkedList<>();
        finishGame = false;
        playerOut = false;
        clientsCount = 0;
        joinedClientsCount = 0;
    }

    public void run() {
        while (server.isRunning() & !finishGame) {
            for (int i = connectionsGame.size() - 1; i >= 0; --i) {
                ClientGameRoomConnection clientGameRoomConnection = connectionsGame.get(i);

                if (!clientGameRoomConnection.isAlive()) {

                        clientsCount--;
                        playerOut = true;
                        clientGameRoomConnection.close();
                        connectionsGame.remove(clientGameRoomConnection);

                } else {
                    GameEvent gameEventIn;
                    while ((gameEventIn = Client.receiveMessage(clientGameRoomConnection)) != null) {
                        if(playerOut) {
                            GameEvent gameEventOut;
                            gameEventOut = new GameEvent(GameEvent.PLAYER_OUT, gameEventIn.getPlayerId());
                            sendMessage(clientGameRoomConnection, gameEventOut);
                        }
                        switch (gameEventIn.getType()) {

                            case GameEvent.LOGIN:
                                if (gameEventIn.getPlayerId() != "") {
                                    if (isPlayerUnique(gameEventIn.getPlayerId())) {
                                        clientGameRoomConnection.setNick(gameEventIn.getPlayerId());
                                        GameEvent gameEventOut;
                                        if (clientsCount == 0) {
                                            gameEventOut = new GameEvent(GameEvent.LOGIN_FIRST_PLAYER, gameEventIn.getPlayerId());
                                            sendBroadcastMessage(gameEventOut);
                                            clientsCount++;
                                        } else {
                                            gameEventOut = new GameEvent(GameEvent.LOGIN_SECOND_PLAYER, gameEventIn.getPlayerId());
                                            sendBroadcastMessage(gameEventOut);
                                            clientsCount++;
                                        }
                                    } else {
                                        GameEvent gameEventOut;
                                        gameEventOut = new GameEvent(GameEvent.FAILED_LOGIN, gameEventIn.getPlayerId());
                                        sendMessage(clientGameRoomConnection, gameEventOut);
                                    }
                                }
                                break;

                            case GameEvent.JOIN_TO_GAME:
                                {
                                    clientGameRoomConnection.setJoined(true);
                                    GameEvent geOut;
                                    geOut = new GameEvent(GameEvent.PLAYER_JOIN_TO_ROOM, gameEventIn.getPlayerId());
                                    sendBroadcastMessage(geOut);
                                    joinedClientsCount++;
                                    if (joinedClientsCount == MAX_PLAYERS) {
                                        startGame();
                                        server.serverTextArea.append("Game started\n");
                                    }
                                }
                                break;

                            case GameEvent.C_SHOT:
                                if (gameEventIn.getPlayerId() != "") {
                                    GameEvent geOut;
                                    geOut = new GameEvent(GameEvent.S_SHOT, gameEventIn.getMessage());
                                    geOut.setPlayerId(gameEventIn.getPlayerId());
                                    sendBroadcastMessage(geOut);
                                }
                                break;

                            case GameEvent.C_SHOT_RESULT:
                                if (gameEventIn.getPlayerId() != "") {
                                    GameEvent geOut;
                                    geOut = new GameEvent(GameEvent.S_SHOT_RESULT, gameEventIn.getMessage());
                                    geOut.setPlayerId(gameEventIn.getPlayerId());
                                    sendBroadcastMessage(geOut);
                                }
                                break;

                            case GameEvent.FINISH_GAME:
                                finishGame = true;
                                server.serverTextArea.append("Game finished. Room deleted\n");
                                break;
                        }
                    }
                }
            }

            try {
                Thread.sleep(50);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(ClientGameRoomConnection clientGameRoomConnection, GameEvent gameEvent) {
        clientGameRoomConnection.sendMessage(gameEvent.toSend());
    }

    private void startGame() {
        GameEvent geOut;
        geOut = new GameEvent(GameEvent.START_GAME);

        for (ClientGameRoomConnection clientGameRoomConnection : connectionsGame) {
            if (clientGameRoomConnection.isAlive() && clientGameRoomConnection.isJoined()) {
                sendMessage(clientGameRoomConnection, geOut);
            }
        }
    }

    public void sendBroadcastMessage(GameEvent gameEvent) {
        for(ClientGameRoomConnection clientGameRoomConnection : connectionsGame){
            if (clientGameRoomConnection.isAlive()) {
                sendMessage(clientGameRoomConnection, gameEvent);
            }
        }
    }

    public boolean isPlayerUnique(String nick) {
        for(ClientGameRoomConnection clientGameRoomConnection : connectionsGame) {
            if (clientGameRoomConnection.getNick().compareTo(nick) == 0) return false;
        }
        return true;
    }

    public LinkedList<ClientGameRoomConnection> getConnectionsGame() {
        return connectionsGame;
    }
}

