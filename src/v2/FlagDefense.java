package v2;

import battlecode.common.*;

import static v2.Constants.*;

public class FlagDefense {

    // if you are within this distance of a distress flag and there are no enemies, stop signal
    static final int FLAG_SAFE_DISTANCE_SQUARED = 4;

    private static int getFlagId(int ind) throws GameActionException {
        return Comms.read(COMMS_FLAG_DISTRESS_FLAGS + ind);
    }

    private static int getFlagIndFromId(int id) throws GameActionException {
        // first try to see if it exists
        for (int i = 0; i < GameConstants.NUMBER_FLAGS; ++i) {
            if (getFlagId(i) == id) {
                return i;
            }
        }

        // then look for next open slot, since we're adding new flag
        for (int i = 0; i < GameConstants.NUMBER_FLAGS; ++i) {
            if (getFlagId(i) == 0) {
                return i;
            }
        }
        return -1;
    }

    private static int getFlagIndFromLoc(MapLocation loc) throws GameActionException {
        for (int i = 0; i < GameConstants.NUMBER_FLAGS; ++i) {
            if (loc.equals(Comms.readLoc(COMMS_FLAG_DISTRESS_LOCS + i))) {
                return i;
            }
        }
        return -1;
    }

    public static void setDistress(MapLocation loc, int id) throws GameActionException {
        int ind = getFlagIndFromId(id);
        if (ind == -1) return;
        Comms.write(COMMS_FLAG_DISTRESS_FLAGS + ind, id);
        Comms.writeLoc(COMMS_FLAG_DISTRESS_LOCS + ind, loc);
    }

    private static void stopDistressInd(int ind) throws GameActionException {
        if (ind == -1) return;
        Comms.write(COMMS_FLAG_DISTRESS_FLAGS + ind, 0);
        Comms.write(COMMS_FLAG_DISTRESS_LOCS + ind, 0);
    }

    public static void stopDistress(int id) throws GameActionException {
        stopDistressInd(getFlagIndFromId(id));
    }

    public static void stopDistressLoc(MapLocation loc) throws GameActionException {
        stopDistressInd(getFlagIndFromLoc(loc));
    }

    public static MapLocation readDistress() throws GameActionException {
        MapLocation nearestLoc = null;
        MapLocation curLoc = rc.getLocation();
        for (int i = 0; i < GameConstants.NUMBER_FLAGS; ++i) {
            int distressFlag = Comms.read(COMMS_FLAG_DISTRESS_FLAGS + i);
            if (distressFlag > 0) {
                MapLocation distressLoc = Comms.readLoc(COMMS_FLAG_DISTRESS_LOCS + i);
                if (distressLoc == null) continue;
                if (nearestLoc == null || curLoc.distanceSquaredTo(nearestLoc) > curLoc.distanceSquaredTo(distressLoc)) {
                    nearestLoc = distressLoc;
                }
            }
        }
        return nearestLoc;
    }
    
    public static void scanAndSignal() throws GameActionException {
        FlagInfo[] nearbyFlags = rc.senseNearbyFlags(-1, rc.getTeam());
        for (FlagInfo nearbyFlag : nearbyFlags) {
            MapLocation curLoc = rc.getLocation();
            if (nearbyFlag.isPickedUp()) {
                setDistress(nearbyFlag.getLocation(), nearbyFlag.getID());
            } else if (curLoc.isWithinDistanceSquared(nearbyFlag.getLocation(), FLAG_SAFE_DISTANCE_SQUARED) &&
                    rc.senseNearbyRobots(-1, rc.getTeam().opponent()).length == 0) {
                stopDistress(nearbyFlag.getID());
            }
        }
    }

    public static MapLocation[] getAllDistressLocs() throws GameActionException {
        int[] distressInds = Utils.filterIntArr(FLAG_INDS, (i) -> Comms.read(COMMS_FLAG_DISTRESS_FLAGS + i) > 0);
        MapLocation[] distressLocs = new MapLocation[distressInds.length];
        for (int i = 0; i < distressInds.length; ++i) {
            distressLocs[i] = Comms.readLoc(COMMS_FLAG_DISTRESS_LOCS + distressInds[i]);
        }
        return distressLocs;
    }
}
