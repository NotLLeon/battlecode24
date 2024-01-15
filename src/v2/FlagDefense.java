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

    public static void setDistress(MapLocation loc, int id, int severity) throws GameActionException {
        int ind = getFlagIndFromId(id);
        if (ind == -1) return;
        Comms.write(COMMS_FLAG_DISTRESS_FLAGS + ind, id);
        Comms.writeLoc(COMMS_FLAG_DISTRESS_LOCS + ind, loc);
        Comms.write(COMMS_FLAG_DISTRESS_LEVEL + ind, severity);
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

    public static MapLocation readDistressLoc() throws GameActionException {
        MapLocation nearestLoc = null;
        int nearestSev = 0;
        MapLocation curLoc = rc.getLocation();
        for (int i = 0; i < GameConstants.NUMBER_FLAGS; ++i) {
            int distressFlag = Comms.read(COMMS_FLAG_DISTRESS_FLAGS + i);
            if (distressFlag > 0) {
                MapLocation distressLoc = Comms.readLoc(COMMS_FLAG_DISTRESS_LOCS + i);
                int sev = Comms.read(COMMS_FLAG_DISTRESS_LEVEL + i);
                if (distressLoc == null) continue;
                else if (nearestLoc == null 
                    || (curLoc.distanceSquaredTo(nearestLoc) > curLoc.distanceSquaredTo(distressLoc) && sev >= nearestSev)) {
                    nearestLoc = distressLoc;
                    nearestSev = sev;
                } else if (sev > nearestSev) {
                    nearestLoc = distressLoc;
                    nearestSev = sev;
                }
            }
        }
        return nearestLoc;
    }

    public static int readDistressLevel(MapLocation loc) throws GameActionException {
        int ind = getFlagIndFromLoc(loc);
        int level = Comms.read(COMMS_FLAG_DISTRESS_LEVEL + ind);
        return level;
    }
    
    public static void scanAndSignal() throws GameActionException {
        FlagInfo[] nearbyFlags = rc.senseNearbyFlags(-1, rc.getTeam());
        for (FlagInfo nearbyFlag : nearbyFlags) {
            MapLocation curLoc = rc.getLocation();
            if (nearbyFlag.isPickedUp()) {
                // set distress and severity based on ratio of nearby enemies and friendlies
                int severity = 0;
                RobotInfo[] nearbyBots = rc.senseNearbyRobots();
                int teammates = 0;

                for (RobotInfo bot : nearbyBots) {
                    if (bot.getTeam() == rc.getTeam()) teammates++;
                }

                if ((nearbyBots.length - teammates)/(float)teammates > ENEMY_DISTRESS_RATIO) {
                    severity = 1;
                }
                
                setDistress(nearbyFlag.getLocation(), nearbyFlag.getID(), severity);
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
