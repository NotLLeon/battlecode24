package v2old;

import battlecode.common.*;

import static v2old.Constants.rc;

// SETUP PHASE LOGIC GOES HERE (TENTATIVE)

public class SetupPhase extends Robot {

    public static boolean exploring = true;
    public static MapLocation setupLoc = null;

    public static void run() throws GameActionException {
        SetupCommunication.init();
        // for now just explore, try to path to crumbs, then if dam found, gather around dam
        // to prepare attack
        SetupCommunication.setupMeetup();

        MapLocation[] crumbLocs = rc.senseNearbyCrumbs(-1);
        if (SetupCommunication.hasMeetup() && rc.getRoundNum() > 100) {
            MapInfo[] adjMap = rc.senseNearbyMapInfos(2);
            boolean stopMoving = false;
            for (MapInfo info : adjMap) {
                if (info.isDam()) {
                    stopMoving = true;
                }
            }
            if (!stopMoving) moveTo(SetupCommunication.meetLocation);
        } else if (crumbLocs.length > 0) {
            moveTo(crumbLocs[0]);
        } else {
            MapInfo[] nearbyMap = rc.senseNearbyMapInfos();
            for (MapInfo info : nearbyMap) {
                if (info.isDam()) {
                    SetupCommunication.writeMeetupLoc(info.getMapLocation());
                    moveTo(SetupCommunication.meetLocation);
                    break;
                }
            }
            if (exploring) Explore.exploreNewArea();
        }
    }
}
