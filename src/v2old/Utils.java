package v2old;

import battlecode.common.GameActionException;

import java.util.function.Function;

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
}
