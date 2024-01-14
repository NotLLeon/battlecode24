package v2;
import battlecode.common.*;

import static v2.Constants.*;
import static v2.Constants.Role;
import static v2.RobotPlayer.role;
import static v2.RobotPlayer.spawnLoc;

import v2.fast.*;

public class Spawner {

    // Returns the Map Center

    private static MapLocation[] spawnCenters;

    private final static MapLocation center = new MapLocation(rc.getMapHeight() / 2, rc.getMapWidth() / 2);

    // Get the array of all the spawn locations
    private static void getSpawnCenters() {
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

    private static void spawnInDir(MapLocation loc, Direction dir) throws GameActionException {
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
                return;
            }
        }
    }

    public static void init() {
        getSpawnCenters();
    }

    public static void spawn() throws GameActionException {
        if (role == Role.SIGNAL) {
            if(rc.canSpawn(spawnLoc)) {
                rc.spawn(spawnLoc);
            }
            return;
        }
        MapLocation spawnCenter = spawnCenters[Random.nextInt(3)];
        spawnInDir(spawnCenter, spawnCenter.directionTo(center));
    }
}
