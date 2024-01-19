package v2s1;

import battlecode.common.*;

import static v2s1.Constants.*;

/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public strictfp class RobotPlayer {

    static int curRound = 0;

    static Role role = Role.GENERAL;

    public static void run(RobotController rc) throws GameActionException {
        
        Constants.rc = rc;
        Spawner.init();
        Random.initRandom();

        while (true) {

            try {
                curRound = rc.getRoundNum();
                boolean isSetupPhase = curRound <= GameConstants.SETUP_ROUNDS;
                tryBuyUpgrade();
                if (!rc.isSpawned()) {
                    boolean isSpawned = Spawner.spawn();
                    if (!isSpawned) continue;
                }
                if (isSetupPhase) SetupPhase.run();
                else MainPhase.run();

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

    private static void tryBuyUpgrade() throws GameActionException {
        if (curRound % GameConstants.GLOBAL_UPGRADE_ROUNDS == 0) {
            if (rc.canBuyGlobal(FIRST_UPGRADE)) rc.buyGlobal(FIRST_UPGRADE);
            else if (rc.canBuyGlobal(SECOND_UPGRADE)) rc.buyGlobal(SECOND_UPGRADE);
            else if (rc.canBuyGlobal(THIRD_UPGRADE)) rc.buyGlobal(THIRD_UPGRADE);
        }
    }
}