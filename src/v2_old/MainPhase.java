package v2_old;

import static v2_old.Constants.*;

import battlecode.common.*;
import v2_old.micro.Micro;

// MAIN PHASE STRATEGY HERE (TENTATIVE)
public class MainPhase extends Robot {

    private static final int[] FLAG_INDS = {0, 1, 2};

    // TODO: should be based on map size
    private static final int LONG_TARGET_ROUND_INTERVAL = 100;
    private static final int SHORT_TARGET_ROUND_INTERVAL = 30;
    private static final int DISTRESS_HELP_DISTANCE_SQUARED = 100;
    private static boolean shouldExplore = false;
    private static final int FLAG_CONVOY_CONGESTION_THRESHOLD = 4;
    private static boolean shouldPickUpFlag = true;

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

    private static int getRushInd() {
        // visit a flag that hasn't been picked up
        // if all flags are picked up, patrol default locs
        // switch targets every LONG_TARGET_ROUND_INTERVAL rounds if we are looking for nonpicked up flags
        // and every SHORT_TARGET_ROUND_INTERVAL if we are patrolling
        int[] rushFlagInds = Utils.filterIntArr(FLAG_INDS, (i) -> !FlagRecorder.isPickedUp(i));
        int interval = LONG_TARGET_ROUND_INTERVAL;

        // all flags are picked up, just cycle between default locs
        // TODO: escort flags back instead
        if (rushFlagInds.length == 0) {
            rushFlagInds = Utils.filterIntArr(FLAG_INDS, (i) -> !FlagRecorder.isCaptured(i));
            interval = SHORT_TARGET_ROUND_INTERVAL;
        }

        // all flags captured, this is the last round unless something broke
        if (rushFlagInds.length == 0) return 0;

        // TODO: modify so that rushLoc doesnt change prematurely when the array changes
        return rushFlagInds[(rc.getRoundNum() / interval) % rushFlagInds.length];
    }

    private static void moveToRushLoc() throws GameActionException {
        if (!rc.isMovementReady()) return;

        int rushInd = getRushInd();
        MapLocation rushLoc = FlagRecorder.getFlagLoc(getRushInd());
        MapLocation curLoc = rc.getLocation();

        if (curLoc.distanceSquaredTo(rushLoc) >= GameConstants.FLAG_BROADCAST_NOISE_RADIUS) shouldExplore = false;

        if (!FlagRecorder.isExactLoc(rushInd) &&
                (shouldExplore || curLoc.isWithinDistanceSquared(rushLoc, FLAG_PICKUP_DIS_SQUARED))) {
            shouldExplore = true;
            Explore.exploreNewArea();
        } else {
            // grouping attempt
//            Micro.tryFollowLeader(rushLoc);
            moveToAdjacent(rushLoc);
        }
    }

    private static void runStrat() throws GameActionException {
        if ((rc.getRoundNum() - 1) % GameConstants.FLAG_BROADCAST_UPDATE_INTERVAL == 0) {
            onBroadcast();
        }

        FlagDefense.scanAndSignal();

        shouldPickUpFlag = true;

        if (rc.hasFlag()) {
            FlagInfo[] enemyFlags = rc.senseNearbyFlags(0, rc.getTeam().opponent());

            FlagInfo pickedUpFlag = enemyFlags[0];
            for (FlagInfo flag : enemyFlags) {
                if (flag.isPickedUp()) {
                    pickedUpFlag = flag;
                    break;
                }
            }

            MapLocation curLoc = rc.getLocation();

            // check if path ahead is congested and drop flag if so
            MapLocation targetLoc = Utils.findClosestLoc(Spawner.getSpawnCenters());
            RobotInfo[] nearbyBots = rc.senseNearbyRobots(16, rc.getTeam()); // TODO: test diff ranges
            Direction intendedDir = curLoc.directionTo(targetLoc);
            int numBlockingBots = 0;

            for (RobotInfo bot : nearbyBots) {
                if (Utils.inGeneralDirection(curLoc.directionTo(bot.getLocation()), intendedDir)) {
                    numBlockingBots++;
                }
            }

            // TODO: test this number and fix this omega condition
            // if (rc.canDropFlag(curLoc.add(intendedDir)) && (numBlockingBots > FLAG_CONVOY_CONGESTION_THRESHOLD ||
            //         (intendedDir != Direction.CENTER && rc.senseRobotAtLocation(curLoc.add(intendedDir)) != null))) {

            MapLocation intendedLoc = curLoc.add(intendedDir);
            if (numBlockingBots > FLAG_CONVOY_CONGESTION_THRESHOLD && rc.senseRobotAtLocation(intendedLoc) != null) {
                if (rc.canDropFlag(intendedLoc)) {
                    Robot.dropFlag(intendedLoc);
                    shouldPickUpFlag = false;
                }
            } else {
                moveTo(targetLoc);
                int flagId = pickedUpFlag.getID();
                if (!rc.hasFlag()) FlagRecorder.setCaptured(flagId);
                else FlagRecorder.notifyCarryingFlag(flagId);
            }

        } else {

            // check if any flags have been dropped and returned
            for (int i = 0; i < GameConstants.NUMBER_FLAGS; ++i) FlagRecorder.checkFlagReturned(i);

            checkDistressSignal();

            if (!Micro.inCombat()) moveToRushLoc();
        }

        FlagInfo[] visibleEnemyFlags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
        for (FlagInfo flag : visibleEnemyFlags) FlagRecorder.foundFlag(flag);

    }

    public static boolean getShouldPickUpFlag() {
        // TODO: only pick up flag if you're closer to spawn or something like that
        return shouldPickUpFlag;
    }

    public static void run() throws GameActionException {
        Micro.run();
        runStrat();
        Micro.run();
    }
}