package v1;

// class to keep track of:
// - if a flag is captured
// - if a flag is picked up
// - if the exact loc of a flag is known
// for each enemy

import static v1.Constants.*;

import battlecode.common.*;

public class FlagRecorder {
    private static final int IND_CAPTURED = 0;
    private static final int IND_PICKED_UP = 1;
    private static final int IND_EXACT_LOC = 2;

    private static int[] flagIds = new int[3];

    private static int getEncodedFlagRecorder() throws GameActionException {
        return Comms.read(COMMS_FLAG_RECORDER);
    }

    private static int getFlagIdInd(int id) {
        for (int i = 0; i < 3; ++i) {
            if (flagIds[i] == id) {
                return i;
            }
        }
        return -1;
    }

    private static int getFlagLocInd(MapLocation loc) throws GameActionException {
        for (int i = 0; i < 3; ++i) {
            MapLocation flagLoc = Comms.readLoc(COMMS_ENEMY_FLAGS_START_IND + i);
            if (flagLoc != null && flagLoc.equals(loc)) {
                return i;
            }
        }
        return -1;
    }

    private static int getMask(int ind, int bitInd) {
        return 1 << (3 * ind + bitInd);
    }

    private static boolean isIndBitSet(int ind, int bitInd) throws GameActionException {
        int encoded = getEncodedFlagRecorder();
        int bit = encoded & getMask(ind, bitInd);
        return bit == 1;
    }

    public static boolean isCaptured(MapLocation loc) throws GameActionException {
        return isIndBitSet(getFlagLocInd(loc), IND_CAPTURED);
    }

    public static boolean isPickedUp(MapLocation loc) throws GameActionException {
        return isIndBitSet(getFlagLocInd(loc), IND_PICKED_UP);
    }

    public static boolean isExactLoc(MapLocation loc) throws GameActionException {
        return isIndBitSet(getFlagLocInd(loc), IND_EXACT_LOC);
    }

    public static boolean isExactLoc(int ind) throws GameActionException {
        return isIndBitSet(ind, IND_EXACT_LOC);
    }

    public static void setCaptured(int id) throws GameActionException {
        int newEncoded = getEncodedFlagRecorder() & getMask(getFlagIdInd(id), IND_CAPTURED);
        Comms.write(COMMS_FLAG_RECORDER, newEncoded);
    }

    public static void setPickedUp(int id) throws GameActionException {
        int newEncoded = getEncodedFlagRecorder() & getMask(getFlagIdInd(id), IND_PICKED_UP);
        Comms.write(COMMS_FLAG_RECORDER, newEncoded);
    }

    /***
     * Call when the exact location of a flag is found
     * @param ind index of flag in broadcasted arr
     * @param id flag id
     * @throws GameActionException
     */
    public static void setExactLoc(int ind, int id) throws GameActionException {
        int newEncoded = getEncodedFlagRecorder() & getMask(getFlagIdInd(id), IND_EXACT_LOC);
        Comms.write(COMMS_FLAG_RECORDER, newEncoded);

        flagIds[ind] = id;
    }
}
