package battleship;

import battleship.board.enums.ShootStatus;
import battleship.board.design.ColorsAndFonts;
import battleship.board.enums.BoardOwner;
import battleship.board.PlayerBoard;
import battleship.board.design.StyledButtonUI;
import battleship.network.Client;
import battleship.network.GameEvent;
import battleship.network.Settings;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Kamil on 2015-11-05.
 */
public class Battleship extends JFrame {

    private static Battleship instance = null;

    public static Battleship getInstance() {
        return instance;
    }
    String playerName;
    public boolean opponentReady;
    boolean isFirstPlayer;
    boolean isGameFinish;
    private Client client;

    private PlayerBoard playerBoard;
    private PlayerBoard opponentBoard;

    private final int TOTAL_SHIPS = 10;
    private int playerHits;
    private int opponentHits;

    private boolean yourRound;

    private JPanel jPanel;
    private JScrollPane jScrollPane;
    private JButton newGameButton;
    private JButton startGameButton;
    private JButton arrangeShipsButton;
    private JLabel opponentShipsLabel;
    private JLabel opponentNick;
    private JTextField playerNick;
    private JTextArea remainingPlayerShipsLabel;
    private JTextArea remainingOpponentShipsLabel;
    private JLabel playerShipsLabel;
    private JTextArea statusGame;

    private void initialize() {
        yourRound = false;
        opponentHits = 0;
        playerHits = 0;
        setTitle("Battleship game");
        setSize(1360, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setResizable(false);
        setContentPane(createPanel());

        new Thread() {
            @Override
            public void run() {
                while (true) {
                    if (client != null && client.isAlive() && !isGameFinish) {
                        processMessages();
                    } else if (client != null && isGameFinish) {
                        client.stop();
                        client = null;
                    }
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }.start();
    }

    public boolean sendMessage(GameEvent ge) {
        if (client != null && client.isAlive()) {
            ge.setPlayerId(playerNick.getText());
            client.sendMessage(ge);
            return true;
        } else {
            return false;
        }
    }

    private void newGame(){
        newGameButton.setEnabled(true);
        startGameButton.setEnabled((false));
        arrangeShipsButton.setEnabled(false);
        playerNick.setEnabled(true);
        playerNick.setText("");
    }

    public boolean isYourRound() {
        return yourRound;
    }

    public void setYourRound(boolean b) {
        yourRound = b;
    }

    public Battleship() {
        super();
        initialize();
        instance = this;
    }

    private void processMessages() {
        GameEvent gameEvent;
        while (client != null && client.isAlive()
                && (gameEvent = client.receiveMessage(client.getClientGameRoomConnection())) != null) {
            switch (gameEvent.getType()) {

                case GameEvent.PLAYER_OUT:
                    if (playerNick.getText().compareTo(gameEvent.getMessage()) == 0) {
                        changeStatus("Opponents out. Start new game");
                        isGameFinish = true;
                        newGame();
                        GameEvent ge = new GameEvent(GameEvent.FINISH_GAME);
                        sendMessage(ge);
                    }
                    break;

                case GameEvent.LOGIN_FIRST_PLAYER:
                    if (playerNick.getText().compareTo(gameEvent.getMessage()) == 0) {
                        setYourRound(true);
                        isFirstPlayer = true;
                        changeStatus("Connected to server as first player\n" +
                                "Arrange your ship and start game");
                    }
                    break;

                case GameEvent.LOGIN_SECOND_PLAYER:
                    if (playerNick.getText().compareTo(gameEvent.getMessage()) == 0) {
                        changeStatus("Connected to server as second player\n" +
                                "Arrange your ship and start game");
                    }
                    break;

                case GameEvent.FAILED_LOGIN:
                    if (isFirstPlayer) {
                        isGameFinish = true;
                        newGame();
                        GameEvent ge = new GameEvent(GameEvent.FINISH_GAME);
                        sendMessage(ge);
                        createPlayerNickNameTextField().setText(playerName);
                        newGameButton.doClick();
                    } else {
                        changeStatus("Error connecting to server. \n Your login already exist \n Change it");
                        isGameFinish = true;
                        newGame();
                    }
                    break;

                case GameEvent.PLAYER_JOIN_TO_ROOM:
                    if (playerNick.getText().compareTo(gameEvent.getMessage()) == 0) {
                        changeStatus("Waiting for opponent");

                    } else if (startGameButton.isEnabled()) {
                        if (arrangeShipsButton.isEnabled()) {
                            changeStatus("Your opponent is ready. Arrange your ship and press \"Start game\" button");
                        }
                    }
                    break;

                case GameEvent.START_GAME:
                    opponentReady = true;
                    if (isYourRound()) {
                        changeStatus("Game is launched. You shoot!");
                    } else {
                        changeStatus("Game is launched. Your opponent's shoot");
                    }
                    break;

                case GameEvent.S_SHOT:
                    if (playerNick.getText().compareTo(gameEvent.getPlayerId()) != 0) {
                        String s = gameEvent.getMessage();
                        int idx1 = s.indexOf('|');
                        String a = s.substring(0, idx1);
                        String b = s.substring(idx1 + 1);

                        try {
                            int x = Integer.parseInt(a);
                            int y = Integer.parseInt(b);
                            ShootStatus w = playerBoard.board.checkShoot(x, y);
                            GameEvent geOut = new GameEvent(GameEvent.C_SHOT_RESULT);
                            geOut.setMessage(x + "|" + y + "|" + w.ordinal());
                            sendMessage(geOut);
                        } catch (NumberFormatException ex) {
                        }
                        repaint();
                    }
                    break;

                case GameEvent.S_SHOT_RESULT: {
                    String s = gameEvent.getMessage();
                    int idx1 = s.indexOf('|');
                    int idx2 = s.indexOf('|', idx1 + 1);
                    String a = s.substring(0, idx1);
                    String b = s.substring(idx1 + 1, idx2);
                    String c = s.substring(idx2 + 1);

                    try {
                        int x = Integer.parseInt(a);
                        int y = Integer.parseInt(b);
                        int n = Integer.parseInt(c);
                        ShootStatus w = ShootStatus.values()[n];

                        if (playerNick.getText().compareTo(gameEvent.getPlayerId()) != 0) {
                            opponentBoard.board.markShoot(x, y, w, BoardOwner.OPPONENT);
                        } else {
                            playerBoard.board.markShoot(x, y, w, BoardOwner.PLAYER);
                        }

                        if (w == ShootStatus.MISHIT) {
                            if (playerNick.getText().compareTo(gameEvent.getPlayerId()) != 0) {
                                changeStatus("You miss! Your opponent's shoot");
                            } else {
                                changeStatus("Your opponent miss. You shoot");
                                setYourRound(true);
                            }
                        } else {
                            if (w == ShootStatus.HIT) {
                                if (playerNick.getText().compareTo(gameEvent.getPlayerId()) != 0) {
                                    changeStatus("You hit your's opponent ship. ShootStatus again");
                                    setYourRound(true);
                                } else {
                                    changeStatus("Your opponent hit your ship. Opponent shoot again");
                                }
                            } else {
                                if (playerNick.getText().compareTo(gameEvent.getPlayerId()) != 0) {
                                    changeStatus("You sank ship! ShootStatus again");
                                    playerHits++;
                                    remainingOpponentShipsLabel.setText("REMAINING OPPONENT'S SHIPS:" + (TOTAL_SHIPS - playerHits));
                                    if (playerHits == TOTAL_SHIPS) {
                                        changeStatus("You win! Congratulations!");
                                        isGameFinish = true;
                                        newGame();
                                        GameEvent ge = new GameEvent(GameEvent.FINISH_GAME);
                                        sendMessage(ge);
                                    } else {
                                        setYourRound(true);
                                    }
                                } else {
                                    changeStatus("Your opponent sank your ship. Opponent shoot again");
                                    opponentHits++;
                                    remainingPlayerShipsLabel.setText("REMAINING YOUR'S SHIPS:" + (TOTAL_SHIPS - opponentHits));
                                    if (opponentHits == TOTAL_SHIPS) {
                                        changeStatus("You lost! Try again!");
                                        isGameFinish = true;
                                        newGame();
                                    }
                                }
                            }
                        } repaint();
                    } catch (NumberFormatException ex) {
                    }
                }
                break;
            }
        }
    }


    private JPanel createPanel() {
        if (jPanel == null) {
            jPanel = new JPanel();
            jPanel.setBackground(new Color(104, 112, 128));
            jPanel.setLayout(null);
            jPanel.add(createOponnentShipsLabel(), null);
            jPanel.add(createPlayerShipsLabel(), null);
            jPanel.add(createRemainingOpponentShipsLabel(), null);
            jPanel.add(createRemainingPlayerShipsLabel(), null);
            jPanel.add(createPlayerBoard(), null);
            jPanel.add(createOpponentBoard(), null);
            jPanel.add(createArrangeShipsButton(), null);
            jPanel.add(createNewGameButton(), null);
            jPanel.add(createStartGameButton(), null);
            jPanel.add(getJScrollPane(), null);
            jPanel.add(createPlayerNickNameTextField(), null);
            //      jPanel.add(createOpponentNickLabel(),null);
        }
        return jPanel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Battleship battleship = new Battleship();
                battleship.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                battleship.setVisible(true);
            }
        });
    }

    private PlayerBoard createPlayerBoard() {
        if (playerBoard == null) {
            playerBoard = new PlayerBoard(BoardOwner.PLAYER);
            playerBoard.setLocation(new java.awt.Point(13, 34));
        }
        return playerBoard;
    }

    private PlayerBoard createOpponentBoard() {
        if (opponentBoard == null) {
            opponentBoard = new PlayerBoard(BoardOwner.OPPONENT);
            opponentBoard.setLocation(new java.awt.Point(600, 34));
        }
        return opponentBoard;
    }

    private JButton createNewGameButton() {

            newGameButton = new JButton();
            newGameButton.setForeground(new Color(255, 255, 255));
            newGameButton.setBackground(new Color(76, 80, 96));
            newGameButton.setUI(new StyledButtonUI());
            newGameButton.setBounds(new Rectangle(1190, 34, 120, 30));
            newGameButton.setText("New game");
            newGameButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    playerName = "ala";
                    if (!playerName.equals("")) {
                        newGameButton.setEnabled(false);
                        startGameButton.setEnabled((true));
                        arrangeShipsButton.setEnabled(true);
                        playerNick.setEnabled(false);
                        isGameFinish = false;
                        opponentReady = false;
                        isFirstPlayer = false;

                        opponentHits = 0;
                        playerHits = 0;
                        remainingOpponentShipsLabel.setText("REMAINING OPPONENT'S SHIPS:" + (TOTAL_SHIPS - playerHits));
                        remainingPlayerShipsLabel.setText("REMAINING YOUR'S SHIPS:" + (TOTAL_SHIPS - opponentHits));
                        if (client == null) {
                            client = new Client(Settings.getInstance().getHost(), Settings.getInstance().getPort());
                            if (client.start()) {
                                GameEvent ge = new GameEvent(GameEvent.LOGIN);
                                sendMessage(ge);
                                playerBoard.board.randomArrangeShips();
                                opponentBoard.board.clearBoard();
                            }
                            else {
                                changeStatus("Server is not running!");
                                isGameFinish = true;
                                newGame();
                            }
                        }
                } else {
                    changeStatus("Login field is empty!");
                }
                    repaint();
            }});

        return newGameButton;
    }

    private JButton createStartGameButton() {

            startGameButton = new JButton();
            startGameButton.setEnabled(false);
            startGameButton.setForeground(new Color(255, 255, 255));
            startGameButton.setBackground(new Color(76, 80, 96));
            startGameButton.setUI(new StyledButtonUI());
            startGameButton.setBounds(new Rectangle(1190, 69, 120, 30));
            startGameButton.setText("Start game");
            startGameButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    arrangeShipsButton.setEnabled(false);
                    startGameButton.setEnabled(false);
                    GameEvent ge = new GameEvent(GameEvent.JOIN_TO_GAME);
                    sendMessage(ge);
                }
            });

        return startGameButton;
    }

    private JButton createArrangeShipsButton() {

            arrangeShipsButton = new JButton();
            arrangeShipsButton.setEnabled(false);
            arrangeShipsButton.setForeground(new Color(255, 255, 255));
            arrangeShipsButton.setBackground(new Color(76, 80, 96));
            arrangeShipsButton.setUI(new StyledButtonUI());
            arrangeShipsButton.setBounds(new Rectangle(1190, 104, 120, 30));
            arrangeShipsButton.setText("Arrange Ships");
            arrangeShipsButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    playerBoard.board.randomArrangeShips();
                    repaint();
                }
            });

        return arrangeShipsButton;
    }

    private JTextArea createStatusArea() {
        if (statusGame == null) {
            statusGame = new JTextArea();
            statusGame.setEnabled(true);
            statusGame.setEditable(false);
            statusGame.setLineWrap(true);
            statusGame.setWrapStyleWord(true);
            statusGame.setBackground(new Color(152, 152, 152));
            statusGame.setForeground(new Color(0, 0, 0));
            statusGame.setFont(new Font("Dialog", Font.BOLD, 12));
            statusGame.setText("Welcome!\nClick new game button.");
        }
        return statusGame;
    }

    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setLocation(new Point(1160, 386));
            jScrollPane.setEnabled(true);
            jScrollPane.setViewportView(createStatusArea());
            jScrollPane.setSize(new Dimension(185, 200));
        }
        return jScrollPane;
    }

    private JLabel createOpponentNickLabel() {
        if (opponentNick == null) {
            opponentNick = new JLabel();
        }
        return opponentNick;
    }

    private JTextField createPlayerNickNameTextField() {
        if (playerNick == null) {
            playerNick = new JTextField();
            playerNick.setBackground(Color.GRAY);
            playerNick.setBounds(new Rectangle(1190, 150, 120, 30));
        }
        return playerNick;
    }

    private JLabel createOponnentShipsLabel() {
        if (opponentShipsLabel == null) {
            opponentShipsLabel = new JLabel();
            opponentShipsLabel.setFont(ColorsAndFonts.font2);
            opponentShipsLabel.setForeground(new Color(0, 0, 0));
            opponentShipsLabel.setBounds(new Rectangle(13, 12, 150, 16));
            opponentShipsLabel.setHorizontalAlignment(SwingConstants.LEFT);
            opponentShipsLabel.setText("YOUR'S SHIPS");
        }
        return opponentShipsLabel;
    }

    private JLabel createPlayerShipsLabel() {
        if (playerShipsLabel == null) {
            playerShipsLabel = new JLabel();
            playerShipsLabel.setFont(ColorsAndFonts.font2);
            playerShipsLabel.setForeground(new Color(0, 0, 0));
            playerShipsLabel.setBounds(new Rectangle(600, 12, 200, 16));
            playerShipsLabel.setHorizontalAlignment(SwingConstants.LEFT);
            playerShipsLabel.setText("OPPONENT'S SHIPS");
        }
        return playerShipsLabel;
    }

    private JTextArea createRemainingPlayerShipsLabel() {
        if (remainingPlayerShipsLabel == null) {
            remainingPlayerShipsLabel = new JTextArea();
            remainingPlayerShipsLabel.setEnabled(true);
            remainingPlayerShipsLabel.setEditable(false);
            remainingPlayerShipsLabel.setLineWrap(true);
            remainingPlayerShipsLabel.setWrapStyleWord(true);
            remainingPlayerShipsLabel.setFont(ColorsAndFonts.font3);
            remainingPlayerShipsLabel.setBackground(new Color(104, 112, 128));
            remainingPlayerShipsLabel.setForeground(new Color(0, 0, 0));
            remainingPlayerShipsLabel.setBounds(new Rectangle(1160, 240, 200, 40));
            remainingPlayerShipsLabel.setText("REMAINING YOUR'S SHIPS:" + (TOTAL_SHIPS - opponentHits));
        }
        return remainingPlayerShipsLabel;
    }

    private JTextArea createRemainingOpponentShipsLabel() {
        if (remainingOpponentShipsLabel == null) {
            remainingOpponentShipsLabel = new JTextArea();
            remainingOpponentShipsLabel.setEnabled(true);
            remainingOpponentShipsLabel.setEditable(false);
            remainingOpponentShipsLabel.setLineWrap(true);
            remainingOpponentShipsLabel.setWrapStyleWord(true);
            remainingOpponentShipsLabel.setFont(ColorsAndFonts.font3);
            remainingOpponentShipsLabel.setBackground(new Color(104, 112, 128));
            remainingOpponentShipsLabel.setForeground(new Color(0, 0, 0));
            remainingOpponentShipsLabel.setBounds(new Rectangle(1160, 310, 200, 70));
            remainingOpponentShipsLabel.setText("REMAINING OPPONENT'S SHIPS:" + (TOTAL_SHIPS - playerHits));
        }
        return remainingOpponentShipsLabel;
    }

    private void changeStatus(String message) {
        statusGame.setText("");
        statusGame.append(message);
    }
}
