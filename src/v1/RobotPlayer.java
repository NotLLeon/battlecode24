package v1;

import battlecode.common.*;

import static v1.Constants.*;
import static v1.Random.rng;

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

            try {
                curRound = rc.getRoundNum();
                if (curRound % GameConstants.GLOBAL_UPGRADE_ROUNDS == 0) {
                    buyUpgrade();
                }
                if (!rc.isSpawned()){
                    MapLocation[] spawnLocs = rc.getAllySpawnLocations();
                    for (int i = 0; i < 100; i++) {
                        MapLocation randomLoc = spawnLocs[rng.nextInt(spawnLocs.length)];
                        if (rc.canSpawn(randomLoc)) rc.spawn(randomLoc);
                    }
                }
                else {
                    Micro.run();
                    if (curRound <= GameConstants.SETUP_ROUNDS) SetupPhase.run();
                    else MainPhase.run();
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
    private static void buyUpgrade() throws GameActionException {
        if (rc.canBuyGlobal(FIRST_UPGRADE)) rc.buyGlobal(FIRST_UPGRADE);
        else if (rc.canBuyGlobal(SECOND_UPGRADE)) rc.buyGlobal(SECOND_UPGRADE);
    }
}
