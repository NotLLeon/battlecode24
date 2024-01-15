package v2;

import battlecode.common.*;

import static v2.Constants.*;
public class FlagDefense {

    // if you are within this distance of a distress flag and there are no enemies, stop signal
    static final int FLAG_SAFE_DISTANCE_SQUARED = 4;
    static final float ENEMY_DISTRESS_RATIO = 5/3;

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

    private static int getFlagIndFromLoc(MapLocation loc) throws GameActionException {
        for (int i = 0; i < GameConstants.NUMBER_FLAGS; ++i) {
            if (loc.equals(Comms.readLoc(COMMS_FLAG_DISTRESS_LOCS + i))) {
                return i;
            }
        }
        return -1;
    }

    public static void setDistress(MapLocation loc, int id) throws GameActionException {
        int ind = getFlagIdInd(id);
        if (ind == -1) return;
        Comms.write(COMMS_FLAG_DISTRESS_FLAGS + ind, id);
        Comms.writeLoc(COMMS_FLAG_DISTRESS_LOCS + ind, loc);
    }

    public static void stopDistress(int id) throws GameActionException {
        int ind = getFlagIdInd(id);
        if (ind == -1) return;
        Comms.write(COMMS_FLAG_DISTRESS_FLAGS + ind, 0);
        Comms.write(COMMS_FLAG_DISTRESS_LOCS + ind, 0);
    }

    public static void stopDistressLoc(MapLocation loc) throws GameActionException {
        int ind = getFlagIndFromLoc(loc);
        stopDistress(ind);
    }

    public static MapLocation readDistressLoc() throws GameActionException {
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
}
