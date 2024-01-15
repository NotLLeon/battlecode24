package v2;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

import java.util.function.Function;

import static v2.Constants.rc;

public class Utils {

    @FunctionalInterface
    public interface CheckedFunction<T, R> {
        R apply (T t) throws GameActionException;
    }

    private static Function<Integer, Boolean> lambdaExceptionWrapper(CheckedFunction<Integer, Boolean> fn) {
        return i -> {
            try { return fn.apply(i); }
            catch(GameActionException e) { return false; }
        };
    }

    public static int[] filterIntArr(int[] arr, CheckedFunction<Integer, Boolean> fn) {
        int numRemaining = 0;
        Function<Integer, Boolean> sfn = lambdaExceptionWrapper(fn);

        // idk if this is the best way to do this
        for (int ind : arr) if (sfn.apply(ind)) ++numRemaining;

        int[] filtered = new int[numRemaining];
        int i = 0;
        for (int ind : arr) if (sfn.apply(ind)) filtered[i++] = ind;
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
}
