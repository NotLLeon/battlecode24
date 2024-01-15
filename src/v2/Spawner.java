package v2;
import battlecode.common.*;

import static v2.Constants.*;

import v2.fast.*;

public class Spawner {

    // Returns the Map Center

    private static MapLocation[] spawnCenters;

    private final static MapLocation center = new MapLocation(rc.getMapHeight() / 2, rc.getMapWidth() / 2);

    // Get the array of all the spawn locations
    private static void computeSpawnCenters() {
        MapLocation[] spawns = rc.getAllySpawnLocations();
        spawnCenters = new MapLocation[3];
        int idx = 0;

        FastIterableLocSet spawnCoords = new FastIterableLocSet();
        for (MapLocation sp : spawns) spawnCoords.add(sp);

        for (MapLocation sp : spawns) {
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

    // FIXME: jank
    public static boolean spawn() throws GameActionException {
        MapLocation[] tryOrder = Utils.sort3Locations(spawnCenters, loc -> Random.nextInt(1000));
        for (MapLocation spawnLoc : tryOrder) {
            if (spawnInDir(spawnLoc, spawnLoc.directionTo(center))) return true;
        }
        return false;
    }

    public static boolean spawnTo(MapLocation toLoc) throws GameActionException {
        MapLocation[] tryOrder = Utils.sort3Locations(spawnCenters, loc -> loc.distanceSquaredTo(toLoc));
        for (MapLocation spawnLoc : tryOrder) {
            if (spawnInDir(spawnLoc, spawnLoc.directionTo(center))) return true;
        }
        return false;
    }
}
