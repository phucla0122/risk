import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * The GameFrame for the GUI component of the game Risk, handles the graphical representation of the GameModel.
 * This class displays the main menu for the Game
 *
 * @author Nicolas Tuttle, Phuc La, Robell Gabriel, Jacob Schmidt
 */
public class GameFrame extends JFrame implements GameView{
    private final String[] options = {"OK"};
    private final JTextArea actionLog;
    private final DefaultMutableTreeNode mapList;
    private final JTree map;
    private final JLabel playerTurn;
    private final DefaultListModel<String> leaderBoardList;
    private final JButton attack,move,done,place;

    /**
     * constructor of the GameFrame that initializes the view of the GameModel
     *
     * @author Robell Gabriel and Phuc La
     */
    public GameFrame() {
        super("RISK!");

        Game game  = new Game();
        //TextArea to show the log
        actionLog = new JTextArea();
        actionLog.setEditable(false);
        actionLog.setRows(20);
        JScrollPane actionLogScroll = new JScrollPane(actionLog);
        actionLogScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        JLabel actionLogLabel = new JLabel("Action log");
        actionLogLabel.setSize(10, 10);
        game.addGameView(this);
        String[] gameOptions = {"New Game", "Load Game", "Custom Map"};
        int result;
        while (true){
            result = JOptionPane.showOptionDialog(this, "Choose Game State", "Game State",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, gameOptions, gameOptions[0]);
            if (result == JOptionPane.YES_OPTION){
                welcomePlayers(game);
                break;
            }else if (result == JOptionPane.NO_OPTION){
                if (!game.loadGame()){
                    JOptionPane.showMessageDialog(this, "There is no saved game");
                }else{
                    try {
                        loadActionLog();
                    } catch (FileNotFoundException fileNotFoundException) {
                        fileNotFoundException.printStackTrace();
                    }
                    break;
                }
            }else if (result == JOptionPane.CANCEL_OPTION){
                if (!game.loadCustomMap()) {
                    JOptionPane.showMessageDialog(this, "Custom map is invalid");
                }else{
                    welcomePlayers(game);
                    break;
                }
            }
        }

        Map<String, Continent> continents = game.getContinents();
        List<Player> activePlayers = game.getActivePlayers();
        Player currentPlayer = game.getCurrentPlayer();
        GameController gc = new GameController(game,this);

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        JMenuItem load = new JMenuItem("Load Game");
        JMenuItem save = new JMenuItem("Save Game");
        load.addActionListener(gc);
        load.setActionCommand(load.getText());
        save.addActionListener(gc);
        save.setActionCommand(save.getText());

        menu.add(load);
        menu.add(save);
        menuBar.add(menu);

        //JTree for map
        mapList = new DefaultMutableTreeNode();
        for (String id : continents.keySet()) {
            DefaultMutableTreeNode contList = new DefaultMutableTreeNode(continents.get(id).getName());
            for (Territory territory : continents.get(id).getTerritoryList()) {
                DefaultMutableTreeNode terr = new DefaultMutableTreeNode(territory.toString());
                contList.add(terr);
            }
            mapList.add(contList);
        }
        map = new JTree(mapList);

        map.setRootVisible(false);
        for (int i = 0; i < map.getRowCount(); i++) {
            map.expandRow(i);
        }
        JScrollPane mapScrollPane = new JScrollPane(map);
        JLabel mapLabel = new JLabel("Map");

        //JList of player leaderboard
        leaderBoardList = new DefaultListModel<>();
        for (Player player : activePlayers) {
            leaderBoardList.addElement(player.getName() + " owns " + player.getAllLandOwnedSize() + " territories");
        }
        JList<String> leaderBoard = new JList<>(leaderBoardList);
        JLabel leaderBoardLabel = new JLabel("Leaderboard");
        leaderBoardLabel.setVerticalAlignment(JLabel.BOTTOM);

        //JLabel for player turn and armies remaining to place
        playerTurn = new JLabel("It is " + currentPlayer.getName() + "'s turn");

        //JButtons for attack, move and done
        attack = new JButton("Attack");
        move = new JButton("Move");
        done = new JButton("Done");
        place = new JButton("Place");
        attack.addActionListener(gc);
        attack.setActionCommand(attack.getText());
        move.addActionListener(gc);
        move.setActionCommand(move.getText());
        done.addActionListener(gc);
        done.setActionCommand(done.getText());
        place.addActionListener(gc);
        place.setActionCommand(place.getText());


        //disable all buttons until place phase is done
        attack.setEnabled(false);
        move.setEnabled(false);
        done.setEnabled(false);
        //panels for main menu
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridLayout(1, 4));
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        JPanel p1 = new JPanel();
        p1.setLayout(new BorderLayout());

        //adding features to main menu
        setLayout(new BorderLayout());
        leftPanel.add(mapLabel);
        leftPanel.add(mapScrollPane);
        add(leftPanel, BorderLayout.WEST);
        p1.add(leaderBoard, BorderLayout.CENTER);
        rightPanel.add(leaderBoardLabel);
        rightPanel.add(p1);
        rightPanel.add(actionLogLabel);
        rightPanel.add(actionLogScroll);
        rightPanel.add(playerTurn);
        add(rightPanel, BorderLayout.CENTER);
        bottomPanel.add(attack);
        bottomPanel.add(move);
        bottomPanel.add(done);
        bottomPanel.add(place);
        add(bottomPanel, BorderLayout.SOUTH);
        this.setJMenuBar(menuBar);
        this.setSize(1200, 600);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        if (game.getCurrentPlayer().isAI()){
            game.AITurn();
        }
    }

    /**
     * Updates the view based on which method from the game model has called the update
     *
     * @param game The game state
     *
     * @author Phuc La and Robell Gabriel
     */
    @Override
    public void updateView(Game game) {
       Map <String,Continent>  continents = game.getContinents();
       Player currentPlayer = game.getCurrentPlayer();
       List<Player> activePlayers = game.getActivePlayers();
       Game.Status status = game.getStatus();

        resetMapAndLeaderBoard(continents, activePlayers);
        switch (status) {
            case PLACE:
                place.setEnabled(false);
                attack.setEnabled(true);
                move.setEnabled(true);
                done.setEnabled(true);
                break;

            case ATTACK:
                checkForWinner(activePlayers,currentPlayer);
                break;

            case DONE:
                playerTurn.setText("It is " + currentPlayer.getName() + "'s turn: ");
                place.setEnabled(true);
                attack.setEnabled(false);
                move.setEnabled(false);
                done.setEnabled(false);
                break;

            case DISABLE:
                disableButtons();
                break;

        }
    }

    public static void main(String[] args) {
        new GameFrame();
    }

    private void disableButtons() {
        place.setEnabled(false);
        attack.setEnabled(false);
        move.setEnabled(false);
        done.setEnabled(false);
    }

    /**
     * Updates the map as the game progresses (IE: attack/move territories losing/gaining armies)
     * Updates the leaderboard as the game progresses (IE: attack territory and conquer the land)
     *
     * @param continents a list of continents on the map
     *
     * @author Robell Gabriel and Phuc La
     */
    private void resetMapAndLeaderBoard(Map<String, Continent> continents, List<Player> activePlayers) {
        mapList.removeAllChildren();
        for (String id : continents.keySet()) {
            DefaultMutableTreeNode contList = new DefaultMutableTreeNode(continents.get(id).getName());
            for (Territory territory : continents.get(id).getTerritoryList()) {
                DefaultMutableTreeNode terr = new DefaultMutableTreeNode(territory.toString());
                contList.add(terr);
            }
            mapList.add(contList);

        }
        SwingUtilities.updateComponentTreeUI(map);
        for (int i = 0; i < map.getRowCount(); i++) {
            map.expandRow(i);
        }

        leaderBoardList.removeAllElements();
        List<Player> sortedPlayer = new ArrayList<>(activePlayers);
        sortedPlayer.sort(Comparator.comparing(Player::getAllLandOwnedSize).reversed());
        for (Player player : sortedPlayer) {
            leaderBoardList.addElement(player.getName() + " owns " + player.getAllLandOwnedSize() + " territories");
        }
    }

    /**
     * Checks if there's a winner
     *
     * @param activePlayers list of all players in game
     * @param currentPlayer Player object of current player's turn
     *
     * @author Robell Gabriel
     */
    private void checkForWinner(List<Player> activePlayers, Player currentPlayer){
        if (activePlayers.size() == 1) {
            JOptionPane.showOptionDialog(this, "Congratulations " + currentPlayer.getName() + ". You are the winner!!!", "Winner",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);
            disableButtons();
        }
    }

    /**
     * Opens panels that ask player count and their names
     *
     * @param game gamemodel which initializes the game here
     *
     * @author Robell Gabriel
     */
    private void welcomePlayers(Game game){
        Map<String, Boolean> playerNames = new HashMap<>();
        int result;
        //welcome panel of risk game
        WelcomePanel wp = new WelcomePanel();
        do {
            result = JOptionPane.showOptionDialog(this, wp, "Welcome",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);
        } while (result == JOptionPane.CLOSED_OPTION);
        int numPlayers = wp.getPlayerCount();

        //PlayerName panel of risk game
        boolean validInput = true;
        for (int i = 0; i < numPlayers; validInput = true) {
            PlayerNamePanel pnp = new PlayerNamePanel(i);
            do {
                result = JOptionPane.showOptionDialog(
                        this, pnp, "Player Names",
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                        null, options, options[0]);

                //checks if name is already used
                for (String nameCheck : playerNames.keySet()) {
                    if (nameCheck.equalsIgnoreCase(pnp.getPlayerName())) {
                        validInput = false;
                        JOptionPane.showMessageDialog(pnp,"Name is already used");
                        break;
                    }
                }

                //checks if name is too long if name was not already used
                if (validInput && pnp.getPlayerName().length() > 15) {
                    validInput = false;
                    JOptionPane.showMessageDialog(pnp,"Name is too long (15 characters max)");
                }
            } while (result == JOptionPane.CLOSED_OPTION || pnp.getPlayerName().isBlank() || pnp.getPlayerName().equals("Name here"));

            if (validInput) {
                i++;
                playerNames.put(pnp.getPlayerName(), pnp.isAI());
            }
        }
        //Initializer to get number of players, player's name, distribution of territory and armies
        game.initialize(playerNames);
    }

    /**
     * Prints a line to the action log
     *
     * @param str The line to be printed to the action log
     *
     * @author Nicolas Tuttle
     */
    @Override
    public void printLine(String str) {
        actionLog.append(str + "\n");
        actionLog.setCaretPosition(actionLog.getDocument().getLength());
    }

    /**
     * This method save the action log of the game into an sav file.
     * @throws IOException if the file cannot be saved
     */
    public void saveActionLog() throws IOException {
        FileWriter fw = new FileWriter("Actionlog.sav");
        for(String line : actionLog.getText().split("\n")){
            fw.write(line + "\n");
        }
        fw.close();
    }

    /**
     * This method load the action log of the loaded game file to the action log text area.
     * @throws FileNotFoundException if the file cannot be loaded
     */
    public void loadActionLog() throws FileNotFoundException {
        File file = new File("Actionlog.sav");
        Scanner sc = new Scanner(file);
        actionLog.selectAll();
        actionLog.replaceSelection("");
        while(sc.hasNextLine()){
            String act = sc.nextLine();
            printLine(act);
        }
        actionLog.append("\n");
    }
}
