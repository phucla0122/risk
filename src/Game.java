import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import javax.swing.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The main Game class for the game Risk and the GameModel for the GUI component. This manages all operations including game initialization,
 * player turns, and command handling. The game ends when there is one player left standing.
 *
 * @author Nicolas Tuttle, Phuc La, Robell Gabriel, Jacob Schmidt
 */

public class Game implements Serializable {
    private List<Player> activePlayers;
    private Map<String, Continent> continents;
    private Player currentPlayer;
    private final ArrayList<GameView> gameViews;

    public enum Status {ATTACK, PLACE, DISABLE, DONE, PASS}
    private Status status = Status.PLACE;

    /**
     * The threshold at which the AI will switch from attack to move phase
     * AI_THRESHOLD must be between 0 and AI_MAX - 1, inclusive
     *
     * Probabilities:
     * Attack = AI_THRESHOLD / AI_MAX
     * Done = 1 / AI_MAX
     * Move = (AI_MAX - AI_THRESHOLD - 1) / AI_MAX
     */
    public static final int AI_THRESHOLD = 16;

    /**
     * The number of outcomes the AI RNG can select from
     */
    public static final int AI_MAX = 20;

    /**
     * Constructor for the Game class
     */
    public Game() {
        activePlayers = new LinkedList<>();
        continents = new HashMap<>();
        gameViews = new ArrayList<>();
    }

    /**
     * places a certain amount of armies into the designated territory.
     * Updates the action log in the view with what happened during place phase
     *
     * @param mt is a hashmap of type string for key (Territory ID) and integer for value, places the value into
     *           the corresponding key
     * @author Robell Gabriel and Phuc La
     */
    public void placePhase(Map<String, Integer> mt) {
        status = Status.PLACE;
        for (String tid : mt.keySet()) {
            findTerritory(tid).ifPresent(territory -> {
                territory.addArmy(mt.get(tid));
                printLine(territory.getOwner().getName() + " has placed " + mt.get(tid) + " armies into " + territory.getName() +
                        " which now has " + territory.getNumArmies() + " armies\n");
            });

        }
        updateView();
    }

    /**
     * completes logic for moving armies from one territory to another and updates view accordingly
     *
     * @param i is an integer that represents how many armies to move from toRemove to toPlace
     * @param toRemove is the territory that has armies removed from it
     * @param toPlace is the territory that has armies placed into it
     *
     * @author Jacob Schmidt
     */
    public void movePhase(int i, Territory toRemove, Territory toPlace) {

        if (toRemove.removeArmy(i)) {
            toPlace.addArmy(i);
            printLine(toRemove.getOwner().getName()+" has moved " + i + " armies from " + toRemove.getName() + " to " + toPlace.getName());
            printLine("Move phase is over\n");
        } else {
            printLine("some how u messed up tough luck");
        }
            done();

    }

    /**
     * completes logic for one territory attacking another and updates the view accordingly
     *
     * @param attacking a territory that is attacking the territory defending
     * @param attackArmy and int that represents the amount of armies to attack with
     * @param defending a territory that is defending the territory attacking
     * @param defendArmy an int that represents the amount of armies to defend with
     * @return a boolean representing whether the attacker takes over the territory
     *
     * @author Nicolas Tuttle
     */
    public boolean attack(Territory attacking, int attackArmy, Territory defending, int defendArmy) {
        status = Status.ATTACK;
        LinkedList<Integer> attackRolls = rollDice(attackArmy);
        LinkedList<Integer> defendRolls = rollDice(defendArmy);

        // Sort to find highest pairs
        attackRolls.sort(Collections.reverseOrder());
        defendRolls.sort(Collections.reverseOrder());

        printLine(attacking.getOwner().getName() + " is attacking " + defending.getName() + " with " + attacking.getName() + "!");
        printLine("Attacker rolled " + attackRolls.size() + " dice: " + Arrays.toString(attackRolls.toArray()));
        printLine("Defender rolled " + defendRolls.size() + " dice: " + Arrays.toString(defendRolls.toArray()) + "\n");

        int attackLosses = 0;
        int defendLosses = 0;
        while (!attackRolls.isEmpty() && !defendRolls.isEmpty()) {
            int attack = attackRolls.removeFirst();
            int defend = defendRolls.removeFirst();

            // Compare pairs with ties going to defender
            if ((attack > defend)) {
                defendLosses++;
            } else {
                attackLosses++;
            }
        }

        if (defending.removeArmy(defendLosses)) {
            // Defending still has units left
            attacking.removeArmy(attackLosses);

            printLine("The attacking territory lost " + attackLosses + " unit(s)! It has " + attacking.getNumArmies() + " unit(s) left.");
            printLine("The defending territory lost " + defendLosses + " unit(s)! It has " + defending.getNumArmies() + " unit(s) left.\n");
            updateView();
            return false;
        } else {
            return true;
        }
    }

    /**
     * If an attack conquers a territory this method completes the logic to move armies into territory
     *
     * @param attacking the territory who's owner conquers the territory defending
     * @param defending the territory that is conquered by the owner of attacking
     * @param armyNum the number of armies to move from attacking to defending
     * @return true if game has winner, false otherwise
     *
     * @author Nicolas Tuttle
     */
    public boolean attackWon(Territory attacking, Territory defending, int armyNum) {
        status = Status.ATTACK;

        Player defendingPlayer = defending.getOwner();

        defending.setPlayer(currentPlayer);
        currentPlayer.addTerritory(defending);
        defendingPlayer.removeTerritory(defending);
        attacking.removeArmy(armyNum);
        defending.setNumArmies(armyNum);
        printLine("The defending territory lost all units and was conquered by " + attacking.getOwner().getName() + "!");
        printLine(armyNum + " armies were transferred to conquered land\n");
        //CHEAT CODE -> winner tester
            /*List<Territory> lst = new ArrayList<Territory>(defendingPlayer.getAllLandOwned());
            for(Territory ter : lst){
                defendingPlayer.removeTerritory(ter);
            }*/

        if (defendingPlayer.getAllLandOwnedSize() == 0) {
            // Defender has no territories left, they are eliminated
            printLine(defendingPlayer.getName() + " has lost all their territories! They have been eliminated.\n");
            activePlayers.remove(defendingPlayer);
            if (activePlayers.size()==1){
                updateView();
                return true;
            }
        }
        updateView();
        return false;
    }

    /**
     * Initiates AI turn if necessary
     */
    public void done() {
        status = Status.DONE;
        isFirstTurn = false;
        printLine(currentPlayer.getName() + " has ended their turn\n");
        currentPlayer = activePlayers.get((activePlayers.indexOf(currentPlayer) + 1) % activePlayers.size());
        updateView();
        while (currentPlayer.isAI() && activePlayers.size() > 1) {
            AITurn();
        }
    }


    /**
     * Roll dice the specified number of times and return the list of results
     *
     * @param numDice The number of times to roll the dice
     * @return The result of the rolls
     * @author Nicolas Tuttle
     */
    private LinkedList<Integer> rollDice(int numDice) {
        LinkedList<Integer> rolls = new LinkedList<>();
        Random random = new Random();

        for (int i = 0; i < numDice; i++) {
            rolls.add(random.nextInt(6) + 1);
        }

        return rolls;
    }

    /**
     * Gets a territory by its full ID (continent + ID)
     *
     * @param id The ID corresponding to the desired territory
     * @return The Optional object containing the territory specified by the ID if it exists, an empty Optional otherwise
     * @author Nicolas Tuttle
     */
    public Optional<Territory> findTerritory(String id) {
        // Return empty if the ID is not 2 letters followed by digits (invalid ID)
        if (!(id.length() > 2 && continents.containsKey(id.substring(0, 2)) && id.substring(2).matches("^\\d+$")))
            return Optional.empty();

        Continent continent = continents.get(id.substring(0, 2));
        return (continent == null) ? Optional.empty() : continent.getTerritoryById(Integer.parseInt(id.substring(2)) - 1);
    }

    /**
     * Initializes the game
     * Calls upon user with Welcome and PlayerName panel for number of players and their names.
     * Creates all territories, continents then evenly adds territories to
     * players at random and randomly adds armies to territories.
     *
     * @param playerName The list of players to initialize
     * @author Robell Gabriel
     */
    public void initialize(Map<String, Boolean> playerName) {
        Stack<String> territoryID = new Stack<>(); //stack of temporary territory IDs
        Stack<Integer> armyList2 = new Stack<>(); //stack of temporary armyList

        try {
            importCustomMap("src/DefaultMap.xml");
        } catch (Exception e) {
            // This should never happen if the DefaultMap file is correct!
            JOptionPane.showMessageDialog(null, "Critical error. Shutting down." + e.getMessage());
            System.exit(-1);
        }

        //creates list of temporary Ids for each territory which is shuffled to player's get random territory
        for (String id : continents.keySet()) {
            for (int j = 1; j <= continents.get(id).getTerritoriesSize(); j++) {
                territoryID.push("" + id + j);
            }
        }
        Collections.shuffle(territoryID);

        //insert then shuffle list of Territory total for when there are 4 or 5 players
        int equalTerrs = territoryID.size() / playerName.size();
        ArrayList<Integer> terrCount = new ArrayList<>(playerName.size());
        for (int i = 0; i < playerName.size(); i++) {
            terrCount.add(equalTerrs);
        }

        for (int leftover = territoryID.size() - playerName.size() * equalTerrs; leftover > 0; leftover--) {
            terrCount.set(leftover, terrCount.get(leftover) + 1);
        }
        Collections.shuffle(terrCount);

        // Allocate armies per player dependant on number of players
        List<Integer> allocatedArmies = List.of(50, 35, 30, 25, 20);

        //Player name panel asks names then adds player's name and their territories with random amount of armies each
        int i = 0;
        for (String name : playerName.keySet()) {
            activePlayers.add(new Player(name, playerName.get(name)));
            initializePlayer(allocatedArmies.get(playerName.size() - 2), terrCount.get(i), i, territoryID, armyList2);

            //add random amount of armies to each territory
            for (Territory territory : activePlayers.get(i).getAllLandOwned()) {
                territory.addArmy(armyList2.pop());
            }
            i++;
        }
        currentPlayer = activePlayers.get(0);
    }

    /**
     * Initializes the player
     * Randomly adds territories to player as well as a random amount of armies to territories.
     * All of which are evenly distributed between players.
     *
     * @param numArmies   int total number of armies for player
     * @param numOfTerr   int total number of territories for player
     * @param i           int used to loop between players
     * @param armyList2   Stack of randomly distributed armies
     * @param territoryID Stack of valid territory ids
     *
     * @author Robell Gabriel
     */
    private void initializePlayer(int numArmies, int numOfTerr, int i, Stack<String> territoryID, Stack<Integer> armyList2) {
        int[] armyList = new int[numOfTerr];
        numArmies -= numOfTerr;
        for (int z = 0; z < numOfTerr; z++) {
            //add player to random territory
            findTerritory(territoryID.peek()).orElseThrow().setPlayer(activePlayers.get(i));
            //add random territory to player
            activePlayers.get(i).addTerritory(findTerritory(territoryID.pop()).orElseThrow());
            //generates a list of random numbers that all add up to
            //total number of armies player can own and total amount of random
            //numbers are equal to number of territories they can own
            if (z < (numOfTerr - 1)) {
                armyList[z] = (int) (Math.random() * numArmies);
            }
        }
        armyList[numOfTerr - 1] = numArmies;
        Arrays.sort(armyList);
        for (int z = numOfTerr - 1; z > 0; z--) {
            armyList[z] -= armyList[z - 1];
        }
        for (int z = 0; z < numOfTerr; z++) {
            armyList[z]++;
        }
        //add armyList to stack so it can be added to territories
        for (int army : armyList) {
            armyList2.push(army);
        }
    }

    /**
     * Handles all commands (Place, Attack, Move, Done) for AI,
     * being chosen at random based off threshold in max range
     */
    public void AITurn() {
        status = Status.DISABLE;
        Random rnd = new Random();

        // Place phase
        int armiesRemaining = Math.max(3, currentPlayer.getAllLandOwnedSize() / 3);
        for (Continent continent : continents.values()) {
            Optional<Player> conqueror = continent.getConqueror();
            if (conqueror.isPresent() && conqueror.get().equals(currentPlayer)) armiesRemaining += continent.BONUS_ARMIES;
        }

        List<Territory> landWithAdjacentEnemy = currentPlayer.getLandWithAdjacentEnemy(this);
        HashMap<String, Integer> toAdd = new HashMap<>();
        while (armiesRemaining > 0) {
            Territory ter = landWithAdjacentEnemy.get(rnd.nextInt(landWithAdjacentEnemy.size()));
            int toPlace = rnd.nextInt(armiesRemaining) + 1;
            if (toAdd.containsKey(ter.getId())) {
                toPlace += toAdd.get(ter.getId());
            }
            toAdd.put(ter.getId(), toPlace);
            armiesRemaining -= toPlace;
        }
        placePhase(toAdd);

        //runs out all commands in a loop at random
        int rng = AI_THRESHOLD - 1;

        while (!currentPlayer.allLandOwnedHas1Army()) {
            if (rng >= AI_THRESHOLD && currentPlayer.allLandOwnedAdjacentIsFriendly(this)) {
                // Move phase
                List<Territory> playerTerrs = currentPlayer.getLandWithAdjacentAlly(this)
                        .stream()
                        .filter(territory -> territory.getNumArmies() > 1)
                        .collect(Collectors.toList());


                rng = rnd.nextInt(playerTerrs.size());
                Territory moveFrom;
                do {
                    moveFrom = playerTerrs.get(rng);
                } while (moveFrom.getNumArmies() < 2);

                List<Territory> adjacentTerrs = moveFrom.getAdjacentFriendly(this);
                rng = rnd.nextInt(adjacentTerrs.size());
                Territory moveTo = adjacentTerrs.get(rng);

                int armiesToMove = rnd.nextInt(moveFrom.getNumArmies() - 1) + 1;
                movePhase(armiesToMove, moveFrom, moveTo);
                return;
            } else if (currentPlayer.allLandOwnedAdjacentIsEnemy(this)) {
                // Attack
                List<Territory> playerTerrs = currentPlayer.getLandWithAdjacentEnemy(this)
                        .stream()
                        .filter(territory -> territory.getNumArmies() > 1)
                        .collect(Collectors.toList());
                Territory attacking = playerTerrs.get(rnd.nextInt(playerTerrs.size()));

                List<Territory> adjacentTerrs = attacking.getAdjacentEnemy(this);
                Territory defending = adjacentTerrs.get(rnd.nextInt(adjacentTerrs.size()));

                int max = Math.min(attacking.getNumArmies() - 1, 3);
                int attackArmyNum = rnd.nextInt(max) + 1;
                int defendArmyNum = GameController.AIOrUserDefendArmies(defending);

                if (attack(attacking, attackArmyNum, defending, defendArmyNum)) {
                    // Transfer random amount of armies for AI
                    int transferNum = rnd.nextInt(attacking.getNumArmies() - attackArmyNum) + attackArmyNum;
                    if (attackWon(attacking, defending, transferNum)){
                        // AI has won the game
                        return;
                    }
                }
            }
            rng = rnd.nextInt(AI_MAX);
        }
        done();
    }

    /**
     * Saves current game state into a file
     *
     * @throws IOException if file cannot save
     */
    public void saveGame() throws IOException {
        FileOutputStream gameSaveFile = new FileOutputStream("RISK.sav");
        ObjectOutputStream risk = new ObjectOutputStream(gameSaveFile);
        risk.writeObject(this);
        risk.close();
        gameSaveFile.close();
    }

    /**
     * Loads a saved game state from a file into current game/new game
     *
     * @throws IOException if file cannot load
     * @throws ClassNotFoundException if file's object cannot be added
     */
    public void loadGame() throws IOException, ClassNotFoundException {
        FileInputStream gameFile = new FileInputStream("RISK.sav");
        ObjectInputStream gameObjin = new ObjectInputStream(gameFile);
        Game risk = (Game) gameObjin.readObject();
        currentPlayer = risk.getCurrentPlayer();
        activePlayers = risk.getActivePlayers();
        continents = risk.getContinents();
    }

    /**
     * Update all the views with the given code
     * Also calls AI turn if player is an AI
     *
     */
    private void updateView(){
        for (GameView gv : gameViews){
            if (currentPlayer.isAI() & status != Status.DONE & status != Status.ATTACK) {
                if (status != Status.DISABLE) {
                    status = Status.PASS;
                }
            }
            gv.updateView(this);
        }
    }

    /**
     * Print a line to the action log
     * @param message The message to print to the action log
     */
    private void printLine(String message) {
        for (GameView gv : gameViews){
            gv.printLine(message);
        }
    }

    /**
     * Get the current active player
     * @return The current player
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * get the status of the game
     * @return enum Status representing the status of the game
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Get the map of all continents
     * @return The map of continents
     */
    public Map<String, Continent> getContinents() {
        return continents;
    }

    /**
     * Get a list of all players remaining in the game
     * @return The list of active players
     */
    public List<Player> getActivePlayers() {
        return activePlayers;
    }

    /**
     * Add a new view to the Game
     * @param view The view to add
     */
    public void addGameView(GameView view){
        gameViews.add(view);
    }

    /**
     * Import a custom map from an XML file
     * @param filename The XML file with the custom map
     * @throws ParserConfigurationException If the parser is incorrectly configured
     * @throws SAXException If the custom map is invalid
     * @throws IOException If the file cannot be opened
     */
    public void importCustomMap(String filename) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser p = spf.newSAXParser();

        CustomMapXMLHandler handler = new CustomMapXMLHandler();
        p.parse(filename, handler);
        continents = handler.getCustomMap();
    }
}