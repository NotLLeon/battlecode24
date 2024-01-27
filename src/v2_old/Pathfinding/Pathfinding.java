package v2_old.pathfinding;

import battlecode.common.*;
import v2_old.Robot;

import static v2_old.Constants.rc;

public class Pathfinding {

    // 1 out of every FILLERS_RATIO units are allowed to fill
    private static final int FILLERS_RATIO = 5;
    private static final int MIN_CRUMBS_TO_FILL = 5 * GameConstants.FILL_COST;

    private static boolean shouldFill() {
        if (rc.hasFlag()) return false;
        return (rc.getID() % FILLERS_RATIO == 0) && (rc.getCrumbs() >= MIN_CRUMBS_TO_FILL);
    }

    public static Direction getNextDir(MapLocation dest, boolean adj, int radius) throws GameActionException {
        MapLocation curLoc = rc.getLocation();
        if(!rc.isMovementReady()
                || curLoc.equals(dest)
                || (adj && curLoc.isAdjacentTo(dest))
                || (radius != -1 && curLoc.distanceSquaredTo(dest) <= radius)) {
            return Direction.CENTER;
        }

        // use BFS when possible, otherwise use BugNav until the obstacle is cleared
        Direction dir = Direction.CENTER;
        if(!BugNav.isTracingObstacle()) {
            dir = BFS.getDir(dest, shouldFill());
            MapLocation nextLoc = curLoc.add(dir);
            if (rc.canFill(nextLoc)) Robot.fill(nextLoc);
        }
        if(dir == Direction.CENTER) {
            dir = BugNav.getDir(dest);
        }
        return dir;
    }

    public static void moveTo(MapLocation dest, boolean adj, int radius) throws GameActionException {
        Direction dir = getNextDir(dest, adj, radius);
        if(rc.canMove(dir)) rc.move(dir);
    }
}
