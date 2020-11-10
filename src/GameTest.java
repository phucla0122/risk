import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

public class GameTest {
    Game game;

    @Test
    public void testMovePhase() {
        game = new Game();
        ArrayList<String> names = new ArrayList<>();
        names.add("Subject1");
        names.add("Patrick");
        game.initialize(2,names);

        Player p  = game.getCurrentPlayer();
        Territory t1 = p.getAllLandOwned().get(0);
        Territory t2 = p.getAllLandOwned().get(1);
        int i = 4, k =9, j = 1;
        t1.setNumArmies(k);
        t2.setNumArmies(j);


        game.movePhase(i, t1,t2);
        assertEquals(t1.getNumArmies() , t2.getNumArmies());
        assertEquals(t1.getNumArmies() + i , k);
        assertEquals(t2.getNumArmies() - i , j);
        assertNotEquals(p,game.getCurrentPlayer());

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
        game = new Game();
        //Testing place phase for 1 specific territory
        Territory testTer = new Territory("Alaska", "NA1", Arrays.asList("NA2", "NA6", "AS6"));
        Player testPlayer = new Player("a");
        testPlayer.addTerritory(testTer);
        testTer.setPlayer(testPlayer);
        HashMap<Territory,Integer> mt = new HashMap<>();
        mt.put(testTer,3);
        game.placePhase(mt);
        assertEquals(3,testTer.getNumArmies());
    }

    /**
     * tests to see if all players have the right amount of territories and armies total
     *
     * @author Robell Gabriel
     */
    @Test
    public void testInitialize() {
        Game game = new Game();
        List<String> playerNames = new ArrayList<>();
        playerNames.add("a");
        playerNames.add("b");
        playerNames.add("c");
        playerNames.add("d");
        playerNames.add("e");
        playerNames.add("f");
        int totalTerr = 0;

        game.initialize(2,playerNames);
        for (Player player : game.getActivePlayers()) {
            totalTerr += player.getAllLandOwned().size();
            int totalArm = 0;
            for (Territory territory : player.getAllLandOwned()){
                totalArm += territory.getNumArmies();
            }
            assertEquals(50, totalArm);
        }
        assertEquals(42, totalTerr);
        totalTerr = 0;

        game = new Game();
        game.initialize(3,playerNames);
        for (Player player : game.getActivePlayers()) {
            totalTerr += player.getAllLandOwned().size();
            int totalArm = 0;
            for (Territory territory : player.getAllLandOwned()){
                totalArm += territory.getNumArmies();
            }
            assertEquals(35, totalArm);
        }
        assertEquals(42, totalTerr);
        totalTerr = 0;

        game = new Game();
        game.initialize(4,playerNames);
        for (Player player : game.getActivePlayers()) {
            totalTerr += player.getAllLandOwned().size();
            int totalArm = 0;
            for (Territory territory : player.getAllLandOwned()){
                totalArm += territory.getNumArmies();
            }
            assertEquals(30, totalArm);
        }
        assertEquals(42, totalTerr);
        totalTerr = 0;

        game = new Game();
        game.initialize(5,playerNames);
        for (Player player : game.getActivePlayers()) {
            totalTerr += player.getAllLandOwned().size();
            int totalArm = 0;
            for (Territory territory : player.getAllLandOwned()){
                totalArm += territory.getNumArmies();
            }
            assertEquals(25, totalArm);
        }
        assertEquals(42, totalTerr);
        totalTerr = 0;

        game = new Game();
        game.initialize(6,playerNames);
        for (Player player : game.getActivePlayers()) {
            totalTerr += player.getAllLandOwned().size();
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
        game = new Game();
        //Testing place phase for multiple territories involved
        Territory testTer1,testTer2,testTer3;
        //Creating multiple territory
        testTer1 = new Territory("Alaska", "NA1", Arrays.asList("NA2", "NA6", "AS6"));
        testTer2 = new Territory("Great Britain (Great Britain & Ireland)", "EU1", Arrays.asList("EU2", "EU3", "EU4", "EU7"));
        testTer3 = new Territory("Afghanistan", "AS1", Arrays.asList("AS2", "AS3", "AS7", "AS11", "EU6"));
        //Create player who will own the territories
        Player testPlayer = new Player("a");
        //Add the territories to the player own list
        testPlayer.addTerritory(testTer1);
        testPlayer.addTerritory(testTer2);
        testPlayer.addTerritory(testTer3);
        //Set the owner of the territory to the player
        testTer1.setPlayer(testPlayer);
        testTer2.setPlayer(testPlayer);
        testTer3.setPlayer(testPlayer);
        //Making hashmap to fulfil the parameter of place phase in game model
        HashMap<Territory,Integer> mt = new HashMap<>();
        //Setting up for testing
        mt.put(testTer1,3);
        mt.put(testTer2,2);
        mt.put(testTer3,1);
        game.placePhase(mt);
        assertEquals(3,testTer1.getNumArmies());
        assertEquals(2,testTer2.getNumArmies());
        assertEquals(1,testTer3.getNumArmies());
    }

    @Test
    public void testAttackWon() {
        game = new Game();
        game.initialize(2, List.of("Patrick", "Spongebob"));

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
        assertEquals(player2.getAllLandOwned().size(), 2);

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
}
