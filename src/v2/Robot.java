package v2;


import battlecode.common.*;
import v2.Pathfinding.*;
import static v2.Constants.rc;

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

    public static void moveToOutsideRadius(MapLocation center, int radius) throws GameActionException {
        MapLocation currLoc = rc.getLocation();
        if (currLoc.isWithinDistanceSquared(center, radius)) {
            Direction opp = currLoc.directionTo(center).opposite();
            moveTo(currLoc.add(opp).add(opp).add(opp));
        } else {
            moveToRadius(center, radius);
        }
    }

}
