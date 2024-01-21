package v2;
import battlecode.common.*;
import v2.fast.FastIntIntMap;

import static v2.Constants.*;

public class TrapTracker {

    // try using max vision radius?
    private static int TRACK_RADIUS_SQUARED = 10;
    private static MapInfo[] nearbyTraps = new MapInfo[0];
    private static final FastIntIntMap stunnedEnemies = new FastIntIntMap();

    private static boolean inTrackRange(MapLocation m) {
        return rc.getLocation().isWithinDistanceSquared(m, TRACK_RADIUS_SQUARED);
    }

    public static void senseTriggeredTraps() throws GameActionException {
        MapInfo[] triggeredTraps = Utils.filterMapInfoArr(nearbyTraps,
            (i) -> {
                MapLocation loc = i.getMapLocation();
                return inTrackRange(loc) && rc.senseMapInfo(loc).getTrapType() == TrapType.NONE;
            }
        );
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        MapLocation[] triggeredLocs = Utils.mapInfoToLocArr(triggeredTraps);
        for (RobotInfo enemy : enemyRobots) {
            for (MapLocation trap : triggeredLocs) {
                if (enemy.getLocation().isWithinDistanceSquared(trap, TrapType.STUN.enterRadius)) {
                    int id = enemy.getID();
                    stunnedEnemies.remove(id);
                    stunnedEnemies.add(id, rc.getRoundNum());
                    break;
                }
            }
        }
    }

    public static void sensePlacedTraps() throws GameActionException {
        MapInfo[] localLocs = rc.senseNearbyMapInfos(TRACK_RADIUS_SQUARED);
        nearbyTraps = Utils.filterMapInfoArr(localLocs, (i) -> i.getTrapType() == TrapType.STUN);
    }

    public static int getLastStunnedRound(int id) {
        return stunnedEnemies.getVal(id);
    }

}
