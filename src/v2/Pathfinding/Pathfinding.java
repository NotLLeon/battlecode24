package v2.Pathfinding;

import battlecode.common.*;

import static v2.Constants.rc;

public class Pathfinding {

    public static void moveTo(MapLocation dest, boolean adj, int radius) throws GameActionException {
        MapLocation curLoc = rc.getLocation();
        if(!rc.isMovementReady()
                || curLoc.equals(dest)
                || (adj && curLoc.isAdjacentTo(dest))
                || (radius != -1 && curLoc.distanceSquaredTo(dest) <= radius)) {
            return;
        }

        // use BFS when possible, otherwise use BugNav until the obstacle is cleared
        Direction dir = Direction.CENTER;
        if(!BugNav.isTracingObstacle()) {
            dir = BFS.getDir(dest);
        }
        if(dir == Direction.CENTER) {
            dir = BugNav.getDir(dest);
        }
        if(dir != Direction.CENTER) rc.move(dir);
    }
}
