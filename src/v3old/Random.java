package v3old;

import battlecode.common.Direction;

import static v3old.Constants.*;

public class Random {

    private static java.util.Random rng = null;

    static void init() {
        rng = new java.util.Random(rc.getID());
    }

    static int getDirectionOrderNum(Direction dir) {
        for (int i = 0; i < 8; ++i) {
            if (DIRECTIONS[i] == dir) return i;
        }
        return -1;
    }

    static Direction nextDir() {
        return DIRECTIONS[rng.nextInt(DIRECTIONS.length)];
    }

    static Direction nextDirWeighted(int[] weights, int totalWeight) {
        int idx = 0;
        for (int i = nextInt(totalWeight); idx < DIRECTIONS.length - 1; ++idx) {
            i -= weights[idx];
            if (i <= 0 && weights[idx] > 0) break;
        }
        return DIRECTIONS[idx];
    }

    static int nextIndexWeighted(int[] weights) {
        int sz = weights.length;
        int[] bounds = new int[sz];
        int x = 0;
        //System.out.print("Bounds: ");
        for (int i = 0; i < sz; i++) {
            x += weights[i];
            bounds[i] = x;
            //System.out.print(bounds[i] + " ");
        }
        int random = nextInt(bounds[sz-1]);
       //System.out.print("Random (raw) : " + random_raw);
       //System.out.print("Random : " + random);
        for (int i = 0; i < sz; i++) {
            if (random <= bounds[i]) {
                //System.out.print("Choice: " + i);
                return i;
            }
        }
        return -1;
    }


    static int nextInt(int bound) {
        return rng.nextInt(bound);
    }
    static boolean nextBoolean() {
        return rng.nextBoolean();
    }

}