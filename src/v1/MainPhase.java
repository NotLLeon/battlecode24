package v1;

import static v1.Constants.*;
import static v1.Random.*;

import battlecode.common.*;

// MAIN PHASE STRATEGY HERE (TENTATIVE)
public class MainPhase extends Robot {

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

            if(!rc.hasFlag()) {
                FlagRecorder.setCaptured(pickedUpFlag.getID());
                pickedUpFlag = null;
            }
            return;
        }
        // just rush the first non pickedup flag
        // if all are pickedup (possibly dropped and respawned), go to non captured flags
        MapLocation flagLoc = null;
        for (int i = 0; i < GameConstants.NUMBER_FLAGS; ++i) {
            if (!FlagRecorder.isPickedUp(i)) {
                flagLoc = FlagRecorder.getFlagLoc(i);
                break;
            }
            if (!FlagRecorder.isCaptured(i)) {
                flagLoc = FlagRecorder.getFlagLoc(i);
            }
        }

        if (flagLoc == null) return;

        // TODO: explore within some radius
        if (rc.getLocation().isAdjacentTo(flagLoc)) Explore.exploreNewArea();
        else {
            moveToAdjacent(flagLoc);
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