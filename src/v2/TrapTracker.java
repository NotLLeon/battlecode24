package v2;
import battlecode.common.*;
import v2.fast.FastIntIntMap;

import static v2.Constants.*;

public class TrapTracker {

    // try using max vision radius?
    private static int TRACK_RADIUS_SQUARED = 10;
    private static MapInfo[] nearbyTraps = new MapInfo[0];
    private static FastIntIntMap stunnedEnemies = new FastIntIntMap();
    private static final int STUNNED_ENEMIES_MAP_MAX_SIZE = 10;

    private static boolean inTrackRange(MapLocation m) {
        return rc.getLocation().isWithinDistanceSquared(m, TRACK_RADIUS_SQUARED);
    }

    public static void senseTriggeredTraps() throws GameActionException {
        MapInfo[] triggeredTraps = Utils.filterMapInfoArr(nearbyTraps,
                (i) -> inTrackRange(i.getMapLocation()) && i.getTrapType() == TrapType.NONE);

        for (MapLocation trap : Utils.mapInfoToLocArr(triggeredTraps)) {
            // FIXME: very expensive
            RobotInfo[] stunned = rc.senseNearbyRobots(trap, 2, rc.getTeam().opponent());
            for (RobotInfo enemy : stunned) {
                int id = enemy.getID();
                stunnedEnemies.remove(id);
                stunnedEnemies.add(id, rc.getRoundNum());
            }
        }

//        if (stunnedEnemies.size > STUNNED_ENEMIES_MAP_MAX_SIZE) {
//            FastIntIntMap temp = new FastIntIntMap();
//            int curRound = rc.getRoundNum();
//            for (int id : stunnedEnemies.getKeys()) {
//                int lastStunned = stunnedEnemies.getVal(id);
//                if (curRound - lastStunned < 5) temp.add(id, stunnedEnemies.getVal(id));
//            }
//            stunnedEnemies = temp;
//         }
    }

    public static void sensePlacedTraps() throws GameActionException {
        MapInfo[] localLocs = rc.senseNearbyMapInfos(TRACK_RADIUS_SQUARED);
        nearbyTraps = Utils.filterMapInfoArr(localLocs, (i) -> i.getTrapType() == TrapType.STUN);
    }

    public static int getLastStunnedRound(int id) {
        return stunnedEnemies.getVal(id);
    }

}
