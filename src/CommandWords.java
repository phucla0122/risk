import java.util.HashMap;
import java.util.Objects;

/**
 * This class is part of the game "Risk"
 *
 * This class holds an enumeration of all command words known to the game.
 * It is used to recognise commands as they are typed in.
 *
 * @author  Robell Gabriel
 */

public class CommandWords
{
    // A mapping between a command word and the CommandWord
    // associated with it.
    private HashMap<String, CommandWord> validCommands;

    /**
     * Constructor - initialise the command words.
     */
    public CommandWords()
    {
        validCommands = new HashMap<>();
        for(CommandWord command : CommandWord.values()) {
            if(command != CommandWord.UNKNOWN) {
                validCommands.put(command.toString(), command);
            }
        }
    }

    /**
     * Find the CommandWord associated with a command word.
     * @param commandWord The word to look up.
     * @return The CommandWord correspondng to commandWord, or UNKNOWN
     *         if it is not a valid command word.
     */
    public CommandWord getCommandWord(String commandWord)
    {
        CommandWord command = validCommands.get(commandWord);
        return Objects.requireNonNullElse(command, CommandWord.UNKNOWN);
    }

    /**
     * Print all valid commands to System.out.
     */
    public void showAll()
    {
        for(String command : validCommands.keySet()) {
            System.out.print(command + "  ");
        }
        System.out.println();
    }
}
