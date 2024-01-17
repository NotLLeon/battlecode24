package v2;

import static v2.Constants.*;

import battlecode.common.*;
import v2.Constants.Role;

import v2.fast.FastIterableLocSet;

// MAIN PHASE STRATEGY HERE (TENTATIVE)
public class MainPhase {

    // TODO: should be based on map size
    private static final int LONG_TARGET_ROUND_INTERVAL = 100;
    private static final int SHORT_TARGET_ROUND_INTERVAL = 30;
    private static final int DISTRESS_DISTANCE_DIFF_THRESHOLD = 50;

    private static void onBroadcast() throws GameActionException {
        FlagRecorder.setApproxFlagLocs();
    }

    private static void checkDistressSignal() throws GameActionException {
        // TODO: Experiment with this, how far away to go help, when to check for help
        MapLocation curLoc = rc.getLocation();
        // get distress
        MapLocation[] flagDistressLocs = FlagDefense.readDistressLocInRange(curLoc);
        int empty = 0;
        for (MapLocation loc : flagDistressLocs) {
            if (loc == null) ++empty;
        }
        if (empty == flagDistressLocs.length) return;

        MapLocation target = flagDistressLocs[Random.nextInt(flagDistressLocs.length - empty)];
 
        if (curLoc.distanceSquaredTo(target) < GameConstants.VISION_RADIUS_SQUARED &&
                rc.senseNearbyFlags(-1, rc.getTeam()).length == 0) {
            FlagDefense.stopDistressLoc(target);
        } else Robot.moveTo(target);
    }

    // should only run if Micro doesnt spot the flag
    private static void moveToRushLoc() throws GameActionException {
        int rushInd = getRushInd();
        MapLocation rushLoc = FlagRecorder.getFlagLoc(rushInd);
        if (rc.getLocation().isAdjacentTo(rushLoc) && !FlagRecorder.isExactLoc(rushInd)) {
            // TODO: only explore within some radius
            Explore.exploreNewArea();
        } else Robot.moveToAdjacent(rushLoc);
    }

    private static int getRushInd() throws GameActionException {
        // rush a flag that hasn't been picked up
        // if all flags are picked up, patrol default locs
        // switch targets every LONG_TARGET_ROUND_INTERVAL rounds if we are looking for nonpicked up flags
        // and every SHORT_TARGET_ROUND_INTERVAL if we are patrolling

        int[] rushFlagInds = Utils.filterIntArr(FLAG_INDS, (Integer i) -> !FlagRecorder.isPickedUp(i));
        int interval = LONG_TARGET_ROUND_INTERVAL;

        // all flags are picked up, just cycle between default locs
        // TODO: escort flags back instead
        if (rushFlagInds.length == 0) {
            rushFlagInds = FLAG_INDS;
            interval = SHORT_TARGET_ROUND_INTERVAL;
        }

        // TODO: modify so that rushLoc doesnt change prematurely when the array changes
        return rushFlagInds[(rc.getRoundNum() / interval) % rushFlagInds.length];
    }

    public static MapLocation getRushLoc() throws GameActionException {
        return FlagRecorder.getFlagLoc(getRushInd());
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

            Robot.moveTo(Utils.findClosestLoc(Spawner.getSpawnCenters()));

            int flagId = pickedUpFlag.getID();
            if (!rc.hasFlag()) FlagRecorder.setCaptured(flagId);
            else FlagRecorder.notifyCarryingFlag(flagId);

        } else {

            // check if any flags have been dropped and returned
            for (int i = 0; i < GameConstants.NUMBER_FLAGS; ++i) FlagRecorder.checkFlagReturned(i);

            checkDistressSignal();

            if (rc.isMovementReady()) moveToRushLoc();
        }

        FlagInfo[] visibleEnemyFlags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
        for (FlagInfo flag : visibleEnemyFlags) FlagRecorder.foundFlag(flag);

    }

    public static void run() throws GameActionException {
        if (Robot.role == Role.SIGNAL) {
            SignalBot.run();
            return;
        }
        Micro.run();
        runStrat();
        Micro.run();
    }
}