import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Random;
import javax.swing.*;

/**
 * The GameController for the GUI component of the game Risk, handles the user inputs which updates the GameModel and GameFrame.
 * This class prompts panels for when command buttons are clicked
 *
 * @author Nicolas Tuttle, Phuc La, Robell Gabriel, Jacob Schmidt
 */

public class GameController implements ActionListener {
    private final Game game;
    private final GameFrame gameView;
    public static final String[] options = {"OK"};

    /**
     * constructor for gameController class
     * @param game the game that is controlled by controller
     * @param gameView the view that represents the model that is controlled by this
     */
    public GameController(Game game,GameFrame gameView) {
        this.game = game;
        this.gameView = gameView;
    }

    /**
     * updates the model based on which button was pressed
     * @param e the button that was pressed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Player player = game.getCurrentPlayer();

        //if place button is pressed pull up a PlacePanel to get input from user and update model accordingly
        switch (e.getActionCommand()) {
            case "Load Game":{
                if (!game.loadGame()){
                    JOptionPane.showMessageDialog(gameView, "There is no saved game");
                }
                break;
            }
            case "Save Game":{
                try {
                    game.saveGame();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                break;
            }
            case "Place": {
                int result;
                PlacePanel plp = new PlacePanel(player, game.getContinents());
                do {
                    result = JOptionPane.showOptionDialog(gameView,
                            plp,
                            "Place",
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[0]);
                }
                while (result == JOptionPane.CLOSED_OPTION || plp.getArmiesRemaining() > 0);
                game.placePhase(plp.territoriesArmyIncreased());
                break;
            }
            //if move button is pressed pull up a movePanel to get input from user and update model accordingly
            case "Move": {
                MovePanel mp = new MovePanel(player, game);
                int result = JOptionPane.CLOSED_OPTION;

                while (
                    // If user has not selected valid territories or closed the window, reopen to prompt again
                        result == JOptionPane.CLOSED_OPTION
                                || (
                                result == JOptionPane.OK_OPTION
                                        && (mp.getMoveTo() == null
                                        || mp.getMoveFrom() == null)
                        )
                ) {
                    result = JOptionPane.showConfirmDialog(
                            gameView,
                            mp,
                            "Select a territory to move from!",
                            JOptionPane.OK_CANCEL_OPTION
                    );
                }
                if (result != JOptionPane.CANCEL_OPTION) {
                    game.movePhase(mp.getArmiesToMove(), mp.getMoveFrom(), mp.getMoveTo());
                }
                break;
            }
            //if attack button is pressed pull up a AttackPanel to get input from user and update model accordingly
            case "Attack": {
                AttackPanel ap = new AttackPanel(player, game);
                int result = JOptionPane.CLOSED_OPTION;

                while (
                    // If user has not selected valid territories or closed the window, reopen to prompt again
                        result == JOptionPane.CLOSED_OPTION
                                || (
                                result == JOptionPane.OK_OPTION
                                        && (ap.getAttackingTerritory() == null
                                        || ap.getDefendingTerritory() == null)
                        )
                ) {
                    result = JOptionPane.showConfirmDialog(
                            gameView,
                            ap,
                            "Select a territory to attack from!",
                            JOptionPane.OK_CANCEL_OPTION
                    );
                }
                if (result != JOptionPane.CANCEL_OPTION) {
                    Territory defending = ap.getDefendingTerritory();
                    Territory attacking = ap.getAttackingTerritory();

                    int armyNum = AIOrUserDefendArmies(defending);

                    boolean won = game.attack(attacking, ap.getArmyNum(), defending, armyNum);
                    if (won) {
                        result = JOptionPane.CLOSED_OPTION;
                        ArmySelectPanel transfer = new ArmySelectPanel(ap.getArmyNum(), attacking.getNumArmies() - 1);

                        while (result == JOptionPane.CLOSED_OPTION) {
                            result = JOptionPane.showOptionDialog(
                                    gameView,
                                    transfer,
                                    "Select a number of armies to transfer!",
                                    JOptionPane.OK_CANCEL_OPTION,
                                    JOptionPane.QUESTION_MESSAGE,
                                    null,
                                    options,
                                    options[0]
                            );
                        }
                        game.attackWon(attacking, defending, transfer.getArmyNum());
                    }
                }
                break;
            }
            case "Done":
                game.done();
                break;
        }
    }

    /**
     * Determines the amount of armies defending territory's owner wants to use,
     * depending if owner is the User or AI
     * @param defending Territory owned by defending player
     * @return int the amount of armies User/AI wants to defend territory with
     */
    public static int AIOrUserDefendArmies(Territory defending) {
        //opens up panels if player defending territory isn't an AI
        if (!defending.getOwner().isAI()){
            ArmySelectPanel dp = new ArmySelectPanel(1, defending.getNumArmies() > 1 ? 2 : 1);
            int result = JOptionPane.CLOSED_OPTION;

            while (result == JOptionPane.CLOSED_OPTION) {
                result = JOptionPane.showOptionDialog(
                        null,
                        dp,
                        defending.getOwner().getName()+", select a number of armies to defend!",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]
                );
            }
            return dp.getArmyNum();
        }else{
            //defend with random amount of armies for AI
            Random rnd = new Random();
            return rnd.nextInt(Math.min(2, defending.getNumArmies())) + 1;
        }
    }
}
