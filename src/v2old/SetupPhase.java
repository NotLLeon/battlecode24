package v2old;

import battlecode.common.*;
import v2old.Constants.Role;

import static v2old.Constants.rc;

// SETUP PHASE LOGIC GOES HERE (TENTATIVE)

public class SetupPhase extends Robot {

    public static boolean exploring = true;
    public static MapLocation setupLoc = null;

    public static void run() throws GameActionException {
        if (RobotPlayer.role == Role.SIGNAL) {
            SignalBot.run();
            return;
        }
        SetupCommunication.init();
        // for now just explore, try to path to crumbs, then if dam found, gather around dam
        // to prepare attack
        SetupCommunication.setupMeetup();

        MapLocation[] crumbLocs = rc.senseNearbyCrumbs(-1);
        if (SetupCommunication.hasMeetup() && rc.getRoundNum() > 100) {
            MapInfo[] adjMap = rc.senseNearbyMapInfos(2);

            MapInfo[] damTiles = Utils.filterMapInfoArr(adjMap, (i) -> i.isDam());
            boolean stopMoving = damTiles.length > 0;

            if (!stopMoving) moveTo(SetupCommunication.meetLocation);
            else {
                MapLocation[] damMapLocs = Utils.mapInfoToLocArr(damTiles);

                if (!rc.isActionReady()) return; 
                for (MapInfo info : adjMap) {
                    MapLocation adjMapLoc = info.getMapLocation();
                    int distSqNearest = adjMapLoc.distanceSquaredTo(damMapLocs[0]);

                    for (int i = 1; i < damMapLocs.length; ++i) {
                        distSqNearest = Math.min(adjMapLoc.distanceSquaredTo(damMapLocs[i]), distSqNearest);
                    }

                    if (info.isWater() && distSqNearest <= 2 && rc.canFill(adjMapLoc)) {
                        Robot.fill(adjMapLoc);
                        return;
                    }
                }
            }
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
