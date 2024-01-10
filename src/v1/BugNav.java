package v1;

import battlecode.common.*;

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

    public static boolean tracingObstacle() {
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
//        assumedLoc = null;
        isReachable = true;
    }


    public static boolean isReachable(MapLocation dest) {
        return !dest.equals(curDest) || isReachable;
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

    private static boolean onTheMap(RobotController rc, Direction dir) {
        MapLocation loc = rc.getLocation().add(dir);
        return rc.onTheMap(loc);
    }


    private static boolean isPassable(RobotController rc, Direction dir, int strictness) throws GameActionException {
        MapLocation loc = rc.getLocation().add(dir);
        if(!rc.onTheMap(loc)) return false;
        return rc.canMove(dir);
    }

    private static void changeTraceDir() {
        changedTrace = true;
        traceLeft = !traceLeft;
        traceDir = traceDir.opposite();
    }


    public static Direction getDir(RobotController rc, MapLocation dest) throws GameActionException {
//        rc.setIndicatorString(""+changedTrace + " colLoc:" + collisionLoc + " curDest:" + curDest);
//        rc.setIndicatorString(locIndex + Arrays.toString(pastLocs));
//        rc.setIndicatorDot(rc.getLocation(), 256, 0, 0);
        // Bug2
//        rc.setIndicatorString(""+collisionLoc);
        MapLocation curLoc = rc.getLocation();
//        rc.setIndicatorString("" +dest);

        // probably stuck in same place
        if(locIndex < 0 || !curLoc.equals(pastLocs[locIndex])) {
            boolean needsReset = false;
//            rc.setIndicatorString(locIndex + " " + Arrays.toString(pastLocs));
            locIndex = (locIndex+1) % numPastLocs;
            for (int i = 0; i < numPastLocs; ++i) {
                int pos1 = (numPastLocs + locIndex - 2) % numPastLocs;
//                int pos2 = (numPastLocs + locIndex - 4) % numPastLocs;
//                MapLocation loc1 = pastLocs[pos1];
//                MapLocation loc2 = pastLocs[pos2];
//                if(loc1 != null && loc2 != null && loc1.equals(curLoc) && loc2.equals(curLoc)) {
//                    needsReset = true;
//                    break;
//                }
                if (i != pos1) {
                    MapLocation loc = pastLocs[i];
                    if (loc != null && loc.equals(curLoc)) {
                        needsReset = true;
//                    rc.setIndicatorDot(curLoc, 0, 0, 256);
                        break;
                    }
                }
            }
            if (needsReset) reset();
            else pastLocs[locIndex] = curLoc;
        }

//        if(!dest.equals(curDest) || !curLoc.equals(assumedLoc)) {
        if(!dest.equals(curDest)) {
//            rc.setIndicatorDot(curLoc, 0, 256, 0);
            reset();
            curDest = dest;
//            assumedLoc = curLoc;
        }
        Direction dir = curLoc.directionTo(dest);

//        if(obstacle) {
//            rc.setIndicatorLine(collisionLoc, dest, 256, 0, 0);
//            rc.setIndicatorString(
//                    "curLoc: " + curLoc
//                            + "dest: "+ dest
//                            + "slope: " + slope
//                            +"infSlope: " + infSlope);
//        }

        Direction nextDir = null;
        if(!obstacle) {
            if (isPassable(rc, dir, 0)) {
//                rc.setIndicatorString("move: " + dir);
//                assumedLoc = curLoc.add(dir);
                return dir;
            }
//            if(rc.canSenseRobotAtLocation(curLoc.add(dir))) blockingRobot = curLoc.add(dir);
            obstacle = true;
            computeSlope(curLoc, dest);
//            rc.setIndicatorString("found obs at: " + curLoc);
            traceDir = dir;
            dis = curLoc.distanceSquaredTo(dest);
            collisionLoc = curLoc;
            nextDir = dir;
        } else {
//            rc.setIndicatorString("" + collisionLoc);
            turnsTracingObstacle++;
            int curDis = curLoc.distanceSquaredTo(dest);
//            rc.setIndicatorString(dis + " " + curDis + " " +onLine(curLoc) + " " + (lineEval(curLoc.x) - curLoc.y) + " " + collisionLoc);
//            if(blockingRobot != null
//                    && curLoc.isWithinDistanceSquared(blockingRobot, 16)
//                    && !rc.canSenseRobotAtLocation(blockingRobot)) {
//                reset();
//                return getDir(rc, dest);
//            }

            if(onLine(curLoc) && curDis < dis - 1) {
//                rc.setIndicatorDot(curLoc, 0, 0, 256);
                reset();
                return getDir(rc, dest);
            }

            if(curLoc.equals(collisionLoc)) {
                reset();
                isReachable = false;
//                rc.setIndicatorString("broke");
                return Direction.CENTER;
            }
            // TODO: keep track of locs of last n currents that youve passed through, treat them as walls
//            if(turnsTracingObstacle > 10 && !changedTrace && curLoc.distanceSquaredTo(dest) >= dis + 64) {
//                changeTraceDir();
//                softReset();
//                Direction recDir = getDir(rc, dest);
//                collisionLoc = curLoc;
//                return recDir;
//            }

            if(traceLeft) nextDir = traceDir.rotateRight().rotateRight();
            else nextDir = traceDir.rotateLeft().rotateLeft();
        }

        Direction prevDir;
        for(int i = 0; i < 8; ++i) {
            int strictness = 0;
            if(nextDir != traceDir) ++ strictness;
            if(isPassable(rc, nextDir, strictness)) {
                traceDir = nextDir;
                if(traceLeft) prevDir = traceDir.rotateRight().rotateRight();
                else prevDir = traceDir.rotateLeft().rotateLeft();
                if(!changeWallTrace && !onTheMap(rc, prevDir)) {
                    changeWallTrace = true;
//                    rc.setIndicatorString(traceDir + " " + traceLeft + " " + prevDir);
                    changeTraceDir();
                    return getDir(rc, dest);
                }
//                assumedLoc = curLoc.add(traceDir);
                return traceDir;
            } else {
                if(traceLeft) nextDir = nextDir.rotateLeft();
                else nextDir = nextDir.rotateRight();
            }
        }
        return Direction.CENTER;
    }
}