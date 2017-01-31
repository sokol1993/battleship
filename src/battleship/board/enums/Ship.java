package battleship.board.enums;

/**
 * Created by Kamil on 2015-11-05.
 */
public enum Ship {
    AIRCRAFT_CARRIER(4),
    DESTROYER(3),
    SUBMARINE(2),
    PATROL_BOAT(1);

    private int shipLength;

    Ship(int shipLength) {
        this.shipLength = shipLength;
    }

    public int getShipLength() {
        return shipLength;
    }
}
