package v2;

// class to keep track of:
// - if a flag is captured
// - if a flag is picked up
// - if the exact loc of a flag is known
// for each enemy

// we use index in the FlagRecorder as a unique identifier for each flag, since the ID is not always known

import static v2.Constants.*;

import battlecode.common.*;

public class FlagRecorder {
    // change this to reflect number of INDs below (must be <6)
    private static final int NUM_BITS_PER_FLAG = 3;
    private static final int IND_CAPTURED = 0;
    private static final int IND_PICKED_UP = 1;
    private static final int IND_EXACT_LOC = 2;

    private static int getFlagId(int ind) throws GameActionException {
        return Comms.read(COMMS_ENEMY_FLAG_IDS_START_IND + ind);
    }

    private static int getEncodedFlagRecorder() throws GameActionException {
        return Comms.read(COMMS_FLAG_RECORDER);
    }

    private static int getFlagIdInd(int id) throws GameActionException {
        for (int i = 0; i < GameConstants.NUMBER_FLAGS; ++i) {
            if (getFlagId(i) == id) return i;
        }
        return -1;
    }

    private static int getFlagLocInd(MapLocation loc) throws GameActionException {
        for (int i = 0; i < GameConstants.NUMBER_FLAGS; ++i) {
            MapLocation flagLoc = Comms.readLoc(COMMS_ENEMY_FLAG_LOCS_START_IND + i);
            if (flagLoc != null && flagLoc.equals(loc)) {
                return i;
            }
        }
        return -1;
    }

    private static int getMask(int ind, int bitInd) {
        return 1 << (NUM_BITS_PER_FLAG * ind + bitInd);
    }

    private static boolean isIndBitSet(int ind, int bitInd) throws GameActionException {
        int encoded = getEncodedFlagRecorder();
        int bit = encoded & getMask(ind, bitInd);
        return bit > 0;
    }

    private static int getClosestApproxLoc(MapLocation loc) throws GameActionException {
        int bestInd = -1;
        MapLocation bestLoc = null;
        for (int i = 0; i < GameConstants.NUMBER_FLAGS; ++i) {
            if (isExactLoc(i)) continue;
            MapLocation approxFlagLoc = Comms.readLoc(COMMS_ENEMY_FLAG_LOCS_START_IND + i);
            if (approxFlagLoc == null) continue;
            if (bestLoc == null || loc.distanceSquaredTo(approxFlagLoc) < loc.distanceSquaredTo(bestLoc)) {
                bestInd = i;
                bestLoc = approxFlagLoc;
            }
        }
        return bestInd;
    }

    private static void foundIndExactLoc(int ind, FlagInfo flag) throws GameActionException {
        int newEncoded = getEncodedFlagRecorder() | getMask(ind, IND_EXACT_LOC);
        Comms.write(COMMS_FLAG_RECORDER, newEncoded);
        Comms.writeLoc(COMMS_ENEMY_FLAG_LOCS_START_IND + ind, flag.getLocation());
        Comms.write(COMMS_ENEMY_FLAG_IDS_START_IND + ind, flag.getID());
    }

    public static boolean isCaptured(int ind) throws GameActionException {
        return isIndBitSet(ind, IND_CAPTURED);
    }

    public static boolean isPickedUp(int ind) throws GameActionException {
        return isIndBitSet(ind, IND_PICKED_UP);
    }

    public static boolean isExactLoc(int ind) throws GameActionException {
        return isIndBitSet(ind, IND_EXACT_LOC);
    }

    public static void setCaptured(int id) throws GameActionException {
        int newEncoded = getEncodedFlagRecorder() | getMask(getFlagIdInd(id), IND_CAPTURED);
        Comms.write(COMMS_FLAG_RECORDER, newEncoded);
    }

    public static void setPickedUp(int id) throws GameActionException {
        int newEncoded = getEncodedFlagRecorder() | getMask(getFlagIdInd(id), IND_PICKED_UP);
        Comms.write(COMMS_FLAG_RECORDER, newEncoded);
    }

    public static void unSetPickedUp(int ind) throws GameActionException {
        if (!isPickedUp(ind)) return;
        int newEncoded = getEncodedFlagRecorder() ^ getMask(ind, IND_PICKED_UP);
        Comms.write(COMMS_FLAG_RECORDER, newEncoded);
    }

    public static void foundFlag(FlagInfo flag) throws GameActionException {
        if (flag.isPickedUp()) return;

        // check if this is a duplicate report or dropped flag
        int flagID = flag.getID();
        int existingInd = getFlagIdInd(flagID);
        if (existingInd != -1) return;

        MapLocation flagLoc = flag.getLocation();
        int ind = getClosestApproxLoc(flagLoc);
        if (ind == -1) return; // wtf
        foundIndExactLoc(ind, flag);
    }

    /***
     * SHOULD ONLY BE CALLED IMMEDIATELY AFTER SETUP
     * @throws GameActionException
     */
    public static void setApproxFlagLocs() throws GameActionException {
        if (Comms.readLoc(COMMS_ENEMY_FLAG_LOCS_START_IND) != null) return;
        MapLocation[] approxFlagLocs = rc.senseBroadcastFlagLocations();
        int ind = 0;
        for (; ind < approxFlagLocs.length; ++ind) {
            Comms.writeLoc(COMMS_ENEMY_FLAG_LOCS_START_IND + ind, approxFlagLocs[ind]);
        }

        if (approxFlagLocs.length == GameConstants.NUMBER_FLAGS) return;

        // in case the calling robot can see any enemy flags
        FlagInfo[] exactFlagLocs = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
        for (FlagInfo flag : exactFlagLocs) {
            foundIndExactLoc(ind, flag);
            ++ind;
        }
    }

    public static MapLocation getFlagLoc(int ind) throws GameActionException {
        return Comms.readLoc(ind);
    }

    public static void notifyCarryingFlag(int id) throws GameActionException {
        int flagInd = getFlagIdInd(id);
        Comms.write(COMMS_ENEMY_FLAG_LAST_ROUND_CARRYING_START_IND + flagInd, rc.getRoundNum());
    }

    public static void checkFlagReturned(int ind) throws GameActionException {
        if (isCaptured(ind) || !isPickedUp(ind)) return;
        int lastRoundCarrying = Comms.read(COMMS_ENEMY_FLAG_LAST_ROUND_CARRYING_START_IND + ind);
        if (lastRoundCarrying < rc.getRoundNum() - GameConstants.FLAG_DROPPED_RESET_ROUNDS - 1) {

            unSetPickedUp(ind);
        }
    }

}
