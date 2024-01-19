package v2s0;

import battlecode.common.*;

import java.util.function.Function;

import static v2s0.Constants.rc;

public class Utils {

    @FunctionalInterface
    public interface CheckedFunction<T, R> {
        R apply (T t) throws GameActionException;
    }

    private static <T, R> Function<T, R> lambdaExceptionWrapper(CheckedFunction<T, R> fn, R defaultVal) {
        return i -> {
            try { return fn.apply(i); }
            catch(GameActionException e) { return defaultVal; }
        };
    }

    public static int[] filterIntArr(int[] arr, CheckedFunction<Integer, Boolean> fn) {
        Function<Integer, Boolean> sfn = lambdaExceptionWrapper(fn, false);
        int numRemaining = 0;

        // idk if this is the best way to do this
        for (int ind : arr) if (sfn.apply(ind)) ++numRemaining;

        int[] filtered = new int[numRemaining];

        int i = 0;
        for (int ind : arr) if (sfn.apply(ind)) filtered[i++] = ind;
        return filtered;
    }

    public static MapLocation[] filterLocArr(MapLocation[] arr, CheckedFunction<MapLocation, Boolean> fn) {
        Function<MapLocation, Boolean> sfn = lambdaExceptionWrapper(fn, false);
        int numRemaining = 0;

        // idk if this is the best way to do this
        for (MapLocation loc : arr) if (sfn.apply(loc)) ++numRemaining;

        MapLocation[] filtered = new MapLocation[numRemaining];

        int i = 0;
        for (MapLocation loc : arr) if (sfn.apply(loc)) filtered[i++] = loc;
        return filtered;
    }

    public static MapLocation findClosestLoc(MapLocation[] locs) {
        MapLocation curLoc = rc.getLocation();
        int minDist = 10000;
        MapLocation closest = null;
        for (MapLocation loc : locs) {
            if (loc == null) continue;
            int newDist = curLoc.distanceSquaredTo(loc);
            if (newDist < minDist) {
                minDist = newDist;
                closest = loc;
            }
        }
        return closest;
    }

    public static MapLocation[] sort3Locations(MapLocation[] locs, CheckedFunction<MapLocation, Integer> keyFn) {
        Function<MapLocation, Integer> sKeyFn = lambdaExceptionWrapper(keyFn, 0);
        int key0 = sKeyFn.apply(locs[0]), key1 = sKeyFn.apply(locs[1]), key2 = sKeyFn.apply(locs[2]);
        if (key0 <= key1 && key1 <= key2) return locs;
        if (key0 <= key2 && key2 <= key1) return new MapLocation[]{locs[0], locs[2], locs[1]};
        if (key1 <= key0 && key0 <= key2) return new MapLocation[]{locs[1], locs[0], locs[2]};
        if (key1 <= key2 && key2 <= key0) return new MapLocation[]{locs[1], locs[2], locs[0]};
        if (key2 <= key0 && key0 <= key1) return new MapLocation[]{locs[2], locs[0], locs[1]};
        return new MapLocation[]{locs[2], locs[1], locs[0]};
    }
}
