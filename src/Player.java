import java.io.Serializable;
import java.util.*;

/**
 * This class is part of the game "RISK"
 *
 * This class hold the command to create an instance of player as well as
 * return the player name and the list of territories that the player owned
 *
 * @author Phuc La
 */
public class Player implements Serializable {
    private final String name;
    private final boolean AI;
    private final List<Territory> ownedlands;

    /**
     * Constructor for the player that will play the game
     * @param name is the name that the player will use in the game
     * @param AI true if the player is AI, false otherwise
     */
    public Player(String name, boolean AI) {
        this.name = name;
        this.AI = AI;
        ownedlands = new LinkedList<>();
    }

    /**
     * Get the player name
     * @return a string that is the name of the player
     */
    public String getName() {
        return name;
    }

    /**
     * Add a new territory that the player conquered into the list of lands that the
     * player owned
     * @param ter is the new territory that will be added to the list
     */
    public void addTerritory(Territory ter) {
        ownedlands.add(ter);
    }

    /**
     * Remove a territory from the player's owned lands
     * @param ter The territory to remove
     */
    public void removeTerritory(Territory ter) {
        ownedlands.remove(ter);
    }

    /**
     * Return the list of lands owned by the player so far in the game
     * @return the list that contained all the land that the player owned at the moment
     */
    public List<Territory> getAllLandOwned() {
        return ownedlands;
    }

    /**
     * returns the amount of all land owned by player
     * @return int amount of ownedlands
     */
    public int getAllLandOwnedSize() { return ownedlands.size();}

    /**
     * Checks if all territories owned by player have 1 army
     * @return true if all territories have 1 army, false otherwise
     */
    public boolean allLandOwnedHas1Army(){
        int terrWith1Army = 0;
        for (Territory terr : ownedlands){
            if (terr.getNumArmies()==1){
                terrWith1Army++;
            }
        }
        return terrWith1Army == ownedlands.size();
    }

    /**
     * Checks if there's friendly territories adjacent to player's owned territories
     * @param game current state of game
     * @return true if at least 1 of owned territories have friendly adjacent
     *              and owned territory has more than 1 army, false otherwise
     */
    public boolean allLandOwnedAdjacentIsFriendly(Game game){
        int friendlyTerr = 0;
        for (Territory terr : ownedlands){
            for (String id : terr.getAdjacentList()){
                Territory tempTerritory = game.findTerritory(id).get();
                if (tempTerritory.getOwner().equals(this) && terr.getNumArmies()>1){
                    friendlyTerr++;
                    break;
                }
            }
        }
        return friendlyTerr > 0;
    }

    /**
     * Checks if there's enemy territories adjacent to player's owned territories
     * @param game current state of game
     * @return true if at least 1 of owned territories have enemy adjacent
     *              and owned territory has more than 1 army, false otherwise
     */
    public boolean allLandOwnedAdjacentIsEnemy(Game game){
        int enemyTerr = 0;
        for (Territory terr : ownedlands){
            for (String id : terr.getAdjacentList()){
                Territory tempTerritory = game.findTerritory(id).get();
                if (!tempTerritory.getOwner().equals(this) && terr.getNumArmies()>1){
                    enemyTerr++;
                    break;
                }
            }
        }
        return enemyTerr > 0;
    }

    /**
     * Get a list containing all owned territories with an enemy adjacent
     * @param gameState the game that is used to search for territories by id string from listTerritories
     * @return List containing all matching territories
     */
    public List<Territory> getLandWithAdjacentEnemy(Game gameState) {
        return getLandWithAdjacentAllyOrEnemy(gameState, false);
    }

    /**
     * Get a list containing all owned territories with a friendly adjacent
     * @param gameState the game that is used to search for territories by id string from listTerritories
     * @return List containing all matching territories
     */
    public List<Territory> getLandWithAdjacentAlly(Game gameState) {
        return getLandWithAdjacentAllyOrEnemy(gameState, true);
    }

    private List<Territory> getLandWithAdjacentAllyOrEnemy(Game gameState, boolean withAlly) {
        List<Territory> output = new ArrayList<>();
        for (Territory terr : ownedlands) {
            List<String> adjacent = terr.getAdjacentList();
            boolean foundValidAdjacent = false;
            for (int i = 0; !foundValidAdjacent && i < adjacent.size(); i++) {
                Optional<Territory> territory = gameState.findTerritory(adjacent.get(i));
                if (territory.isPresent() && (territory.get().getOwner() == this) == withAlly) {
                    output.add(terr);
                    foundValidAdjacent = true;
                }
            }
        }
        output.sort(Comparator.comparing(Territory::getId));
        return output;
    }

    /**
     * checks if player is an AI
     * @return true if player name represents AI, false otherwise
     */
    public boolean isAI(){
        return AI;
    }

}
