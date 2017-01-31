package battleship.board;


import battleship.board.enums.*;

import java.awt.*;
import java.util.Random;

/**
 * Created by Kamil on 2015-11-05.
 */
public class Board {

    private FieldStatus[][] board;
    private Random random;

    public Board() {
        board = new FieldStatus[10][10];
        random = new Random();
    }

    public void clearBoard() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                board[i][j] = FieldStatus.SEA;
            }
        }
    }

    public FieldStatus[][] getBoard() {
        return board;
    }

    public void randomArrangeShips() {
        clearBoard();

        for (int i = 0; i < 10; i++) {
            int shipLength;
            if (i < 1)
                shipLength = Ship.AIRCRAFT_CARRIER.getShipLength();
            else if (i < 3)
                shipLength = Ship.DESTROYER.getShipLength();
            else if (i < 6)
                shipLength = Ship.SUBMARINE.getShipLength();
            else
                shipLength = Ship.PATROL_BOAT.getShipLength();

            boolean isSuccessful;
            Point leftUp;
            Point rightDown;

            int x, y;
            Direction direction;

            do {
                direction = (Math.abs(random.nextInt()) % 2 == 0) ? Direction.HORIZONTAL
                        : Direction.VERTICAL;

                if (direction == Direction.HORIZONTAL) {
                    x = Math.abs(random.nextInt()) % (10 - shipLength);
                    y = Math.abs(random.nextInt()) % 10;
                    leftUp = new Point(Math.max(x - 1, 0), Math.max(y - 1, 0));
                    rightDown = new Point(Math.min(x + shipLength + 1, 9), Math
                            .min(y + 1, 9));
                } else {
                    x = Math.abs(random.nextInt()) % 10;
                    y = Math.abs(random.nextInt()) % (10 - shipLength);
                    leftUp = new Point(Math.max(x - 1, 0), Math.max(y - 1, 0));
                    rightDown = new Point(Math.min(x + 1, 9), Math.min(y
                            + shipLength + 1, 9));
                }

                isSuccessful = true;

                checkIsSea:
                for (int m = leftUp.x; m <= rightDown.x; m++)
                    for (int n = leftUp.y; n <= rightDown.y; n++)
                        if (board[m][n] != FieldStatus.SEA) {
                            isSuccessful = false;
                            break checkIsSea;
                        }
            } while (!isSuccessful);

            for (int d = 0; d < shipLength; d++)
                if (direction == Direction.HORIZONTAL) {
                    board[x + d][y] = FieldStatus.SHIP;
                } else {
                    board[x][y + d] = FieldStatus.SHIP;
                }
        }
    }

    public boolean checkEmptyField(int x, int y) {
        return board[x][y] == FieldStatus.SEA;
    }

    public boolean checkShipOrHitShipField(int x, int y) {
        return board[x][y] == FieldStatus.SHIP || board[x][y] == FieldStatus.HIT_SHIP;
    }

    public boolean checkHitShip(int x, int y) {
        return board[x][y] == FieldStatus.HIT_SHIP;
    }

    public boolean checkSunkShip(int x, int y) {
        int i;
        i = x;
        while (--i >= 0 && checkShipOrHitShipField(i, y))
            if (board[i][y] == FieldStatus.SHIP)
                return false;

        i = x;
        while (++i < 10 && checkShipOrHitShipField(i, y))
            if (board[i][y] == FieldStatus.SHIP)
                return false;

        i = y;
        while (--i >= 0 && checkShipOrHitShipField(x, i))
            if (board[x][i] == FieldStatus.SHIP)
                return false;

        i = y;
        while (++i < 10 && checkShipOrHitShipField(x, i))
            if (board[x][i] == FieldStatus.SHIP)
                return false;

        return true;
    }

    public ShootStatus checkShoot(int x, int y) {
        if (board[x][y] == FieldStatus.SHIP) {
            if (checkSunkShip(x, y)) {
                return ShootStatus.SUNK_HIT;
            } else {
                return ShootStatus.HIT;
            }
        } else {
            return ShootStatus.MISHIT;
        }
    }

    public void checkSunkShipShoot(int x, int y, ShootStatus shoot, BoardOwner boardOwner) {
        if (shoot == ShootStatus.SUNK_HIT) {
            markSunkShip(x, y, boardOwner);
        }
    }

    public void markShoot(int x, int y, ShootStatus shoot, BoardOwner boardOwner) {
        if (shoot == ShootStatus.MISHIT) {
            board[x][y] = FieldStatus.MISHIT;
        } else {
            board[x][y] = FieldStatus.HIT_SHIP;
            checkSunkShipShoot(x, y, shoot, boardOwner);
        }
    }

    public void markSunkShip(int x, int y, BoardOwner boardOwner) {
        int x1 = x;
        int x2 = x;
        int y1 = y;
        int y2 = y;

        while (x1 > 0 && checkHitShip(x1, y)) {
            x1--;
        }

        while (x2 < 9 && checkHitShip(x2, y)) {
            x2++;
        }

        while (y1 > 0 && checkHitShip(x, y1)) {
            y1--;
        }

        while (y2 < 9 && checkHitShip(x, y2)) {
            y2++;
        }

        for (int i = x1; i <= x2; i++)
            for (int j = y1; j <= y2; j++)
                if (checkHitShip(i, j))
                    board[i][j] = FieldStatus.SUNK_SHIP;
                else if (boardOwner == BoardOwner.OPPONENT && checkEmptyField(i, j)) {
                    board[i][j] = FieldStatus.MISHIT;
                }
    }
}
