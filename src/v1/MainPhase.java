package v1;

import static v1.Constants.*;
import static v1.Random.nextDir;
import static v1.Random.nextInt;
import static v1.Random.rng;

import battlecode.common.*;

// MAIN PHASE STRATEGY HERE (TENTATIVE)
public class MainPhase extends Robot {

//    private static MapLocation destLoc = null;
    private static MapLocation[] friendlySpawnLocs = rc.getAllySpawnLocations();

    private static void onBroadcast() throws GameActionException {
        if (Comms.readLoc(COMMS_ENEMY_FLAGS_START_IND) != null) {
            return;
        }

        MapLocation[] approxFlagLocs = rc.senseBroadcastFlagLocations();
        for (int i = 0; i < GameConstants.NUMBER_FLAGS; ++i) {
            int comms_ind = COMMS_ENEMY_FLAGS_START_IND + i;
            Comms.writeLoc(comms_ind, approxFlagLocs[i]);
        }
    }

    private static MapLocation getClosestFriendlySpawn() {
        MapLocation curLoc = rc.getLocation();
        MapLocation closestSpawn = friendlySpawnLocs[0];
        for (int i = 1; i < friendlySpawnLocs.length; ++i) {
            MapLocation spawn = friendlySpawnLocs[i];
            if (curLoc.distanceSquaredTo(spawn) < curLoc.distanceSquaredTo(closestSpawn)) {
                closestSpawn = spawn;
            }
        }
        return closestSpawn;
    }

    public static void run() throws GameActionException {
        if (rc.getRoundNum() % GameConstants.FLAG_BROADCAST_UPDATE_INTERVAL == 0) {
            onBroadcast();
        }

        if (rc.hasFlag()){
            moveTo(getClosestFriendlySpawn());
            return;
        }

        FlagInfo[] visibleEnemyFlags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
        for (FlagInfo flag : visibleEnemyFlags) {

            // FIXME: might alternate between moving to 2 different flags
            if (!flag.isPickedUp()) {
                moveTo(flag.getLocation());
                return;
            }
        }

        // just rush whichever flag is stored first in comms
        // TODO: mark as captured/removed and move on to next flag
        MapLocation destFlag = Comms.readLoc(COMMS_ENEMY_FLAGS_START_IND);
        if (destFlag == null) return;

        // TODO: explore within some radius
        if (rc.getLocation().isAdjacentTo(destFlag)) Explore.exploreNewArea();
        else moveToAdjacent(destFlag);

        // Rarely attempt placing traps behind the robot.
        MapLocation prevLoc = rc.getLocation().subtract(nextDir());
        if (rc.canBuild(TrapType.EXPLOSIVE, prevLoc) && nextInt(2) == 0)
            rc.build(TrapType.EXPLOSIVE, prevLoc);
        // We can also move our code into different methods or classes to better organize it!
    }
}