package v1;


import battlecode.common.*;
import v1.Pathfinding.*;

public abstract class Robot {

    public static void moveTo(RobotController rc, MapLocation dest) throws GameActionException {
        moveTo(rc, dest, false, -1);
    }

    public static void moveToRadius(RobotController rc, MapLocation dest, int radius) throws GameActionException {
        moveTo(rc, dest, false, radius);
    }

    public static void moveToOutsideRadius(RobotController rc, MapLocation center, int radius) throws GameActionException {
MapLocation currLoc = rc.getLocation();
        if (currLoc.isWithinDistanceSquared(center, radius)) {
Direction opp = currLoc.directionTo(center).opposite();
            moveTo(rc, currLoc.add(opp).add(opp).add(opp));
        } else {
int r = (int)Math.sqrt(radius) + 1;
            moveToRadius(rc, center, r*r);
        }
    }

    private static void moveTo(RobotController rc, MapLocation dest, boolean adj, int radius) throws GameActionException {
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
dir = BFS.getDir(rc, dest);
        }
        if(dir == Direction.CENTER) {
dir = BugNav.getDir(rc, dest);
        }
        if(dir != Direction.CENTER) rc.move(dir);
    }

    static MapLocation findClosestLoc(RobotController rc, MapLocation[] locs) {
MapLocation curLoc = rc.getLocation();
int minDist = 10000;
MapLocation closest = new MapLocation(0, 0);
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
}
