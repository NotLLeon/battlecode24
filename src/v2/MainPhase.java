package v2;

import static v2.Constants.*;
import static v2.Random.*;

import battlecode.common.*;

// MAIN PHASE STRATEGY HERE (TENTATIVE)
public class MainPhase extends Robot {

    private static final int[] FLAG_INDS = {0, 1, 2};

    // TODO: should be based on map size
    private static final int LONG_TARGET_ROUND_INTERVAL = 100;
    private static final int SHORT_TARGET_ROUND_INTERVAL = 30;
    private static final int DISTRESS_HELP_DISTANCE_SQUARED_LO = 100;
    private static final int DISTRESS_HELP_DISTANCE_SQUARED_HI = 400;

    private static MapLocation[] friendlySpawnLocs = rc.getAllySpawnLocations();

    private static void onBroadcast() throws GameActionException {
        FlagRecorder.setApproxFlagLocs();
    }

    private static void checkDistressSignal() throws GameActionException {
        // TODO: Experiment with this, how far away to go help, when to check for help
        MapLocation distressLoc = FlagDefense.readDistressLoc();
        if (distressLoc == null) return;
        int dist = rc.getLocation().distanceSquaredTo(distressLoc);

        // determine severity
        int level = FlagDefense.readDistressLevel(distressLoc);
        int threshold = DISTRESS_HELP_DISTANCE_SQUARED_LO;
        if (level == 1) {
            threshold = DISTRESS_HELP_DISTANCE_SQUARED_HI;
        }
        
        if (dist < threshold) {
            if (dist < GameConstants.VISION_RADIUS_SQUARED &&
                    rc.senseNearbyFlags(-1, rc.getTeam()).length == 0) {
                FlagDefense.stopDistressLoc(distressLoc);
            } else moveTo(distressLoc);
        }
    }

    private static void moveToRushLoc() throws GameActionException {
        // visit a flag that hasn't been picked up
        // if all flags are picked up, patrol default locs
        // switch targets every LONG_TARGET_ROUND_INTERVAL rounds if we are looking for nonpicked up flags
        // and every SHORT_TARGET_ROUND_INTERVAL if we are patrolling
        int[] rushFlagInds = Utils.filterIntArr(FLAG_INDS, (i) -> !FlagRecorder.isPickedUp(i));
        int interval = LONG_TARGET_ROUND_INTERVAL;

        // all flags are picked up, just cycle between default locs
        // TODO: escort flags back instead
        if (rushFlagInds.length == 0) {
            rushFlagInds = FLAG_INDS;
            interval = SHORT_TARGET_ROUND_INTERVAL;
        }

        // TODO: modify so that rushLoc doesnt change prematurely when the array changes
        int rushInd = rushFlagInds[(rc.getRoundNum() / interval) % rushFlagInds.length];
        MapLocation rushLoc = FlagRecorder.getFlagLoc(rushInd);

        if (rc.getLocation().isAdjacentTo(rushLoc) && !FlagRecorder.isExactLoc(rushInd)) {
            // TODO: only explore within some radius
            Explore.exploreNewArea();
        } else moveToAdjacent(rushLoc);

    }

    public static void run() throws GameActionException {
        if ((rc.getRoundNum() - 1) % GameConstants.FLAG_BROADCAST_UPDATE_INTERVAL == 0) {
            onBroadcast();
        }

        FlagDefense.scanAndSignal();

        if (rc.hasFlag()){
            FlagInfo pickedUpFlag = rc.senseNearbyFlags(0)[0];

            moveTo(findClosestLoc(friendlySpawnLocs));

            int flagId = pickedUpFlag.getID();
            if(!rc.hasFlag()) FlagRecorder.setCaptured(flagId);
            else FlagRecorder.notifyCarryingFlag(flagId);

        } else {

            // check if any flags have been dropped and returned
            for (int i = 0; i < GameConstants.NUMBER_FLAGS; ++i) FlagRecorder.checkFlagReturned(i);

            checkDistressSignal();

            if (rc.isMovementReady()) moveToRushLoc();
        }

        FlagInfo[] visibleEnemyFlags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
        for (FlagInfo flag : visibleEnemyFlags) FlagRecorder.foundFlag(flag);

        // Rarely attempt placing traps behind the robot.
        MapLocation prevLoc = rc.getLocation().subtract(nextDir());
        if (rc.canBuild(TrapType.EXPLOSIVE, prevLoc) && nextInt(2) == 0)
            rc.build(TrapType.EXPLOSIVE, prevLoc);
    }
}