package v2;

import battlecode.common.*;

import static v2.Constants.rc;
import v2.Constants.Role;
import static v2.RobotPlayer.role;
import static v2.RobotPlayer.spawnLoc;

// SETUP PHASE LOGIC GOES HERE (TENTATIVE)

public class SetupPhase extends Robot {

    public static boolean exploring = true;
    public static MapLocation setupLoc = null;

    public static void run() throws GameActionException {
        // If signal bot, then signal
        if (role == Role.SIGNAL) {
            FlagDefense.scanAndSignal();
            return;
        }

        // try to become signal bot, wont happen if signal bot already exists on flag
        FlagInfo[] flags = rc.senseNearbyFlags(2);
        if (flags.length > 0 && !rc.canSenseRobotAtLocation(flags[0].getLocation())) {
            rc.setIndicatorString("BECOMING SIGNAL");
            moveTo(flags[0].getLocation());
            if (flags[0].getLocation().equals(rc.getLocation())) {
                role = Role.SIGNAL;
                rc.setIndicatorString("SIGNAL");
                spawnLoc = rc.getLocation();
                return;
            }
        }
        // for now just explore, try to path to crumbs, then if dam found, gather around dam
        // to prepare attack
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
