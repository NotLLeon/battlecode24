package v2;

import battlecode.common.*;

import static v2.Constants.rc;
import v2.Constants.Role;

// SETUP PHASE LOGIC GOES HERE (TENTATIVE)

public class SetupPhase {

    public static boolean exploring = true;
    public static MapLocation setupLoc = null;

    // anything that happens only once at the beginning of the game
    public static void onSpawn() throws GameActionException {
        SignalBot.tryBecomeSignalBot();
        if (Robot.role == Role.SIGNAL) {
            Micro.setShouldMove(false);
        }
    }

    public static void run() throws GameActionException {
        if (Robot.role == Role.SIGNAL) return;
        
        // for now just explore, try to path to crumbs, then if dam found, gather around dam
        // to prepare attack
        MapLocation[] crumbLocs = rc.senseNearbyCrumbs(-1);
        if (crumbLocs.length > 0) {
            Robot.moveTo(crumbLocs[0]);
        } else if (exploring){
            MapInfo[] nearbyMap = rc.senseNearbyMapInfos();
            for (MapInfo info : nearbyMap) {
                if (info.isDam()) {
                    exploring = false;
                    setupLoc = info.getMapLocation();
                    Robot.moveTo(setupLoc);
                    break;
                }
            }
            if (exploring) Explore.exploreNewArea();
        } else {
            Robot.moveTo(setupLoc);
        }
    }
}
