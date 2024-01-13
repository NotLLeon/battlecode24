package v1old;

import battlecode.common.*;

import static v1old.Constants.rc;

// SETUP PHASE LOGIC GOES HERE (TENTATIVE)

public class SetupPhase extends Robot {

    public static boolean exploring = true;
    public static MapLocation setupLoc = null;

    public static void run() throws GameActionException {
        // for now just explore, naturally gather crumbs
        MapLocation[] crumbLocs = rc.senseNearbyCrumbs(-1);
        if (crumbLocs.length > 0) {
            moveTo(crumbLocs[0]);
        } else if (exploring){
            MapInfo[] nearbyMap = rc.senseNearbyMapInfos();
            for (MapInfo info : nearbyMap) {
                if (info.isDam()) {
                    exploring = false;
                    setupLoc = info.getMapLocation();
                    moveTo(setupLoc);
                    break;
                }
            }
            if (exploring) Explore.exploreNewArea();
        } else {
            moveTo(setupLoc);
        }
    }
}
