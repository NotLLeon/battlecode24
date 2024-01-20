package v2;
import battlecode.common.*;

import static v2.Constants.*;

public class TrapTracker {

    private static int TRACK_RADIUS_SQUARED = 10;

    private static MapInfo[] nearbyTraps = new MapInfo[0];

    private static boolean inTrackRange(MapLocation m) throws GameActionException {
        return rc.getLocation().isWithinDistanceSquared(m, TRACK_RADIUS_SQUARED);
    }

    public static void updateTraps() throws GameActionException {
        MapInfo[] localLocs = rc.senseNearbyMapInfos(TRACK_RADIUS_SQUARED);
        nearbyTraps = Utils.filterMapInfoArr(localLocs, (i) -> i.getTrapType() != TrapType.NONE);
    }

    public static MapLocation[] getTriggeredTraps() throws GameActionException {
        MapInfo[] triggeredTraps = Utils.filterMapInfoArr(nearbyTraps,
                (i) -> inTrackRange(i.getMapLocation()) && i.getTrapType() == TrapType.NONE);
            
        // MapInfo[] testTraps = Utils.filterMapInfoArr(nearbyTraps, (i) -> inTrackRange(i.getMapLocation()));
        // System.out.println(nearbyTraps.length);
        // System.out.println(testTraps.length);
        // System.out.println(triggeredTraps.length);
        return Utils.mapInfoToLocArr(triggeredTraps);
    }

}
