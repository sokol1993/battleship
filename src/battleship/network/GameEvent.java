package battleship.network;

/**
 * Created by Kamil on 2015-11-11.
 */
public class GameEvent {
    /////////KLIENT EVENTS//////////
    public static final int LOGIN = 1001;
    public static final int C_SHOT = 1002;
    public static final int C_SHOT_RESULT = 1003;
    public static final int JOIN_TO_GAME = 1004;
    public static final int FINISH_GAME = 1005;

    /////////SERWER EVENTS//////////
    public static final int LOGIN_FIRST_PLAYER = 2001;
    public static final int LOGIN_SECOND_PLAYER = 2002;
    public static final int FAILED_LOGIN = 2003;
    public static final int S_SHOT = 2004;
    public static final int S_SHOT_RESULT = 2010;
    public static final int START_GAME = 2005;
    public static final int PLAYER_JOIN_TO_ROOM = 2009;
    public static final int PLAYER_OUT = 2011;

    private int eventType;
    private String playerId = "";
    private String message;

    public GameEvent(int type) {
        setType(type);
    }

    public GameEvent(int type, String message) {
        this(type);
        this.message = message;
    }

    public GameEvent(String receivedMessage) {
        String x = receivedMessage;
        int idx1 = x.indexOf('|');
        int idx2 = x.indexOf('|', idx1 + 1);
        String type = x.substring(0, idx1);
        String player = x.substring(idx1 + 1, idx2);
        String message = x.substring(idx2 + 1);
        try {
            setType(Integer.parseInt(type));
        } catch (NumberFormatException ex) {
            setType(-1);
        }
        setPlayerId(player);
        setMessage(message);
    }

    public String toSend() {
        String toSend = eventType + "|" + playerId + "|" + getMessage();
        return toSend;
    }

    public void setType(int type) {
        eventType = type;
    }

    public int getType() {
        return eventType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String id) {
        playerId = id;
    }
}
