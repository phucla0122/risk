import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * This class create a JPanel asking the players for their names
 *
 * @author Robell Gabriel uthor Robell Gabriel
 */
public class PlayerNamePanel extends JPanel {
    private final JTextField name;
    private final JCheckBox AIPlayer;

    /**
     * Constructor for PlayerNamePanel asking player's name
     * @param i int looping for each player
     */
    public PlayerNamePanel(int i) {
        name = new JTextField("Name here",10);
        AIPlayer = new JCheckBox("AI Player");

        JLabel askName = new JLabel("Enter Player " + (i + 1) + " name: ");
        name.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (!AIPlayer.isSelected())
                    name.setText("");
            }

            @Override
            public void focusLost(FocusEvent e) {

            }
        });

        AIPlayer.addActionListener(e -> {
            if (AIPlayer.isSelected()) {
                name.setText("AI Player " + (i + 1));
                name.setEditable(false);
            } else {
                name.setText("Name here");
                name.setEditable(true);
            }
        });

        setLayout(new GridLayout(2,2));
        add(askName);
        add(name);
        if (i > 0) {
            add(AIPlayer);
        }
    }

    /**
     * getting each player's name
     * @return String player's name
     */
    public String getPlayerName() {
        return name.getText();
    }

    /**
     * checks if the player is an AI
     * @return true if AIPlayer checkbox is checked, false otherwise
     */
    public boolean isAI() {
        return AIPlayer.isSelected();
    }
}
