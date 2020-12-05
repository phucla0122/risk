import java.io.Serializable;
import java.util.*;

/**
 * This class represents a continent in the game of Risk. It gives operations to find the conqueror of a
 * continent, add new territories, and get a String representation of the object.
 *
 * @author Nicolas Tuttle
 */
public class Continent implements Serializable {
    private final String name;
    private final List<Territory> territories;

    public final int BONUS_ARMIES;

    /**
     * Constructor for class Continent. Initializes the object with a name and the specified territory collection
     * @param name The continent's name
     * @param territories The collection of continents to be used to initialize the object
     * @param bonusArmies The number of bonus armies to be awarded if the continent is conquered
     */
    public Continent(String name, List<Territory> territories, int bonusArmies) {
        this.name = name;
        this.territories = territories;
        this.BONUS_ARMIES = bonusArmies;
    }

    /**
     * Get an Optional object containing the player that has conquered all territories on a continent
     * @return An Optional object containing the player object corresponding to the conqueror, or an empty Optional if
     * the continent is not conquered
     */
    public Optional<Player> getConqueror() {
        HashSet<Player> conquerors = new HashSet<>();
        for (Territory territory: territories) {
            conquerors.add(territory.getOwner());
        }

        // Return the conqueror if all territories have the same owner, or an empty optional object otherwise
        return (conquerors.size() == 1) ? Optional.of(conquerors.iterator().next()) : Optional.empty();
    }

    /**
     * Find the territory associated with a given ID
     * @param id The requested territory's ID
     * @return The territory associated with the ID
     */
    public Optional<Territory> getTerritoryById(int id) {
        try {
            return Optional.of(territories.get(id));
        } catch (IndexOutOfBoundsException e) {
            return Optional.empty();
        }
    }
    /**
     * Get the amount of territories in continent
     * @return an int that is the size of territories list
     */
    public int getTerritoriesSize() {
        return territories.size();
    }

    /**
     * Get the list of territories in continent
     * @return a list that is all the territories
     */
    public List<Territory> getTerritoryList() {
        return territories;
    }

    /**
     * Get name of continent
     * @return a String of continent's name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a String representation of this continent. The String representation consists of the
     * continent's name followed by each territory contained within it and its respective owner.
     * Different territories are separated by a new line ("\n")
     * @return A string representation of the continent
     */
    @Override
    public String toString() {
        ArrayList<String> output = new ArrayList<>(Collections.singletonList(name + ":"));
        for (Territory territory: territories) {
            output.add(territory.toString());
        }
        return String.join("\n", output);
    }
}
