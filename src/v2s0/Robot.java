package v2s0;


import battlecode.common.*;
import v2s0.Pathfinding.*;
import static v2s0.Constants.rc;

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

}
