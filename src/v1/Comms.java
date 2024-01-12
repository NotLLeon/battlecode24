package v1;

import battlecode.common.*;

import java.util.HashMap;
import java.util.HashSet;

import static v1.Constants.rc;

public class Comms {

    private static int encodeLoc(MapLocation loc) {
        return loc.x + loc.y * rc.getMapWidth() + 1;
    }

    private static MapLocation decodeLoc(int encodedLoc) {
        encodedLoc -= 1;
        int x = encodedLoc % rc.getMapWidth();
        int y = encodedLoc / rc.getMapWidth();
        return new MapLocation(x, y);
    }

    public static void setDistress(MapLocation loc) throws GameActionException {
        rc.writeSharedArray(3, encodeLoc(loc));
    }

    public static void stopDistress() throws GameActionException {
        rc.writeSharedArray(3, 0);
    }

    public static MapLocation readDistress() throws GameActionException {
        int distressLoc = rc.readSharedArray(3);
        if (distressLoc > 0) {
            return decodeLoc(distressLoc);
        } else {
            return null; // is this how you do it in java
        }
    }
}