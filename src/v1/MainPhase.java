package v1;

import static v1.Constants.*;
import static v1.Random.*;

import battlecode.common.*;

import java.util.function.Function;

// MAIN PHASE STRATEGY HERE (TENTATIVE)
public class MainPhase extends Robot {

    private static final int[] FLAG_INDS = {0, 1, 2};
    private static final int TARGET_ROUND_INTERVAL = 100; // modify based on map size?
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
            // switch targets every TARGET_ROUND_INTERVAL rounds
            int[] rushFlagInds = filterFlagInds((i) -> !FlagRecorder.isPickedUp(i));
            if (rushFlagInds.length == 0) rushFlagInds = filterFlagInds((i) -> !FlagRecorder.isCaptured(i));
            if (rushFlagInds.length == 0) rushFlagInds = FLAG_INDS;

            int rushInd = rushFlagInds[(rc.getRoundNum() / TARGET_ROUND_INTERVAL) % rushFlagInds.length];
            MapLocation rushLoc = FlagRecorder.getFlagLoc(rushInd);

            // TODO: explore within some radius if we are close to approx loc of flag
            if (rc.getLocation().isAdjacentTo(rushLoc) && !FlagRecorder.isExactLoc(rushInd)) {
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