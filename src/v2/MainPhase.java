package v2;

import static v2.Constants.*;

import battlecode.common.*;
import v2.Constants.Role;

// MAIN PHASE STRATEGY HERE (TENTATIVE)
public class MainPhase {

    // TODO: should be based on map size
    private static final int LONG_TARGET_ROUND_INTERVAL = 100;
    private static final int SHORT_TARGET_ROUND_INTERVAL = 30;
    private static final int DISTRESS_HELP_DISTANCE_SQUARED_LO = 100;
    private static final int DISTRESS_HELP_DISTANCE_SQUARED_HI = 400;
    private static final int DISTRESS_DISTANCE_DIFF_THRESHOLD = 50;

    private static void onBroadcast() throws GameActionException {
        FlagRecorder.setApproxFlagLocs();
    }

    private static void checkDistressSignal() throws GameActionException {
        // TODO: Experiment with this, how far away to go help, when to check for help
        MapLocation curLoc = rc.getLocation();
        // get flag distress
        MapLocation flagDistressLoc = FlagDefense.readDistressLoc();
        int flagDistressDist = rc.getLocation().distanceSquaredTo(flagDistressLoc);

        // get signal distress
        MapLocation signalDistressLoc = SignalBot.getHighestPrioDistress(curLoc);
        int signalDistressDist = rc.getLocation().distanceSquaredTo(signalDistressLoc);

        // determine severity
        int flagSev = FlagDefense.readDistressLevel(flagDistressLoc);
        int signalSev = SignalBot.readSignalDistressLevel(signalDistressLoc);
        
        // compare
        MapLocation targetLoc = null;
        int targetSev = 0;
        int targetDist = 0;
        if (flagSev > signalSev) {
            targetSev = flagSev;
            targetLoc = flagDistressLoc;
            targetDist = flagDistressDist;
        } else if (flagSev < signalSev) {
            targetSev = signalSev;
            targetLoc = signalDistressLoc;
            targetDist = signalDistressDist;
        } else if (flagDistressDist < DISTRESS_DISTANCE_DIFF_THRESHOLD + signalDistressDist) {
            targetLoc = flagDistressLoc;
            targetSev = flagSev;
            targetDist = flagDistressDist;
        } else {
            targetLoc = signalDistressLoc;
            targetSev = signalSev;
            targetDist = signalDistressDist;
        }

        int threshold = DISTRESS_HELP_DISTANCE_SQUARED_LO;
        if (targetSev == 1) {
            threshold = DISTRESS_HELP_DISTANCE_SQUARED_HI;
        }
        
        if (targetDist < threshold) {
            if (targetDist < GameConstants.VISION_RADIUS_SQUARED &&
                    rc.senseNearbyFlags(-1, rc.getTeam()).length == 0) {
                FlagDefense.stopDistressLoc(targetLoc);
            } else Robot.moveTo(targetLoc);
        }
    }

    // should only run if Micro doesnt spot the flag
    private static void moveToRushLoc() throws GameActionException {

        int rushInd = getRushInd();
        MapLocation rushLoc = getRushLoc();

        if (rc.getLocation().isAdjacentTo(rushLoc)) {
            if (FlagRecorder.isPickedUp(rushInd)) changeRushInd();

            // TODO: only explore within some radius
            Explore.exploreNewArea();
        } else Robot.moveToAdjacent(rushLoc);
    }

    private static void changeRushInd() throws GameActionException {
        MapLocation curRushLoc = getRushLoc();

        int[] rushFlagInds = Utils.filterIntArr(FLAG_INDS, (Integer i) -> !FlagRecorder.isPickedUp(i));
        if (rushFlagInds.length == 0) return;

        int curInd = 0;
        for (int i = 0; i < rushFlagInds.length; ++i) {
            if (curRushLoc.equals(FlagRecorder.getFlagLoc(rushFlagInds[i]))) {
                curInd = i;
                break;
            }
        }

        Comms.write(COMMS_RUSH_IND, rushFlagInds[(curInd + 1) % rushFlagInds.length]);
        Comms.write(COMMS_RUSH_LAST_CHANGED, rc.getRoundNum());
    }

    private static void tryChangeRushInd() throws GameActionException {
        int lastUpdated = Comms.read(COMMS_RUSH_LAST_CHANGED);
        int curRound = rc.getRoundNum();
        int roundsPassed = (curRound - lastUpdated) - 1;
        if (roundsPassed > 0 && roundsPassed % LONG_TARGET_ROUND_INTERVAL == 0) {
            changeRushInd();
        }
    }

    private static int getRushInd() throws GameActionException {
        return Comms.read(COMMS_RUSH_IND);
    }

    public static MapLocation getRushLoc() throws GameActionException {
        return FlagRecorder.getFlagLoc(getRushInd());
    }

    public static void run() throws GameActionException {
        if (Robot.role == Role.SIGNAL) {
            SignalBot.run();
            return;
        }

        Micro.run();

        if ((rc.getRoundNum() - 1) % GameConstants.FLAG_BROADCAST_UPDATE_INTERVAL == 0) {
            onBroadcast();
        }

        FlagDefense.scanAndSignal();

        if (rc.hasFlag()){
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
            if(!rc.hasFlag()) FlagRecorder.setCaptured(flagId);
            else FlagRecorder.notifyCarryingFlag(flagId);

        } else {

            // check if any flags have been dropped and returned
            for (int i = 0; i < GameConstants.NUMBER_FLAGS; ++i) FlagRecorder.checkFlagReturned(i);

            checkDistressSignal();

            tryChangeRushInd();

            if (rc.isMovementReady()) moveToRushLoc();
        }

        FlagInfo[] visibleEnemyFlags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
        for (FlagInfo flag : visibleEnemyFlags) FlagRecorder.foundFlag(flag);

    }
}