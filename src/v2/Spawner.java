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
        for(Direction tryDir : Utils.getDirOrdered(dir)) {
            MapLocation spawnPoint = loc.add(tryDir);
            if(rc.canSpawn(spawnPoint)) {
                rc.spawn(spawnPoint);
                return true;
            }
        }
        return false;
    }
    
    private static boolean distressSpawn() throws GameActionException {
        for (int i = 0; i < GameConstants.NUMBER_FLAGS; ++i) {
            MapLocation loc = Comms.readLoc(COMMS_SIGNAL_BOT_DISTRESS_LOCS + i);
            if (loc != null && rc.canSpawn(loc)) {
                for(Direction tryDir : Utils.getDirOrdered(Direction.CENTER)) {
                    MapLocation spawnPoint = loc.add(tryDir);
                    if(rc.canSpawn(spawnPoint)) {
                        rc.spawn(spawnPoint);
                        return true;
                    }
                }
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

    public static boolean spawn() throws GameActionException {
        if (distressSpawn()) return true;
        MapLocation[] tryOrder = Utils.sort3Locations(spawnCenters, i -> Random.nextInt(1000));
        for (MapLocation spawnCenter : tryOrder) {
            if (rc.canSpawn(spawnCenter)) {
                rc.spawn(spawnCenter);
                return true;
            }
            if (spawnInDir(spawnCenter, spawnCenter.directionTo(center))) {
                return true;
            }
        }
        return false;
    }
}
