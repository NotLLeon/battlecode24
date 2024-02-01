package v3;

import battlecode.common.*;
import static v3.Constants.*;

public class Symmetry {
    private static final int MIN_RADIUS_SQ = 36;
    private static int isInit = -1;
    
    public static MapLocation[] H;
    public static MapLocation[] V;
    public static MapLocation[] R;

    public static MapLocation[] allyCenters;

    public static int getSymmetry() throws GameActionException {
        return Comms.read(COMMS_SYMM_CHECK);
    }

    public static MapLocation guessEnemyCentroid() throws GameActionException {
        int sym = Comms.read(COMMS_SYMM_CHECK);
        int temp = sym;

        int bitSet = 0;
        while (temp > 0) {
            bitSet += temp % 2;
            temp >>= 1;
        }

        if (H == null) { System.out.println("BRUH"); }
        if (V == null) { System.out.println("BRUH"); }
        if (R == null) { System.out.println("BRUH"); }

        int idx = 0;
        MapLocation[] possibleEnemyLocations = new MapLocation[3 * bitSet];
        if (sym % 2 == 1) {
            for (MapLocation es : H) {
                possibleEnemyLocations[idx++] = es;
            }
        }
        if ((sym >> 1) % 2 == 1) {
            for (MapLocation es : V) {
                possibleEnemyLocations[idx++] = es;
            }
        }
        if ((sym >> 2) % 2 == 1) {
            for (MapLocation es : R) {
                possibleEnemyLocations[idx++] = es;
            }
        }
        return Utils.getCentroid(possibleEnemyLocations);
    }

    private static void initSymmetries() throws GameActionException {
        // first bit - H, second bit - V, third bit - R
        int possibleSyms = 7;

        for (MapLocation allyCenter : allyCenters) {
            for (MapLocation h : H) {
                if (h.distanceSquaredTo(allyCenter) < MIN_RADIUS_SQ) {
                    // turn off horizontal
                    possibleSyms &= (7-(1<<0));
                    break;
                }
            }
            for (MapLocation v : V) {
                if (v.distanceSquaredTo(allyCenter) < MIN_RADIUS_SQ) {
                    // turn off vertical
                    possibleSyms &= (7-(1<<1));
                    break;
                }
            }
            for (MapLocation r : R) {
                if (r.distanceSquaredTo(allyCenter) < MIN_RADIUS_SQ) {
                    // turn off rotational
                    possibleSyms &= (7-(1<<2));
                    break;
                }
            }
        }
        Comms.write(COMMS_SYMM_CHECK, possibleSyms);
    }

    private static void generateTargets() throws GameActionException {
        allyCenters = Spawner.getSpawnCenters();

        int mapH = rc.getMapHeight();
        int mapW = rc.getMapWidth();

        H = Utils.mapLocArr(allyCenters, (i) -> new MapLocation(i.x, mapH-i.y-1));
        V = Utils.mapLocArr(allyCenters, (i) -> new MapLocation(mapW-i.x-1, i.y));
        R = Utils.mapLocArr(allyCenters, (i) -> new MapLocation(mapW-i.x-1, mapH-i.y-1));
    }

    public static void init() throws GameActionException {
        // already initialized -> skip
        if (isInit == 0) return; 

        // check if the bit in the array is 0 and return if it is since -> init is done
        generateTargets();
        initSymmetries();
        isInit = 0;
    }

}
