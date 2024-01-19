package v2s0;

import battlecode.common.*;
import static v2s0.Constants.rc;

public class Comms {

    public static void writeLoc(int index, MapLocation loc) throws GameActionException {
        rc.writeSharedArray(index, encodeLoc(loc));
    }

    public static MapLocation readLoc(int index) throws GameActionException {
        int encoded = rc.readSharedArray(index);
        if (encoded == 0) return null;

        return decodeLoc(encoded);
    }

    private static int encodeLoc(MapLocation loc) {
        return loc.x + loc.y * rc.getMapWidth() + 1;
    }

    private static MapLocation decodeLoc(int encodedLoc) {
        encodedLoc -= 1;
        int x = encodedLoc % rc.getMapWidth();
        int y = encodedLoc / rc.getMapWidth();
        return new MapLocation(x, y);
    }

    public static int read(int index) throws GameActionException {
        return rc.readSharedArray(index);
    }

    public static void write(int index, int data) throws GameActionException {
        rc.writeSharedArray(index, data);
    }
}