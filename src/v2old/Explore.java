package v2old;

import battlecode.common.*;

import static v2old.Constants.*;

public class Explore {
    private static int prevlocIdx = 0;
    private static int numMoves = 0;
    private static MapLocation[] prevLocs = new MapLocation[EXPLORE_NUM_TRACKED_LOCATIONS];

    private static final int FILLERS_RATIO = 3;
    private static final int MIN_CRUMBS_TO_FILL = 5 * GameConstants.FILL_COST;

    private static Direction prevDir;

    private static boolean shouldFill() {
        return (rc.getID() % FILLERS_RATIO == 0) && (rc.getCrumbs() >= MIN_CRUMBS_TO_FILL);
    }

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

        for (Direction dir : DIRECTIONS) {
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
            Direction tmp = DIRECTIONS[i];
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
            numMoves = 0;
        }

        if (prevLocs[0] == null) {
            Direction dir = Random.nextDir();
            for (int i = 0; i < 8; ++i) {
                if (shouldFill() && rc.canFill(rc.getLocation().add(dir))) {
                    Robot.fill(rc.getLocation().add(dir));
                }
                if (rc.canMove(dir)) {
                    if ((++numMoves) % EXPLORE_MOVES_TO_TRACK_LOCATION == 0) {
                        prevLocs[prevlocIdx] = rc.getLocation();
                        prevlocIdx++;
                    }
                    prevDir = dir;
                    rc.move(dir);
                    break;
                }
                dir = dir.rotateLeft();
            }

        } else {
            Direction dir = prevDir;
            RobotInfo [] robots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
            boolean nearbyEnemy = robots.length > 0 ? true : false;
            if (shouldFill() && rc.canFill(rc.getLocation().add(dir))) {
                Robot.fill(rc.getLocation().add(dir));
            }
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
