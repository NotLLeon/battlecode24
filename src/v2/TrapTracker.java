package v2;
import battlecode.common.*;

import static v2.Constants.*;

public class TrapTracker {

    private static int TRACK_RADIUS_SQUARED = 10;

    private static MapLocation[] nearbyTraps = new MapLocation[0];

    private static boolean inTrackRange(MapLocation m, MapLocation curLoc) throws GameActionException {
        return m.distanceSquaredTo(curLoc) <= TRACK_RADIUS_SQUARED;
    }

    public static MapLocation[] updateAndReturnTriggeredTraps() throws GameActionException {
        MapLocation curLoc = rc.getLocation();

        nearbyTraps = Utils.filterLocArr(nearbyTraps, (i) -> inTrackRange(i, curLoc));
        MapLocation[] triggeredTraps = Utils.filterLocArr(nearbyTraps, (m) -> rc.senseMapInfo(m).getTrapType() == TrapType.NONE);
        
        MapInfo[] localLocs = rc.senseNearbyMapInfos(TRACK_RADIUS_SQUARED);
        nearbyTraps = Utils.mapInfoToLocArr(localLocs, (i) -> (i.getMapLocation().distanceSquaredTo(curLoc) <= TRACK_RADIUS_SQUARED) && i.getTrapType() != TrapType.NONE);
        return triggeredTraps;
    }

}
