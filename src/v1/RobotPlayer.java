package v1;

import battlecode.common.*;

import static v1.Random.rng;

/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public strictfp class RobotPlayer {

    /**
     * We will use this variable to count the number of turns this robot has been alive.
     * You can use static variables like this to save any information you want. Keep in mind that even though
     * these variables are static, in Battlecode they aren't actually shared between your robots.
     */
    static int turnCount = 0;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        
        Constants.rc = rc;
        Random.initRandom(rc.getID());

        while (true) {
            turnCount += 1;  // We have now been alive for one more turn!

            try {
                if (!rc.isSpawned()){
                    MapLocation[] spawnLocs = rc.getAllySpawnLocations();
                    for (int i = 0; i < 100; i++) {
                        MapLocation randomLoc = spawnLocs[rng.nextInt(spawnLocs.length)];
                        if (rc.canSpawn(randomLoc)) rc.spawn(randomLoc);
                    }
                }
                else {
                    if (turnCount < GameConstants.SETUP_ROUNDS) {
                        // we are in setup phase
                        SetupPhase.run();
                    } else {
                        // else run main phase logic
                        MainPhase.run();
                    }
                }

            } catch (GameActionException e) {
                System.out.println("GameActionException");
                e.printStackTrace();

            } catch (Exception e) {
                System.out.println("Exception");
                e.printStackTrace();

            } finally {
                Clock.yield();
            }
        }
    }
    
}
