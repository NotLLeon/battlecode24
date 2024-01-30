package v3.pathfinding;

import battlecode.common.*;
import v3.MainPhase;
import v3.Robot;

import static v3.Constants.rc;

public class Pathfinding {

    // 1 out of every FILLERS_RATIO units are allowed to fill
    private static final int FILLERS_RATIO = 5;
    private static final int MIN_CRUMBS_TO_FILL = 5 * GameConstants.FILL_COST;
    private static final int MOVEMENT_HISTORY_RECORD_COUNT = 5;
    private static final int MOVEMENT_HISTORY_INTERVAL = 5;
    private static MapLocation[] movementHistory = new MapLocation[MOVEMENT_HISTORY_RECORD_COUNT];
    private static int movesMade = 0;
    private static int movementHistoryInd = 0;

    private static boolean shouldFill() {
        if (rc.hasFlag()) return false;
        return (rc.getID() % FILLERS_RATIO == 0) && (rc.getCrumbs() >= MIN_CRUMBS_TO_FILL) || MainPhase.getDistressFill();
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

    public static MapLocation backtrack() throws GameActionException {
        int nextInd = (movementHistoryInd + MOVEMENT_HISTORY_RECORD_COUNT - 1) % MOVEMENT_HISTORY_RECORD_COUNT;
        if (movementHistory[nextInd] != null) {
            MapLocation retval = movementHistory[nextInd];
            if (rc.getLocation().equals(movementHistory[nextInd])) {
                movementHistory[nextInd] = null;
                movementHistoryInd = (movementHistoryInd + MOVEMENT_HISTORY_RECORD_COUNT - 1) % MOVEMENT_HISTORY_INTERVAL;
            }
            return retval;
        }
        return null;
    }

    public static String getIndicatorString() {
        int nextInd = (movementHistoryInd + MOVEMENT_HISTORY_RECORD_COUNT - 1) % MOVEMENT_HISTORY_RECORD_COUNT;
        String ret = "";
        while (movementHistory[nextInd] != null) {
            ret += movementHistory[nextInd];
            nextInd = (nextInd + MOVEMENT_HISTORY_RECORD_COUNT - 1) % MOVEMENT_HISTORY_RECORD_COUNT;
        }
        return ret;
    }

    public static void moveTo(MapLocation dest, boolean adj, int radius, boolean recordSpots) throws GameActionException {
        Direction dir = getNextDir(dest, adj, radius);
        if(rc.canMove(dir)) {
            if (recordSpots) {
                if (movesMade - MOVEMENT_HISTORY_INTERVAL == 0) {
                    movementHistory[movementHistoryInd] = rc.getLocation();
                    movementHistoryInd = (movementHistoryInd + 1) % MOVEMENT_HISTORY_RECORD_COUNT;
                    movesMade = 0;
                }
                ++movesMade;
            }
            rc.move(dir);
        }
    }
}
