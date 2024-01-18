package v2;

import battlecode.common.*;
import v2.fast.FastLocIntMap;

import static v2.Constants.rc;

// SETUP PHASE LOGIC GOES HERE (TENTATIVE)

public class SetupPhase extends Robot {

    public static boolean exploring = true;
    public static MapLocation setupLoc = null;
    private static int roundsWaiting = 0;

    public static void run() throws GameActionException {
        // for now just explore, try to path to crumbs, then if dam found, gather around dam
        // to prepare attack
        MapLocation[] crumbLocs = rc.senseNearbyCrumbs(-1);

        // build traps if possible
        // placeDefensiveTraps();

        if (crumbLocs.length > 0 && rc.getRoundNum() < 150) {
            moveTo(crumbLocs[0]);
        } else if (exploring){
            MapInfo[] nearbyMap = rc.senseNearbyMapInfos();
            FastLocIntMap damMap = new FastLocIntMap();
            
            for (MapInfo info : nearbyMap) {
                if (info.isDam()) {
                    damMap.add(info.getMapLocation(), 1);
                }
            }

            if (roundsWaiting > 0 && rc.getRoundNum() < 150) {
                roundsWaiting--;
                setupMeetLocation();
            }
            else {
                MapLocation[] dams = damMap.getKeys();
                if (dams.length > 0) {
                    setupLoc = dams[Random.nextInt(dams.length)];
                    exploring = false;
                    moveTo(setupLoc);
                }
            }
            if (exploring) Explore.exploreNewArea();
        } else {
            MapInfo[] adjacent = rc.senseNearbyMapInfos(2);
            // not adjacent yet to dam tile
            if (rc.getLocation().distanceSquaredTo(setupLoc) > 2) {
                setupMeetLocation();
                moveToAdjacent(setupLoc);

                roundsWaiting++;
                if (roundsWaiting >= 4) {
                    exploring = true;
                }
            } else {
                moveToAdjacent(setupLoc);
                for (MapInfo info : adjacent) {
                    if (info.isWater() && rc.canFill(info.getMapLocation())) {
                        rc.fill(info.getMapLocation());
                    }
                }
            }
        }
    }

    private static boolean setupMeetLocation() throws GameActionException {
        MapInfo[] adjacent = rc.senseNearbyMapInfos(2);
        for (MapInfo info : adjacent) {
            if (info.isDam()) {
                setupLoc = info.getMapLocation();
                roundsWaiting = 0;
                exploring = false;
                return true;
            }
        }
        return false;
    }
}
