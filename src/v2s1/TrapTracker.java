package v2s1;
import battlecode.common.*;
import v2s1.fast.FastIntIntMap;

import static v2s1.Constants.*;

public class TrapTracker {

    // TODO: tune these
    // we store all stun traps in this radius
    private static int TRAP_TRACK_RADIUS_SQUARED = 10;
    // we check for stunned enemies in this radius
    private static int STUN_TRACK_RADIUS_SQUARED = GameConstants.VISION_RADIUS_SQUARED;

    private static MapInfo[] nearbyTraps = new MapInfo[0];
    private static FastIntIntMap stunnedEnemies = new FastIntIntMap();
//    private static final int STUNNED_ENEMIES_MAX_SIZE = 10;
//    private static final int STUN_ROUNDS = 5;

    private static boolean inTrackRange(MapLocation m) {
        return rc.getLocation().isWithinDistanceSquared(m, TRAP_TRACK_RADIUS_SQUARED);
    }

    // should be called at beginning of turn
    public static void senseTriggeredTraps() throws GameActionException {
        MapInfo[] triggeredTraps = Utils.filterMapInfoArr(nearbyTraps,
            (i) -> {
                MapLocation loc = i.getMapLocation();
                return inTrackRange(loc) && rc.senseMapInfo(loc).getTrapType() == TrapType.NONE;
            }
        );
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(STUN_TRACK_RADIUS_SQUARED, rc.getTeam().opponent());
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
//        if (stunnedEnemies.size > STUNNED_ENEMIES_MAX_SIZE) {
//            FastIntIntMap newStunnedEnemies = new FastIntIntMap();
//            int curRound = rc.getRoundNum();
//            for (int id : stunnedEnemies.getKeys()) {
//                int round = stunnedEnemies.getVal(id);
//                if (curRound - round <= STUN_ROUNDS) newStunnedEnemies.add(id, round);
//            }
//            stunnedEnemies = newStunnedEnemies;
//        }
    }

    // should be called at end of turn
    public static void sensePlacedTraps() throws GameActionException {
        MapInfo[] localLocs = rc.senseNearbyMapInfos(TRAP_TRACK_RADIUS_SQUARED);
        nearbyTraps = Utils.filterMapInfoArr(localLocs, (i) -> i.getTrapType() == TrapType.STUN);
    }

    public static int getLastStunnedRound(int id) {
        return stunnedEnemies.getVal(id);
    }

}
