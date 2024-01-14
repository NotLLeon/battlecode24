package v2_water_trap;


import battlecode.common.*;
import v2_water_trap.Pathfinding.*;
import static v2_water_trap.Constants.rc;

public abstract class Robot {

    public static void moveTo(MapLocation dest) throws GameActionException {
        Pathfinding.moveTo(dest, false, -1);
    }

    public static void moveToRadius(MapLocation dest, int radius) throws GameActionException {
        Pathfinding.moveTo(dest, false, radius);
    }

    public static void moveToAdjacent(MapLocation dest) throws GameActionException {
        Pathfinding.moveTo(dest, true, -1);
    }

    static MapLocation findClosestLoc(MapLocation[] locs) {
        MapLocation curLoc = rc.getLocation();
        int minDist = 10000;
        MapLocation closest = null;
        for (MapLocation loc : locs) {
            if (loc == null) continue;
            int newDist = curLoc.distanceSquaredTo(loc);
            if (newDist < minDist) {
                minDist = newDist;
                closest = loc;
            }
        }
        return closest;
    }

    public static void buildDefensiveTrap() throws GameActionException {
        // Check if the robot can build an water trap at the location
        FlagInfo[] nearbyFlags = rc.senseNearbyFlags(-1, rc.getTeam());
        if (nearbyFlags.length == 0) return;
        MapLocation sp = nearbyFlags[0].getLocation();

        if (rc.canBuild(TrapType.WATER, sp)) {
            rc.build(TrapType.WATER, sp);
        }
    }

    public static void buildOffensiveTrap() throws GameActionException {
        // Check if the robot can build an water trap at the location
        MapInfo[] nearbyLocs = rc.senseNearbyMapInfos(2);

        for (MapInfo test : nearbyLocs) {
            MapLocation testPn = test.getMapLocation();
            if (test.getSpawnZoneTeam() == (rc.getTeam() == Team.A ? 2 : 1) && rc.canBuild(TrapType.WATER, testPn)) {
                rc.build(TrapType.WATER, testPn);
        }
        }
    }
}
