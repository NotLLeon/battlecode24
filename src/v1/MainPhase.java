package v1;

import static v1.Constants.*;
import static v1.Random.*;

import battlecode.common.*;

// MAIN PHASE STRATEGY HERE (TENTATIVE)
public class MainPhase extends Robot {

    private static final int[] FLAG_INDS = {0, 1, 2};

    // TODO: should be based on map size
    private static final int LONG_TARGET_ROUND_INTERVAL = 100;
    private static final int SHORT_TARGET_ROUND_INTERVAL = 20;
    private static MapLocation[] friendlySpawnLocs = rc.getAllySpawnLocations();
    private static FlagInfo pickedUpFlag = null;

    private static void onBroadcast() throws GameActionException {
        FlagRecorder.setApproxFlagLocs();
    }

    public static void run() throws GameActionException {
        if (rc.getRoundNum() % GameConstants.FLAG_BROADCAST_UPDATE_INTERVAL == 0) {
            onBroadcast();
        }

        if (rc.hasFlag()){
            if (pickedUpFlag == null) pickedUpFlag = rc.senseNearbyFlags(0)[0];

            moveTo(findClosestLoc(friendlySpawnLocs));

            int flagId = pickedUpFlag.getID();
            if(!rc.hasFlag()) {
                FlagRecorder.setCaptured(flagId);
                pickedUpFlag = null;
            } else FlagRecorder.notifyCarryingFlag(flagId);
        } else {
            // check if any flags have been dropped and returned
            for (int i = 0; i < GameConstants.NUMBER_FLAGS; ++i) FlagRecorder.checkFlagReturned(i);

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

        FlagInfo[] visibleEnemyFlags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
        for (FlagInfo flag : visibleEnemyFlags) {
            if (!flag.isPickedUp()) FlagRecorder.foundFlag(flag);
        }
        // Rarely attempt placing traps behind the robot.
        MapLocation prevLoc = rc.getLocation().subtract(nextDir());
        if (rc.canBuild(TrapType.EXPLOSIVE, prevLoc) && nextInt(2) == 0)
            rc.build(TrapType.EXPLOSIVE, prevLoc);
    }
}