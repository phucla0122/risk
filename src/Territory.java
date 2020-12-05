import java.io.Serializable;
import java.util.*;

/** the class Territory represents a territory in the risk game identifiable by a Name and ID that can be read.
 *  The Territory can have armies represented by integers be removed,added or read. Lastly it has a list containing all
 *  adjacent territories.
 *
 * @author Jacob Schmidt
 */
public class Territory implements Serializable {
    private Player owner; // owner of the country
    private int numArmies = 0; //amount of armies contained in the country
    private final String name; //name to identify country by
    private final String id; //id to identify country by
    private final List<String> listOfAdjacents; //List of countries that belong to other countries adjacent to this one

    /**
     * constructor for territory.
     * @param name longer string used to identify territories
     * @param id string to used identify territories
     * @param listOfAdjacents The list of adjacent territory IDs
     */
    public Territory(String name, String id, List<String> listOfAdjacents) {
        this.name = name;
        this.id = id;
        this.listOfAdjacents = listOfAdjacents;
    }

    /**
     * function that increments the amount of armies in the territory by numAdd as long as the number does
     * not surpass the integer canAdd
     * @param numAdd integer to add to numArmies
     */
    public void addArmy(int numAdd) {
        numArmies += numAdd;
    }

    /**
     * function that decrements numArmies in the territory by numRemove as long as the resulting int is
     * larger than 0
     * @param numRemove integer that represent the amount to decrement numArmies by
     * @return a boolean representing whether the method was successful
     */
    public boolean removeArmy(int numRemove) {
        if (numArmies - numRemove <= 0){
            return false;
        }
        numArmies -= numRemove;
        return true;
    }

    /**
     * Set the number of armies in the territory to a different value
     * @param newArmies The new amount of armies
     */
    public void setNumArmies(int newArmies) {
        this.numArmies = newArmies;
    }

    /**
     * Get this territory's ID
     * @return The territory's id
     */
    public String getId() {
        return id;
    }

    /**
     * a method to read the integer numArmies
     * @return an integer representing numArmies
     */
    public int getNumArmies() {
        return numArmies;
    }

    /**
     * a method used to set owner to an existing player
     * @param owner the player that will be set to owner
     */
    public void setPlayer (Player owner) {
        this.owner = owner;
    }

    /**
     * a method to read who the owner is
     * @return a Player, owner
     */
    public Player getOwner() {
        return owner;
    }

    /**
     * a method to read the name of the territory
     * @return a string representation of the name
     */
    public String getName() {
        return name;
    }

    /**
     * a method to read the list of all adjacent territories
     * @return a string representing all adjacent territory's IDs
     */
    public List<String> getAdjacentList() {
        return listOfAdjacents;
    }

    /**
     * get a list of friendly adjacent territories
     * @param gameState the game that is used to search for territories by id string from listTerritories
     * @return a list containing friendly adjacent territories
     */
    public List<Territory> getAdjacentFriendly(Game gameState) {
        return getAdjacentFriendlyOrEnemy(gameState, true);
    }

    /**
     * get a list of enemy adjacent territories
     * @param gameState the game that is used to search for territories by id string from listTerritories
     * @return a list containing enemy adjacent territories
     */
    public List<Territory> getAdjacentEnemy(Game gameState) {
        return getAdjacentFriendlyOrEnemy(gameState, false);
    }

    private List<Territory> getAdjacentFriendlyOrEnemy(Game gameState, boolean getFriendly) {
        ArrayList<Territory> output = new ArrayList<>();
        for (String toCheck : listOfAdjacents){
            gameState.findTerritory(toCheck).ifPresent(territory -> {
                if ((territory.getOwner() == owner) == getFriendly) {
                    output.add(territory);
                }
            });
        }
        output.sort(Comparator.comparing(Territory::getId));
        return output;
    }

    @Override
    public String toString() {
        return name + " [" + id + "] | Owner: " + owner.getName() + " | Armies: " + numArmies + " | Adjacent Territories: " +listOfAdjacents;
    }
}