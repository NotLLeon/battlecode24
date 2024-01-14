package v1;

import battlecode.common.*;

import static v1.Constants.rc;
import static v1.Constants.COMMS_FLAG_DISTRESS_FLAGS;
import static v1.Constants.COMMS_FLAG_DISTRESS_LOCS;
import v1.Constants.Role;

public class FlagDefense extends Robot {

    private static int getFlagId(int ind) throws GameActionException {
        return Comms.read(COMMS_FLAG_DISTRESS_FLAGS + ind);
    }

    private static int getFlagIdInd(int id) throws GameActionException {
        for (int i = 0; i < GameConstants.NUMBER_FLAGS; ++i) {
            if (getFlagId(i) == id || getFlagId(i) == 0) {
                return i;
            }
        }
        return -1;
    }

    public static int getFlagIdFromLoc(MapLocation loc) throws GameActionException {
        for (int i = 0; i < GameConstants.NUMBER_FLAGS; ++i) {
            if (Comms.readLoc(COMMS_FLAG_DISTRESS_LOCS + i).equals(loc)) {
                return i;
            }
        }
        return -1;
    }

    public static void setDistress(MapLocation loc, int id) throws GameActionException {
        int ind = getFlagIdInd(id);
        Comms.write(COMMS_FLAG_DISTRESS_FLAGS + ind, id);
        Comms.writeLoc(COMMS_FLAG_DISTRESS_LOCS + ind, loc);
    }

    public static void stopDistress(int id) throws GameActionException {
        int ind = getFlagIdInd(id);
        Comms.write(COMMS_FLAG_DISTRESS_FLAGS + ind, 0);
    }

    public static MapLocation readDistress() throws GameActionException {
        MapLocation nearestLoc = null;
        MapLocation curLoc = rc.getLocation();
        for (int i = 0; i < GameConstants.NUMBER_FLAGS; ++i) {
            int distressFlag = Comms.read(COMMS_FLAG_DISTRESS_FLAGS + i);
            if (distressFlag > 0) {
                MapLocation distressLoc = Comms.readLoc(COMMS_FLAG_DISTRESS_LOCS + i);
                if (nearestLoc == null || curLoc.distanceSquaredTo(nearestLoc) > curLoc.distanceSquaredTo(distressLoc)) {
                    nearestLoc = distressLoc;
                }
            }
        }
        return nearestLoc;
    }
    
    public static void scanAndSignal() throws GameActionException {
        FlagInfo[] nearbyFlags = rc.senseNearbyFlags(-1, rc.getTeam());
        for (int i = 0; i < nearbyFlags.length; ++i) {
            if (nearbyFlags[i].isPickedUp()) {
                setDistress(nearbyFlags[i].getLocation(), nearbyFlags[i].getID());
            } else if (rc.senseNearbyRobots(-1, rc.getTeam().opponent()).length == 0) {
                stopDistress(nearbyFlags[i].getID());
            }
        }
    }
}
