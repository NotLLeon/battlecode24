package v2_old;

import battlecode.common.*;

import java.util.function.Function;

import static v2_old.Constants.rc;

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

    // there has to be a better way to do this... arrays dont play nice with generics
    public static int[] filterIntArr(int[] arr, CheckedFunction<Integer, Boolean> fn) {
        Function<Integer, Boolean> sfn = lambdaExceptionWrapper(fn, false);
        int[] filtered = new int[arr.length];
        int ind = 0;
        for (int i = arr.length; --i >= 0;) if (sfn.apply(arr[i])) filtered[ind++] = arr[i];
        int[] filteredTrimmed = new int[ind];
        for (int i = ind; --i >= 0;) filteredTrimmed[i] = filtered[ind - i - 1];
        return filteredTrimmed;
    }

    public static MapLocation[] filterLocArr(MapLocation[] arr, CheckedFunction<MapLocation, Boolean> fn) {
        Function<MapLocation, Boolean> sfn = lambdaExceptionWrapper(fn, false);
        MapLocation[] filtered = new MapLocation[arr.length];
        int ind = 0;
        for (int i = arr.length; --i >= 0;) if (sfn.apply(arr[i])) filtered[ind++] = arr[i];
        MapLocation[] filteredTrimmed = new MapLocation[ind];
        for (int i = ind; --i >= 0;) filteredTrimmed[i] = filtered[ind - i - 1];
        return filteredTrimmed;
    }

    public static Direction[] filterDirArr(Direction[] arr, CheckedFunction<Direction, Boolean> fn) {
        Function<Direction, Boolean> sfn = lambdaExceptionWrapper(fn, false);
        Direction[] filtered = new Direction[arr.length];
        int ind = 0;
        for (int i = arr.length; --i >= 0;) if (sfn.apply(arr[i])) filtered[ind++] = arr[i];
        Direction[] filteredTrimmed = new Direction[ind];
        for (int i = ind; --i >= 0;) filteredTrimmed[i] = filtered[ind - i - 1];
        return filteredTrimmed;
    }

    public static MapInfo[] filterMapInfoArr(MapInfo[] arr, CheckedFunction<MapInfo, Boolean> fn) {
        Function<MapInfo, Boolean> sfn = lambdaExceptionWrapper(fn, false);
        MapInfo[] filtered = new MapInfo[arr.length];
        int ind = 0;
        for (int i = arr.length; --i >= 0;) if (sfn.apply(arr[i])) filtered[ind++] = arr[i];
        MapInfo[] filteredTrimmed = new MapInfo[ind];
        for (int i = ind; --i >= 0;) filteredTrimmed[i] = filtered[ind - i - 1];
        return filteredTrimmed;
    }

    public static RobotInfo[] filterRobotInfoArr(RobotInfo[] arr, CheckedFunction<RobotInfo, Boolean> fn) {
        Function<RobotInfo, Boolean> sfn = lambdaExceptionWrapper(fn, false);
        RobotInfo[] filtered = new RobotInfo[arr.length];
        int ind = 0;
        for (int i = arr.length; --i >= 0;) if (sfn.apply(arr[i])) filtered[ind++] = arr[i];
        RobotInfo[] filteredTrimmed = new RobotInfo[ind];
        for (int i = ind; --i >= 0;) filteredTrimmed[i] = filtered[ind - i - 1];
        return filteredTrimmed;
    }

    public static MapLocation[] mapInfoToLocArr(MapInfo[] arr) {
        MapLocation[] mapLocs = new MapLocation[arr.length];
        for (int i = arr.length; --i >= 0;) mapLocs[i] = arr[i].getMapLocation();

        return mapLocs;
    }

    public static MapLocation[] robotInfoToLocArr(RobotInfo[] arr) {
        MapLocation[] mapLocs = new MapLocation[arr.length];
        for (int i = arr.length; --i >= 0;) mapLocs[i] = arr[i].getLocation();

        return mapLocs;
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

    public static Direction[] getDirOrdered(Direction dir) {
        return new Direction[] {
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
    }

    public static MapLocation getCentroid(MapLocation[] locs) {
        int x = 0, y = 0;
        for (MapLocation loc : locs) {
            x += loc.x;
            y += loc.y;
        }
        return new MapLocation(x / locs.length, y / locs.length);
    }

    // Gets if dir1 is within 45 degrees of dir2
    public static boolean inGeneralDirection(Direction dir1, Direction dir2) {
        return dir1.rotateLeft() == dir2 || dir1.rotateRight() == dir2 || dir1 == dir2;
    }
}