package v1;

import static v1.Constants.*;
import static v1.Random.*;

import battlecode.common.*;

import java.util.function.Function;
import static v1.Constants.directions;
import static v1.Constants.rc;
import static v1.Constants.Role;
import static v1.Random.nextDir;
import static v1.Random.nextInt;
import static v1.Random.rng;

import static v1.RobotPlayer.role;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.TrapType;
import v1.Constants.Role;
import battlecode.common.MapInfo;

// MAIN PHASE STRATEGY HERE (TENTATIVE)
public class MainPhase extends Robot {

    private static final int[] FLAG_INDS = {0, 1, 2};
    private static final int LONG_TARGET_ROUND_INTERVAL = 100; // modify based on map size?
    private static final int SHORT_TARGET_ROUND_INTERVAL = 20;
    private static final int DISTRESS_HELP_DISTANCE = 10;
    private static MapLocation[] friendlySpawnLocs = rc.getAllySpawnLocations();
    private static FlagInfo pickedUpFlag = null;

    private static void onBroadcast() throws GameActionException {
        FlagRecorder.setApproxFlagLocs();
    }

    @FunctionalInterface
    public interface CheckedFunction<T, R> {
        R apply (T t) throws GameActionException;
    }

    private static Function<Integer, Boolean> lambdaExceptionWrapper(CheckedFunction<Integer, Boolean> fn) {
        return i -> {
            try { return fn.apply(i); }
            catch(GameActionException e) { return false; }
        };
    }

    private static int[] filterFlagInds(CheckedFunction<Integer, Boolean> fn) {
        int numRemaining = 0;
        Function<Integer, Boolean> sfn = lambdaExceptionWrapper(fn);

        // idk if this is the best way to do this
        for (int ind : FLAG_INDS) if (sfn.apply(ind)) ++numRemaining;

        int[] filtered = new int[numRemaining];
        int i = 0;
        for (int ind : FLAG_INDS) if (sfn.apply(ind)) filtered[i++] = ind;
        return filtered;
    }

    public static void run() throws GameActionException {
        if (rc.getRoundNum() % GameConstants.FLAG_BROADCAST_UPDATE_INTERVAL == 0) {
            onBroadcast();
        }

        // TODO: Experiment with this, how far away to go help, when to check for help
        FlagDefense.scanAndSignal();
        MapLocation distressLoc = FlagDefense.readDistress();
        if (distressLoc != null) {
            if (rc.getLocation().distanceSquaredTo(distressLoc) < 10) {
                moveTo(distressLoc);
                return;
            }
        }
        
        
        if (rc.canPickupFlag(rc.getLocation())){
            rc.pickupFlag(rc.getLocation());
            rc.setIndicatorString("Holding a flag!");
        }

        if (rc.hasFlag()){
            if (pickedUpFlag == null) pickedUpFlag = rc.senseNearbyFlags(0)[0];

            moveTo(findClosestLoc(friendlySpawnLocs));

            if(!rc.hasFlag()) {
                FlagRecorder.setCaptured(pickedUpFlag.getID());
                pickedUpFlag = null;
            }
        } else {
            // prioritize flags that have not been picked up
            // if all have been picked up, visit noncaptured flags
            // switch targets every LONG_TARGET_ROUND_INTERVAL rounds if we are looking for nonpicked up flags
            // and every SHORT_TARGET_ROUND_INTERVAL if we are looking for noncaptured flags
            int[] rushFlagInds = filterFlagInds((i) -> !FlagRecorder.isPickedUp(i));
            int interval = LONG_TARGET_ROUND_INTERVAL;

            if (rushFlagInds.length == 0) {
                rushFlagInds = filterFlagInds((i) -> !FlagRecorder.isCaptured(i));
                interval = SHORT_TARGET_ROUND_INTERVAL;
            }

            // FlagRecorder thinks all flags are captured, something is broken if this executes
            if (rushFlagInds.length == 0) rushFlagInds = FLAG_INDS;

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