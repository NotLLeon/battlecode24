package v1;

import battlecode.common.*;

import static v1.Constants.rc;
import static v1.Constants.directions;
import static v1.Constants.EXPLORE_NUM_TRACKED_LOCATIONS;
import static v1.Constants.EXPLORE_HIGH_WEIGHT_DIRECTION;
import static v1.Constants.EXPLORE_MOVES_TO_TRACK_LOCATION;

public class Explore {
    static int prevlocIdx = 0;
    static int numMoves = 0;
    static MapLocation[] prevLocs = new MapLocation[EXPLORE_NUM_TRACKED_LOCATIONS];

    static Direction prevDir;

    public static MapLocation [] getAllDetectableWalls() throws GameActionException {
        MapLocation [] detectedAreas = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), 4);
        for (int i = 0; i < detectedAreas.length; ++i) {
            MapInfo info = rc.senseMapInfo(detectedAreas[i]);
            if (info.isPassable()) detectedAreas[i] = null;
        }
        return detectedAreas;
    }

    public static Direction exploreAwayFromLoc(MapLocation loc) throws GameActionException {
        MapLocation curLoc = rc.getLocation();
        Direction locDir = curLoc.directionTo(loc);

        // 8 Directions, init all weight 1
        int[] weights = {1, 1, 1, 1, 1, 1, 1, 1};

        if (locDir != Direction.CENTER) {
            // Directions pointing towards loc lowest weight
            // Directions away from loc higher weight
            int dirIndex = Random.getDirectionOrderNum(locDir);
            weights[(dirIndex+2)%8] *= EXPLORE_HIGH_WEIGHT_DIRECTION;
            weights[(dirIndex+3)%8] *= EXPLORE_HIGH_WEIGHT_DIRECTION;
            weights[(dirIndex+4)%8] *= EXPLORE_HIGH_WEIGHT_DIRECTION;
            weights[(dirIndex+5)%8] *= EXPLORE_HIGH_WEIGHT_DIRECTION;
            weights[(dirIndex+6)%8] *= EXPLORE_HIGH_WEIGHT_DIRECTION;
        }
        MapLocation [] unmoveableAreas = getAllDetectableWalls();
        for (MapLocation wall : unmoveableAreas) {
            if (wall == null) continue;
            weights[Random.getDirectionOrderNum(curLoc.directionTo(wall))] = 0;
        }

        for (Direction dir : directions) {
            if (!rc.onTheMap(curLoc.add(dir).add(dir))) {
                weights[Random.getDirectionOrderNum(dir)] = 0;
            }
        }

        RobotInfo [] robots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        for(RobotInfo robot : robots) {
            int dirIndex = Random.getDirectionOrderNum(curLoc.directionTo(robot.getLocation()));
            weights[dirIndex] = 0;
            weights[(dirIndex+1)%8] = 0;
            weights[(dirIndex+7)%8] = 0;
            weights[(dirIndex+3)%8] *= EXPLORE_HIGH_WEIGHT_DIRECTION;
            weights[(dirIndex+4)%8] *= EXPLORE_HIGH_WEIGHT_DIRECTION;
            weights[(dirIndex+5)%8] *= EXPLORE_HIGH_WEIGHT_DIRECTION;
            break;
        }

        for(int i = 0; i < 8; ++i) {
            Direction tmp = directions[i];
            if(!rc.canMove(tmp)) weights[i] = 0;
        }
        int totalWeight = 0;
        for (int w : weights) totalWeight += w;
        if(totalWeight == 0) return Direction.CENTER;
        return Random.nextDirWeighted(weights, totalWeight);
    }

    public static void exploreNewArea() throws GameActionException {
        if (!rc.isMovementReady()) return;

        int numClosePrevLocs = 0;
        for (MapLocation loc : prevLocs) {
            if (loc != null && rc.canSenseLocation(loc)) numClosePrevLocs++;
        }

        if (numClosePrevLocs == EXPLORE_NUM_TRACKED_LOCATIONS) {
            rc.setIndicatorString("TRAPPED");
            numMoves = 0;
        }

        if (prevLocs[0] == null) {
            Direction dir = Random.nextDir();
            for (int i = 0; i < 8; ++i) {
                if (rc.canMove(dir)) {
                    if ((++numMoves) % EXPLORE_MOVES_TO_TRACK_LOCATION == 0) {
                        prevLocs[prevlocIdx] = rc.getLocation();
                        prevlocIdx++;
                    }
                    rc.move(dir);
                    // for empty carriers
                    exploreNewArea();
                    break;
                }
                dir = dir.rotateLeft();
            }

        } else {
            Direction dir = prevDir;
            RobotInfo [] robots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
            boolean nearbyEnemy = robots.length > 0 ? true : false;
            if (numMoves % EXPLORE_MOVES_TO_TRACK_LOCATION == 0 || !rc.canMove(dir) || nearbyEnemy) {
                dir = exploreAwayFromLoc(getAvgLocation(prevLocs));
            }
            if(dir != Direction.CENTER) {
                if ((++numMoves) % EXPLORE_MOVES_TO_TRACK_LOCATION == 0) {
                    numMoves = 0;
                    prevLocs[prevlocIdx] = rc.getLocation();
                    prevlocIdx = (prevlocIdx + 1) % EXPLORE_NUM_TRACKED_LOCATIONS;
                }
                prevDir = dir;
                rc.move(dir);
                exploreNewArea();
            }
        }
    }


    static MapLocation getAvgLocation(MapLocation [] locs) {
        int len = locs.length;
        int x = 0;
        int y = 0;
        for (MapLocation loc : locs) {
            if (loc == null) {
                len--;
                continue;
            }
            x += loc.x;
            y += loc.y;
        }
        return new MapLocation(x / len, y / len);
    }
}
