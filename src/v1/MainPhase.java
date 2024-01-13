package v1;

import static v1.Constants.*;
import static v1.Random.*;

import battlecode.common.*;
import battlecode.world.Flag;

// MAIN PHASE STRATEGY HERE (TENTATIVE)
public class MainPhase extends Robot {

    private static MapLocation[] friendlySpawnLocs = rc.getAllySpawnLocations();
    private static FlagInfo pickedUpFlag = null;

    private static void onBroadcast() throws GameActionException {
        FlagRecorder.setApproxFlagLocs();
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
            // does this work??
            if (pickedUpFlag == null) pickedUpFlag = rc.senseNearbyFlags(0)[0];

            moveTo(getClosestFriendlySpawn());

            if(!rc.hasFlag()) {
                FlagRecorder.setCaptured(pickedUpFlag.getID());
                pickedUpFlag = null;
            }
            return;
        }
        // just rush the first noncaptured flag
        MapLocation flagLoc = null;
        for (int i = 0; i < GameConstants.NUMBER_FLAGS; ++i) {
            if (!FlagRecorder.isPickedUp(i)) {
                flagLoc = FlagRecorder.getFlagLoc(i);
                break;
            }
        }

        if (flagLoc == null) return;

        // TODO: explore within some radius
        if (rc.getLocation().isAdjacentTo(flagLoc)) Explore.exploreNewArea();
        else {
//            rc.setIndicatorString("moving to " + flagLoc);
            moveToAdjacent(flagLoc);
        }

        FlagInfo[] visibleEnemyFlags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
        for (FlagInfo flag : visibleEnemyFlags) {
            if (!flag.isPickedUp()) FlagRecorder.foundExactLoc(flag);
        }

        // Rarely attempt placing traps behind the robot.
        MapLocation prevLoc = rc.getLocation().subtract(nextDir());
        if (rc.canBuild(TrapType.EXPLOSIVE, prevLoc) && nextInt(2) == 0)
            rc.build(TrapType.EXPLOSIVE, prevLoc);
        // We can also move our code into different methods or classes to better organize it!
    }
}