package v0;

import battlecode.common.*;

import static v0.Random.rng;

/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public strictfp class RobotPlayer {

    static int turnCount = 0;

    static int curRound = 0;

    public static void run(RobotController rc) throws GameActionException {
        
        Constants.rc = rc;
        Random.initRandom(rc.getID());

        while (true) {
            turnCount += 1;  // We have now been alive for one more turn!

            try {
                curRound = rc.getRoundNum();
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
                // went over bytecode limit and computation took >1 turn
                if (curRound != rc.getRoundNum()) {
                    System.out.println("Went over bc limit");
                }

                Clock.yield();
            }
        }
    }
    
}
