package v2;
import battlecode.common.*;

import static v2.Constants.*;

import v2.Constants.Role;
import v2.fast.*;

public class Spawner {

    private static final int SPAWN_WAVE_INTERVAL = 25;
    private static MapLocation[] spawnCenters;

    private static MapLocation[] allSpawns;

    // different from one in Constants - includes center
    private static Direction[] allDirections = Direction.allDirections();

    private final static MapLocation center = new MapLocation(rc.getMapHeight() / 2, rc.getMapWidth() / 2);

    // Get the array of all the spawn locations
    private static void computeSpawnCenters() {
        allSpawns = rc.getAllySpawnLocations();
        spawnCenters = new MapLocation[3];
        int idx = 0;

        FastIterableLocSet spawnCoords = new FastIterableLocSet();
        for (MapLocation sp : allSpawns) spawnCoords.add(sp);

        for (MapLocation sp : allSpawns) {
            MapLocation NE = sp.add(Direction.NORTHEAST);
            MapLocation SW = sp.add(Direction.SOUTHWEST);
            if (spawnCoords.contains(NE) && spawnCoords.contains(SW)) {
                spawnCenters[idx++] = sp;
            }
        }
    }

    private static boolean spawnInDir(MapLocation loc, Direction dir) throws GameActionException {
        Direction[] tryDirs = {
                dir,
                dir.rotateRight(),
                dir.rotateLeft(),
                Direction.CENTER,
                dir.rotateRight().rotateRight(),
                dir.rotateLeft().rotateLeft(),
                dir.rotateLeft().opposite(),
                dir.rotateRight().opposite(),
                dir.opposite()
        };
        for(Direction tryDir : tryDirs) {
            MapLocation spawnPoint = loc.add(tryDir);
            if(rc.canSpawn(spawnPoint)) {
                rc.spawn(spawnPoint);
                return true;
            }
        }
        return false;
    }

    public static MapLocation[] getSpawnCenters() {
        return spawnCenters;
    }

    public static void init() {
        computeSpawnCenters();
    }

    public static boolean initialSpawn() throws GameActionException {
        for(MapLocation loc : allSpawns) {
            if (rc.canSpawn(loc)) {
                rc.spawn(loc);
                return true;
            }
        }
        return false;
    }

    // FIXME: jank
    public static boolean spawn() throws GameActionException {
        if (Robot.role == Role.SIGNAL) return spawnSignalBot();

        if (spawnNearDistress()) return true;

        if (rc.getRoundNum() % SPAWN_WAVE_INTERVAL != 1) return false;

        for (int i = 0; i < 10; ++i) {
            MapLocation spawnCenter = spawnCenters[Random.nextInt(3)];
            if (spawnInDir(spawnCenter, spawnCenter.directionTo(center))) {
                return true;
            }
        }
        return false;
    }

    // TODO: don't spawn signal bot on captured flag (how do we tell if captured?)
    private static boolean spawnSignalBot() throws GameActionException {
        for(MapLocation loc : spawnCenters) {
            if (rc.canSpawn(loc)) {
                rc.spawn(loc);
                return true;
            }
        }
        return false;
    }

    // TODO: also spawn near flag distress signals (not just signal bot),
    //  also spawn in direction of distress (needs to be stored)
    private static boolean spawnNearDistress() throws GameActionException {
        MapLocation[] signalDistressLocs = Utils.filterLocArr(spawnCenters, SignalBot::isDistressLoc);

        // if no signal distress, try spawning in dir of flag distress
        if(signalDistressLocs.length == 0) {
            MapLocation[] distressLocs = FlagDefense.getAllDistressLocs();
            if (distressLocs.length == 0) return false;
            MapLocation targetLoc = distressLocs[Random.nextInt(distressLocs.length)];
            MapLocation nearestSpawnCenter = null;
            for (int i = 0; i < 3; ++i) {
                if (nearestSpawnCenter == null || 
                    targetLoc.distanceSquaredTo(spawnCenters[i]) < targetLoc.distanceSquaredTo(nearestSpawnCenter)) {
                        nearestSpawnCenter = spawnCenters[i];
                }
            }
            return spawnInDir(nearestSpawnCenter, nearestSpawnCenter.directionTo(targetLoc));
        }
        // FIXME: janky
        MapLocation spawnDistressLoc = signalDistressLocs[Random.nextInt(signalDistressLocs.length)];

        return spawnInDir(spawnDistressLoc, allDirections[Random.nextInt(allDirections.length)]);
    }
}
