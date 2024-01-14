package v2;
import battlecode.common.*;

import static v2.Constants.*;
import v2.fast.*;

public class Spawner {

    // Returns the Map Center
    public static MapLocation center() {
        return new MapLocation(rc.getMapHeight() / 2, rc.getMapWidth() / 2);
    }

    // Get the array of all the spawn locations
    public static MapLocation[] spawnCenters() {
        MapLocation[] spawns = rc.getAllySpawnLocations();
        MapLocation[] spawnCenters = new MapLocation[3];
        int idx = 0;

        FastIterableLocSet spawnCoords = new FastIterableLocSet();
        for (MapLocation sp : spawns) {
            spawnCoords.add(sp);
        }

        for (MapLocation sp : spawns) {
            MapLocation NE = sp.add(Direction.NORTHEAST);
            MapLocation SW = sp.add(Direction.SOUTHWEST);
            if (spawnCoords.contains(NE) && spawnCoords.contains(SW)) {
                spawnCenters[idx++] = sp;
            }
        }
        return spawnCenters;
    }
}
