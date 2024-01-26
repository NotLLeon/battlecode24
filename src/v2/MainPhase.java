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
    private static boolean shouldExplore = false;
    private static final int FLAG_CONVOY_CONGESTION_THRESHOLD = 4;
    private static boolean shouldPickUpFlag = true;
    private static final int FLAG_ESCORT_RADIUS_SQUARED = 4;

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
            rushFlagInds = FLAG_INDS;
            interval = SHORT_TARGET_ROUND_INTERVAL;
        }

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

    private static boolean isGoodFlagPickup(MapLocation flagLoc) throws GameActionException {
        RobotInfo[] nearbyBots = rc.senseNearbyRobots(4);
        int numEnemies = 0;
        for (RobotInfo bot : nearbyBots) {
            if (bot.getTeam() == rc.getTeam().opponent()) {
                numEnemies++;
            }
        }
        // TODO: SIMULATD ANNEALING
        if (0.75 * (nearbyBots.length - numEnemies - 1) < numEnemies || !shouldPickUpFlag) return false;
        if (!isDroppedFlag(flagLoc)) return true;
        MapLocation flagDropperLoc = getFlagDropperLoc(flagLoc);
        if (flagDropperLoc == null) return false;
        if (Utils.inGeneralDirection(flagDropperLoc.directionTo(flagLoc), flagDropperLoc.directionTo(rc.getLocation()))) {
            return true;
        }
        return false;
    }

    private static void tryPickupFlag() throws GameActionException {
        if (rc.hasFlag()) return;
        FlagInfo[] nearbyFlags = rc.senseNearbyFlags(4);
        for (FlagInfo flag : nearbyFlags) {
            if (flag.getTeam() != rc.getTeam() && flag.isPickedUp()) continue;
            MapLocation flagLoc = flag.getLocation();
            if (!isGoodFlagPickup(flagLoc)) {
                continue;
            } else if (flag.getTeam() != rc.getTeam()) {
                if (!rc.canPickupFlag(flagLoc)) {
                    moveTo(flag.getLocation());
                }
                if (rc.canPickupFlag(flagLoc)) {
                    Robot.pickupFlag(flagLoc);
                    if (isDroppedFlag(flagLoc)) clearFlagDropLoc(flagLoc);
                    if (!rc.hasFlag()) {
                        FlagRecorder.setCaptured(flag.getID());
                    }
                    FlagRecorder.setPickedUp(flag.getID());
                }
            }
        }
    }

    private static int getFlagDropInd(MapLocation loc) throws GameActionException {
        for (int i = 0; i < GameConstants.NUMBER_FLAGS; ++i) {
            if (Comms.readLoc(COMMS_FLAG_DROP_LOC + i) == null) {
                return i;
            }
        }
        return -1;
    }

    private static void setFlagDropLoc(MapLocation loc) throws GameActionException {
        int ind = getFlagDropInd(loc);
        Comms.writeLoc(COMMS_FLAG_DROP_LOC + ind, loc);
        Comms.writeLoc(COMMS_FLAG_DROPPER_LOC + ind, rc.getLocation());
    }

    private static boolean isDroppedFlag(MapLocation flag) throws GameActionException {
        for (int i = 0; i < GameConstants.NUMBER_FLAGS; ++i) {
            MapLocation loc = Comms.readLoc(COMMS_FLAG_DROP_LOC + i);
            if (loc != null && loc.equals(flag)) {
                return true;
            }
        }
        return false;
    }

    private static MapLocation getFlagDropperLoc(MapLocation flag) throws GameActionException {
        for (int i = 0; i < GameConstants.NUMBER_FLAGS; ++i) {
            MapLocation loc = Comms.readLoc(COMMS_FLAG_DROP_LOC + i);
            if (loc != null && loc.equals(flag)) {
                return Comms.readLoc(COMMS_FLAG_DROPPER_LOC + i);
            }
        }
        return null;
    }

    private static void clearFlagDropLoc(MapLocation flag) throws GameActionException {
        for (int i = 0; i < GameConstants.NUMBER_FLAGS; ++i) {
            MapLocation loc = Comms.readLoc(COMMS_FLAG_DROP_LOC + i);
            if (loc != null && loc.equals(flag)) {
                Comms.write(COMMS_FLAG_DROP_LOC + i, 0);
                Comms.write(COMMS_FLAG_DROPPER_LOC + i, 0);
            }
        }
    }

    private static void escortFlag(MapLocation flagLoc) throws GameActionException {
        MapLocation curLoc = rc.getLocation();
        Direction moveDir;
        if (curLoc.isWithinDistanceSquared(flagLoc, FLAG_ESCORT_RADIUS_SQUARED)) moveDir = flagLoc.directionTo(curLoc);
        else moveDir = curLoc.directionTo(flagLoc);

        if (rc.canMove(moveDir)) rc.move(moveDir);
    }

    private static void tryMoveToFlag() throws GameActionException {
        // move towards dropped enemy flags and picked up friendly flags
        if (!rc.isMovementReady() || !shouldPickUpFlag || !rc.hasFlag()) return;
        FlagInfo[] nearbyFlags = rc.senseNearbyFlags(-1);

        FlagInfo targetFlag = null;
        for (FlagInfo flag : nearbyFlags) {
            Team flagTeam = flag.getTeam();

            // chase picked up friendly flags
            if (flagTeam == rc.getTeam() && flag.isPickedUp()
                    && rc.getRoundNum() > GameConstants.SETUP_ROUNDS) {
                targetFlag = flag;
                break;
            }

            // move towards enemy flags
            if (flagTeam != rc.getTeam()) targetFlag = flag;
        }

        if (targetFlag == null) return;

        MapLocation flagLoc = targetFlag.getLocation();

        if (targetFlag.getTeam() != rc.getTeam() && targetFlag.isPickedUp()) {
            escortFlag(flagLoc);
        } else if (rc.canPickupFlag(flagLoc) && isGoodFlagPickup(flagLoc)) {
            Robot.pickupFlag(flagLoc);
            if (isDroppedFlag(flagLoc)) clearFlagDropLoc(flagLoc);
            if (!rc.hasFlag()) {
                FlagRecorder.setCaptured(targetFlag.getID());
            }
            FlagRecorder.setPickedUp(targetFlag.getID());
        } else {
            moveTo(flagLoc);
        }
    }

    private static void runStrat() throws GameActionException {
        if ((rc.getRoundNum() - 1) % GameConstants.FLAG_BROADCAST_UPDATE_INTERVAL == 0) {
            onBroadcast();
        }

        FlagDefense.scanAndSignal();

        tryMoveToFlag();

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
            // Direction intendedDir = Robot.getNextDirection(targetLoc);
            Direction intendedDir = curLoc.directionTo(targetLoc);

            int numBlockingBots = 0;

            for (RobotInfo bot : nearbyBots) {
                if (Utils.inGeneralDirection(curLoc.directionTo(bot.getLocation()), intendedDir)) {
                    numBlockingBots++;
                }
            }
            MapLocation intendedLoc = curLoc.add(intendedDir);
            RobotInfo blockingBot = rc.senseRobotAtLocation(intendedLoc);

            rc.setIndicatorString(intendedDir.toString() + " " + targetLoc + " " + curLoc + " " + numBlockingBots);

            if (intendedDir != Direction.CENTER &&
                (numBlockingBots > FLAG_CONVOY_CONGESTION_THRESHOLD && 
                (blockingBot != null && blockingBot.getTeam() == rc.getTeam()))) {
                if (rc.canDropFlag(intendedLoc)) {
                    setFlagDropLoc(intendedLoc);
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

    public static void run() throws GameActionException {
        tryPickupFlag();
        Micro.run();
        runStrat();
        Micro.run();
    }
}