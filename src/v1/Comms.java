package v1;

import battlecode.common.*;
import static v1.Constants.rc;

public class Comms {

   // 0-3: CENTER OF FRIENDLY SPAWN LOCS

    private static int encodeLoc(MapLocation loc) {
        return loc.x + loc.y * rc.getMapWidth() + 1;
    }

    private static MapLocation decodeLoc(int encodedLoc) {
        encodedLoc -= 1;
        int x = encodedLoc % rc.getMapWidth();
        int y = encodedLoc / rc.getMapWidth();
        return new MapLocation(x, y);
    }
}