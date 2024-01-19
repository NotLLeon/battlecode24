package v2;

import static v2.Constants.*;

import battlecode.common.*;

// MAIN PHASE STRATEGY HERE (TENTATIVE)
public class MainPhase extends Robot {

    private static final int[] FLAG_INDS = {0, 1, 2};

    // TODO: should be based on map size
    private static final int LONG_TARGET_ROUND_INTERVAL = 100;
    private static final int SHORT_TARGET_ROUND_INTERVAL = 30;
    private static final int DISTRESS_HELP_DISTANCE_SQUARED = 100;

    private static void onBroadcast() throws GameActionException {
        FlagRecorder.setApproxFlagLocs();
    }

    private static void checkDistressSignal() throws GameActionException {
        // TODO: Experiment with this, how far away to go help, when to check for help
        MapLocation distressLoc = FlagDefense.readDistress();
        if (distressLoc == null) return;
        int dist = rc.getLocation().distanceSquaredTo(distressLoc);
        if (dist < DISTRESS_HELP_DISTANCE_SQUARED) {
            if (dist < GameConstants.VISION_RADIUS_SQUARED &&
                    rc.senseNearbyFlags(-1, rc.getTeam()).length == 0) {
                FlagDefense.stopDistressLoc(distressLoc);
            } else moveTo(distressLoc);
        }
    }

    private static void moveToRushLoc() throws GameActionException {
        if (!rc.isMovementReady()) return;

        MapLocation curLoc = rc.getLocation();
        MapLocation rushLoc = null;
        int minDis = 99999999;
        for (int flagInd : FLAG_INDS) {
            MapLocation testLoc = FlagRecorder.getFlagLoc(flagInd);
            int testDis = curLoc.distanceSquaredTo(testLoc);
            if (!FlagRecorder.isPickedUp(flagInd) && testDis < minDis) {
                rushLoc = testLoc;
                minDis = testDis;
            }
        }

        if (rushLoc == null) {
            int rushInd = FLAG_INDS[(rc.getRoundNum() / SHORT_TARGET_ROUND_INTERVAL) % FLAG_INDS.length];
            rushLoc = FlagRecorder.getFlagLoc(rushInd);
        }
        
        if (curLoc.isWithinDistanceSquared(rushLoc, FLAG_PICKUP_DIS_SQUARED)) {
            Explore.exploreNewArea();
        } else moveToAdjacent(rushLoc);
    }

    private static void runStrat() throws GameActionException {
        if ((rc.getRoundNum() - 1) % GameConstants.FLAG_BROADCAST_UPDATE_INTERVAL == 0) {
            onBroadcast();
        }

        FlagDefense.scanAndSignal();

        if (rc.hasFlag()) {
            FlagInfo[] enemyFlags = rc.senseNearbyFlags(0, rc.getTeam().opponent());

            FlagInfo pickedUpFlag = enemyFlags[0];
            for (FlagInfo flag : enemyFlags) {
                if (flag.isPickedUp()) {
                    pickedUpFlag = flag;
                    break;
                }
            }

            moveTo(Utils.findClosestLoc(Spawner.getSpawnCenters()));

            int flagId = pickedUpFlag.getID();
            if (!rc.hasFlag()) FlagRecorder.setCaptured(flagId);
            else FlagRecorder.notifyCarryingFlag(flagId);

        } else {

            // check if any flags have been dropped and returned
            for (int i = 0; i < GameConstants.NUMBER_FLAGS; ++i) FlagRecorder.checkFlagReturned(i);

            checkDistressSignal();

            if (!Micro.inCombat()) moveToRushLoc();
        }

        FlagInfo[] visibleEnemyFlags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
        for (FlagInfo flag : visibleEnemyFlags) FlagRecorder.foundFlag(flag);

    }

    public static void run() throws GameActionException {
        Micro.run();
        runStrat();
        Micro.run();
    }
}