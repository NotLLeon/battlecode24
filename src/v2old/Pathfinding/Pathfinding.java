package v2old.Pathfinding;

import battlecode.common.*;

import static v2old.Constants.rc;

public class Pathfinding {

    // 1 out of every FILLERS_RATIO units are allowed to fill
    private static final int FILLERS_RATIO = 5;
    private static final int MIN_CRUMBS_TO_FILL = 5 * GameConstants.FILL_COST;

    private static boolean shouldFill() {
        return (rc.getID() % FILLERS_RATIO == 0) && (rc.getCrumbs() >= MIN_CRUMBS_TO_FILL);
    }

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
            dir = BFS.getDir(dest, shouldFill());
            MapLocation nextLoc = curLoc.add(dir);
            if (rc.canFill(nextLoc)) rc.fill(nextLoc);
        }
        if(dir == Direction.CENTER) {
            dir = BugNav.getDir(dest);
        }
        if(rc.canMove(dir)) rc.move(dir);
    }
}
