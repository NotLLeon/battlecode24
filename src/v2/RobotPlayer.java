package v2;

import battlecode.common.*;

import static v2.Constants.*;

/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public strictfp class RobotPlayer {

    static int curRound = 0;

    static Role role = Role.GENERAL;

    static MapLocation spawnLoc = null;

    public static void run(RobotController rc) throws GameActionException {
        
        Constants.rc = rc;
        Spawner.init();
        Random.initRandom(rc.getID());

        while (true) {

            try {
                curRound = rc.getRoundNum();
                boolean isMainPhase = curRound <= GameConstants.SETUP_ROUNDS;
                if (curRound % GameConstants.GLOBAL_UPGRADE_ROUNDS == 0) {
                    buyUpgrade();
                }
                if (!rc.isSpawned()) Spawner.spawn();
                else {
                    Micro.run();
                    if (isMainPhase) SetupPhase.run();
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
