package v2old;


import battlecode.common.*;
import v2old.Pathfinding.*;
import static v2old.Constants.rc;

public abstract class Robot {

    public static void moveTo(MapLocation dest) throws GameActionException {
        Pathfinding.moveTo(dest, false, -1);
    }

    public static Direction getNextDirection(MapLocation dest) throws GameActionException {
        return Pathfinding.getNextDir(dest, false, -1);
    }

    public static void moveToRadius(MapLocation dest, int radius) throws GameActionException {
        Pathfinding.moveTo(dest, false, radius);
    }

    public static void moveToAdjacent(MapLocation dest) throws GameActionException {
        Pathfinding.moveTo(dest, true, -1);
    }

}