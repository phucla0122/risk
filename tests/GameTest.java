import org.junit.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

public class GameTest {
    Game game;
    HashMap<String, Boolean> playerNames;

    @Before
    public void SetUp() {
        game = new Game();
        playerNames = new HashMap<>();
        playerNames.put("a",false);
        playerNames.put("b",false);
        game.initialize(playerNames);
    }

    @After
    public void TearDown(){
        game = null;
        playerNames = null;
    }

    @Test
    public void testMovePhase() {

        Player p  = game.getCurrentPlayer();
        Territory t1 = p.getAllLandOwned().get(0);
        Territory t2 = p.getAllLandOwned().get(1);
        int i = 4, k = 9, j = 1;
        t1.setNumArmies(k);
        t2.setNumArmies(j);


        game.movePhase(i, t1, t2);
        assertEquals(t1.getNumArmies(), t2.getNumArmies());
        assertEquals(t1.getNumArmies() + i, k);
        assertEquals(t2.getNumArmies() - i, j);
        assertNotEquals(p, game.getCurrentPlayer());

        k = 1;
        j = 10;
        t1.setNumArmies(k);
        t2.setNumArmies(j);
        game.movePhase(i,t1,t2);
        assertEquals(t1.getNumArmies() , k);
        assertEquals(t2.getNumArmies()  , j);
        assertEquals(p,game.getCurrentPlayer());

    }

    /**
     * This method test place phase when player add all bonus armies into a single territory
     */
    @Test
    public void testPlacePhaseSingleTer() {

        Player p = game.getCurrentPlayer();
        Territory placing = p.getAllLandOwned().get(0);
        int placingArmies = placing.getNumArmies();
        HashMap<String,Integer> mt = new HashMap<>();
        mt.put(placing.getId(), 3);
        game.placePhase(mt);
        assertEquals(placingArmies + 3, placing.getNumArmies());
    }

    /**
     * tests to see if all players have the right amount of territories and armies total
     *
     * @author Robell Gabriel
     */
    @Test
    public void testInitialize() {

        int totalTerr = 0;

        for (Player player : game.getActivePlayers()) {
            totalTerr += player.getAllLandOwnedSize();
            int totalArm = 0;
            for (Territory territory : player.getAllLandOwned()){
                totalArm += territory.getNumArmies();
            }
            assertEquals(50, totalArm);
        }
        assertEquals(42, totalTerr);
        totalTerr = 0;

        playerNames.put("c", false);
        game = new Game();
        game.initialize(playerNames);
        for (Player player : game.getActivePlayers()) {
            totalTerr += player.getAllLandOwnedSize();
            int totalArm = 0;
            for (Territory territory : player.getAllLandOwned()){
                totalArm += territory.getNumArmies();
            }
            assertEquals(35, totalArm);
        }
        assertEquals(42, totalTerr);
        totalTerr = 0;

        playerNames.put("d",false);
        game = new Game();
        game.initialize(playerNames);
        for (Player player : game.getActivePlayers()) {
            totalTerr += player.getAllLandOwnedSize();
            int totalArm = 0;
            for (Territory territory : player.getAllLandOwned()){
                totalArm += territory.getNumArmies();
            }
            assertEquals(30, totalArm);
        }
        assertEquals(42, totalTerr);
        totalTerr = 0;

        playerNames.put("e",false);
        game = new Game();
        game.initialize(playerNames);
        for (Player player : game.getActivePlayers()) {
            totalTerr += player.getAllLandOwnedSize();
            int totalArm = 0;
            for (Territory territory : player.getAllLandOwned()){
                totalArm += territory.getNumArmies();
            }
            assertEquals(25, totalArm);
        }
        assertEquals(42, totalTerr);
        totalTerr = 0;

        playerNames.put("f",false);
        game = new Game();
        game.initialize(playerNames);
        for (Player player : game.getActivePlayers()) {
            totalTerr += player.getAllLandOwnedSize();
            int totalArm = 0;
            for (Territory territory : player.getAllLandOwned()){
                totalArm += territory.getNumArmies();
            }
            assertEquals(20, totalArm);
        }
        assertEquals(42, totalTerr);
    }

    /**
     * This method test place phase when player distribute multiple armies to multiple territories
     */
    @Test
    public void testPlacePhaseMultipleTer() {
        //Testing place phase for multiple territories involved
        Territory testTer1, testTer2, testTer3;
        int ter1Armies, ter2Armies, ter3Armies;
        Player p = game.getCurrentPlayer();
        //Creating multiple territory
        testTer1 = p.getAllLandOwned().get(0);
        ter1Armies = testTer1.getNumArmies();
        testTer2 = p.getAllLandOwned().get(1);
        ter2Armies = testTer2.getNumArmies();
        testTer3 = p.getAllLandOwned().get(2);
        ter3Armies = testTer3.getNumArmies();
        //Making hashmap to fulfil the parameter of place phase in game model
        HashMap<String, Integer> mt = new HashMap<>();
        //Setting up for testing
        mt.put(testTer1.getId(), 3);
        mt.put(testTer2.getId(), 2);
        mt.put(testTer3.getId(), 1);
        game.placePhase(mt);
        assertEquals(ter1Armies + 3, testTer1.getNumArmies());
        assertEquals(ter2Armies + 2, testTer2.getNumArmies());
        assertEquals(ter3Armies + 1, testTer3.getNumArmies());
    }

    @Test

    public void testAttackWon() {
        Player player1 = game.getActivePlayers().get(0);
        Player player2 = game.getActivePlayers().get(1);

        // Remove all territories from player 2
        List<Territory> player2Land = player2.getAllLandOwned();
        player2Land.clear();

        Territory attacking = new Territory("Attacking", "ATT", List.of());
        attacking.setPlayer(player1);
        attacking.setNumArmies(10);
        player1.addTerritory(attacking);

        Territory defending1 = new Territory("Defending", "DEF", List.of());
        Territory defending2 = new Territory("Defending", "DEF", List.of());
        defending1.setPlayer(player2);
        defending1.setNumArmies(2);
        player2.addTerritory(defending1);
        defending2.setPlayer(player2);
        defending2.setNumArmies(2);
        player2.addTerritory(defending2);

        assertTrue(game.getActivePlayers().contains(player2));
        assertEquals(player2.getAllLandOwnedSize(), 2);

        game.attackWon(attacking, defending1, 3);
        assertEquals(defending1.getNumArmies(), 3);
        assertEquals(defending1.getOwner(), player1);
        assertTrue(player1.getAllLandOwned().contains(defending1));

        game.attackWon(attacking, defending2, 3);
        assertFalse(game.getActivePlayers().contains(player2));
        assertEquals(defending2.getNumArmies(), 3);
        assertEquals(defending2.getOwner(), player1);
        assertTrue(player1.getAllLandOwned().contains(defending2));
    }

    /**
     * Test save and load features for GameModel and ActionLog
     *
     * @throws IOException if the GameModelfile is invalid
     * @throws ClassNotFoundException if adding objects from GameModelfile is invalid
     * @throws FileNotFoundException if actionlog file doesnt exist
     *
     * @author Robell Gabriel
     */
    @Test
    public void saveEqualsLoadGame() throws IOException, ClassNotFoundException, FileNotFoundException {
        Game gSave = new Game();
        Game gLoad = new Game();

        Map<String, Boolean> playerNames = new HashMap<>();
        playerNames.put("a",false);
        playerNames.put("b",false);
        gSave.initialize(playerNames);

        gSave.saveGame();
        gLoad.loadGame();

        assertEquals(gSave.toString(), gLoad.toString());

        GameFrame gFSave = new GameFrame();
        GameFrame gFLoad = new GameFrame();

        gFSave.printLine("Testing");
        gFSave.saveActionLog();
        gFLoad.loadActionLog();

        assertEquals(gSave.getActionLog.getText(), gLoad.getActionLog.getText());
    }
    /**
     * Test importing a custom map.
     * @throws IOException If the file cannot be read
     * @throws SAXException If the file is improperly formatted
     * @throws ParserConfigurationException If the parser is incorrectly configured
     * @author Nicolas Tuttle
     */
    @Test
    public void testImportValidMap() throws IOException, SAXException, ParserConfigurationException {
        HashMap<String, Continent> expectedContinents = new HashMap<>();
        expectedContinents.put("NA", new Continent(
                "North America",
                List.of(
                        new Territory("Eastern United States", "NA1", List.of("NA2", "NA3", "NA4")),
                        new Territory("Western United States", "NA2", List.of("NA1", "NA3", "NA4")),
                        new Territory("Northern United States", "NA3", List.of("NA2")),
                        new Territory("Southern United States", "NA4", List.of("NA2"))
                ),
                4
        ));
        Game game = new Game();
        game.importCustomMap("tests/validCustomMap.xml");
        assertEquals(expectedContinents, game.getContinents());
    }

    /**
     * Test importing an invalid custom map.
     * @throws IOException If the file cannot be read
     * @throws SAXException Should throw this as the file is invalid
     * @throws ParserConfigurationException If the parser is incorrectly configured
     * @author Nicolas Tuttle
     */
    @Test(expected = SAXException.class)
    public void testImportInvalidMap() throws IOException, SAXException, ParserConfigurationException {
        Game game = new Game();
        game.importCustomMap("tests/invalidCustomMap.xml");
    }
}
