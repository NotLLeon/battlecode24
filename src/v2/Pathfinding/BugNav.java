package v2.Pathfinding;

import battlecode.common.*;

import static v2.Constants.rc;

//import java.util.Arrays;

public class BugNav {
    private static boolean obstacle = false;
    private static MapLocation curDest = null;
    private static int dis = 0;
    private static boolean infSlope = false;
    private static double slope = 0;
    private static Direction traceDir = null;
    private static MapLocation collisionLoc = null;
    private static boolean isReachable = true;
    //    private static MapLocation assumedLoc = null;
    private final static int numPastLocs = 15;
    private static MapLocation[] pastLocs = new MapLocation[numPastLocs];
    private static int locIndex = -1;
    private static MapLocation blockingRobot = null;
    private static boolean traceLeft = true;
    private static int turnsTracingObstacle = 0;
    private static boolean changedTrace = false;
    private static boolean changeWallTrace = false;

    public static boolean isTracingObstacle() {
        return obstacle;
    }

    private static void reset() {
        softReset();
        turnsTracingObstacle = 0;
        curDest = null;
        collisionLoc = null;
        obstacle = false;
        changedTrace = false;
        changeWallTrace = false;
    }

    private static void softReset() {
        pastLocs = new MapLocation[numPastLocs];
        locIndex = -1;
        isReachable = true;
    }

    private static double lineEval(double x) {
        return slope*(x-collisionLoc.x) + collisionLoc.y;
    }

    private static boolean onLine(MapLocation loc) {
        if(infSlope) return loc.x == collisionLoc.x;
        if(slope == 0) return loc.y == collisionLoc.y;
        double eval = lineEval(loc.x);
        boolean cond1 = lineEval(loc.x + 0.5) >= eval;
        boolean cond2 = lineEval(loc.x - 0.5) >= eval;
        return (cond1^cond2);
    }

    private static void computeSlope(MapLocation p1, MapLocation p2) {
        if (p1.x == p2.x) {
            infSlope = true;
            slope = 0;
            return;
        }
        slope = ((double) (p1.y - p2.y)) / (p1.x - p2.x);
        infSlope = false;
    }

    private static boolean onTheMap(Direction dir) {
        MapLocation loc = rc.getLocation().add(dir);
        return rc.onTheMap(loc);
    }

    private static boolean isPassable(Direction dir) throws GameActionException {
        MapLocation loc = rc.getLocation().add(dir);
        if(!rc.onTheMap(loc)) return false;
        return rc.canMove(dir);
    }

    private static void changeTraceDir() {
        changedTrace = true;
        traceLeft = !traceLeft;
        traceDir = traceDir.opposite();
    }


    public static Direction getDir(MapLocation dest) throws GameActionException {
        MapLocation curLoc = rc.getLocation();

        // probably stuck in same place
        if(locIndex < 0 || !curLoc.equals(pastLocs[locIndex])) {
            boolean needsReset = false;
            locIndex = (locIndex+1) % numPastLocs;
            for (int i = 0; i < numPastLocs; ++i) {
                int pos1 = (numPastLocs + locIndex - 2) % numPastLocs;
                if (i != pos1) {
                    MapLocation loc = pastLocs[i];
                    if (loc != null && loc.equals(curLoc)) {
                        needsReset = true;
                        break;
                    }
                }
            }
            if (needsReset) reset();
            else pastLocs[locIndex] = curLoc;
        }

        if(!dest.equals(curDest)) {
            reset();
            curDest = dest;
        }
        Direction dir = curLoc.directionTo(dest);

        Direction nextDir = null;
        if(!obstacle) {
            if (isPassable(dir)) return dir;
            obstacle = true;
            computeSlope(curLoc, dest);
            traceDir = dir;
            dis = curLoc.distanceSquaredTo(dest);
            collisionLoc = curLoc;
            nextDir = dir;
            determineTraceSide();
        } else {
            turnsTracingObstacle++;
            int curDis = curLoc.distanceSquaredTo(dest);

            if(onLine(curLoc) && curDis < dis - 1) {
                reset();
                return getDir(dest);
            }

            if(curLoc.equals(collisionLoc)) {
                reset();
                isReachable = false;
                return Direction.CENTER;
            }

            if(traceLeft) nextDir = traceDir.rotateRight().rotateRight();
            else nextDir = traceDir.rotateLeft().rotateLeft();
        }

        Direction prevDir;
        for(int i = 8; --i >= 0;) {
            if (isPassable(nextDir)) {
                traceDir = nextDir;
                if(traceLeft) prevDir = traceDir.rotateRight().rotateRight();
                else prevDir = traceDir.rotateLeft().rotateLeft();
                if(!changeWallTrace && !onTheMap(prevDir)) {
                    changeWallTrace = true;
                    changeTraceDir();
                    return getDir(dest);
                }
                return traceDir;
            } else {
                if(traceLeft) nextDir = nextDir.rotateLeft();
                else nextDir = nextDir.rotateRight();
            }
        }
        return Direction.CENTER;
    }

    private static void determineTraceSide() throws GameActionException {
        Direction firstLeft = traceDir.rotateLeft();
        Direction firstRight = traceDir.rotateRight();
        for(int i = 8; --i >= 0;) {
            if (!isPassable(firstLeft)) firstLeft = firstLeft.rotateLeft();
            if (!isPassable(firstRight)) firstRight = firstRight.rotateRight();
        }
        MapLocation curLoc = rc.getLocation();
        MapLocation leftLoc = curLoc.add(firstLeft);
        MapLocation rightLoc = curLoc.add(firstRight);
        traceLeft = leftLoc.distanceSquaredTo(curDest) < rightLoc.distanceSquaredTo(curDest);
    }
}