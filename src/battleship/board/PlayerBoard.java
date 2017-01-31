package battleship.board;

import battleship.Battleship;
import battleship.board.enums.BoardOwner;
import battleship.network.GameEvent;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static battleship.board.design.ColorsAndFonts.*;
import static battleship.board.enums.FieldStatus.*;

/**
 * Created by Kamil on 2015-11-05.
 */
public class PlayerBoard extends JPanel {

    private BoardOwner boardOwner;
    public Board board;
    private BufferedImage imageHit;
    private BufferedImage imageSea;
    private BufferedImage imageSea2;
    private BufferedImage imageFire;
    private BufferedImage imageShip;

    public PlayerBoard(final BoardOwner boardOwner) {
        this.boardOwner = boardOwner;
        board = new Board();

        Dimension rozmiar = new Dimension(552, 552);
        setSize(rozmiar);
        setMinimumSize(rozmiar);
        setMaximumSize(rozmiar);
        setPreferredSize(rozmiar);

        File imageFile = new File("src\\battleship\\images\\target.jpg");
        File imageFile1 = new File("src\\battleship\\images\\ship.jpg");
        File imageFile2 = new File("src\\battleship\\images\\sea2.jpg");
        File imageFile3 = new File("src\\battleship\\images\\fire.jpg");
        File imageFile5 = new File("src\\battleship\\images\\sea.jpg");
        try {
            imageHit = ImageIO.read(imageFile);
            imageSea = ImageIO.read(imageFile2);
            imageFire = ImageIO.read(imageFile3);
            imageShip = ImageIO.read(imageFile1);
            imageSea2 = ImageIO.read(imageFile5);
        } catch (IOException e) {
            System.err.println("Error reading image");
            e.printStackTrace();
        }

        Dimension dimension = new Dimension(imageHit.getWidth(), imageHit.getHeight());
        setPreferredSize(dimension);

        board.clearBoard();
        repaint();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1
                        && boardOwner == BoardOwner.OPPONENT
                        && Battleship.getInstance().isYourRound() && Battleship.getInstance().opponentReady) {

                    Point p = e.getPoint();

                    if (p.x % 50 != 0 && p.x % 50 != 1 && p.y % 50 != 0
                            && p.y % 50 != 1) {
                        int x = ((p.x - 2) / 50) - 1;
                        int y = ((p.y - 2) / 50) - 1;
                        if (x >= 0 && x < 10 && y >= 0 && y < 10)
                            if (board.getBoard()[x][y] == SEA) {
                                Battleship.getInstance().setYourRound(false);
                                board.getBoard()[x][y] = HIT;
                                GameEvent ge = new GameEvent(GameEvent.C_SHOT);
                                ge.setMessage(x + "|" + y);
                                Battleship.getInstance().sendMessage(ge);
                                repaint();
                            }
                    }
                }
            }
        });
    }


    @Override
    public void paint(Graphics g) {
        Image image = createImage(getSize().width, getSize().height);

        Graphics2D graphics = (Graphics2D) image.getGraphics();

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.setColor(Color.black);
        graphics.fillRect(0, 0, 552, 552);

        paintColumns(graphics);
        paintRow(graphics);

        for (int i = 0; i < board.getBoard().length; i++) {
            for (int j = 0; j < board.getBoard()[i].length; j++) {
                switch (board.getBoard()[i][j]) {
                    case SEA:
                        graphics.drawImage(imageSea, null, 52 + 50 * i, 52 + 50 * j);
                        break;
                    case SHIP:
                        graphics.drawImage(imageShip, null, 52 + 50 * i, 52 + 50 * j);
                        break;
                    case HIT:
                        graphics.drawImage(imageHit, null, 52 + 50 * i, 52 + 50 * j);
                        break;
                    case HIT_SHIP:
                        graphics.drawImage(imageFire, null, 52 + 50 * i, 52 + 50 * j);
                        break;
                    case MISHIT:
                        graphics.drawImage(imageSea2, null, 52 + 50 * i, 52 + 50 * j);
                        break;
                    case SUNK_SHIP:
                        graphics.drawImage(imageShip, null, 52 + 50 * i, 52 + 50 * j);
                        graphics.setStroke(new BasicStroke(2.0f));
                        graphics.setColor(Color.RED);
                        graphics.drawLine(52 + 50 * i, 52 + 50 * j, 100 + 50 * i, 100 + 50 * j);
                        graphics.drawLine(100 + 50 * i, 52 + 50 * j, 52 + 50 * i, 100 + 50 * j);
                        break;
                }
            }
        }
        g.drawImage(image, 0, 0, this);
    }

    @Override
    public void update(Graphics g) {
        paint(g);
    }

    public void paintColumns(Graphics2D graphics2D) {
        for (int i = 1; i < 11; i++) {
            graphics2D.setColor(greyColor);
            graphics2D.fillRect(2 + 50 * i, 2, 48, 46);
        }
        graphics2D.setColor(whiteColor);
        graphics2D.setFont(font);
        graphics2D.drawString("1", 65, 41);
        graphics2D.drawString("2", 115, 41);
        graphics2D.drawString("3", 165, 41);
        graphics2D.drawString("4", 215, 41);
        graphics2D.drawString("5", 265, 41);
        graphics2D.drawString("6", 315, 41);
        graphics2D.drawString("7", 365, 41);
        graphics2D.drawString("8", 415, 41);
        graphics2D.drawString("9", 465, 41);
        graphics2D.drawString("10", 505, 41);
    }

    public void paintRow(Graphics2D graphics2D) {
        for (int j = 1; j < 11; j++) {
            graphics2D.setColor(greyColor);
            graphics2D.fillRect(2, 2 + 50 * j, 46, 48);
            graphics2D.setColor(whiteColor);
        }
        graphics2D.setColor(whiteColor);
        graphics2D.setFont(font);
        graphics2D.drawString("A", 11, 92);
        graphics2D.drawString("B", 11, 142);
        graphics2D.drawString("C", 11, 192);
        graphics2D.drawString("D", 11, 242);
        graphics2D.drawString("E", 11, 292);
        graphics2D.drawString("F", 11, 342);
        graphics2D.drawString("G", 11, 392);
        graphics2D.drawString("H", 11, 442);
        graphics2D.drawString("I", 20, 492);
        graphics2D.drawString("J", 11, 542);
    }
}

