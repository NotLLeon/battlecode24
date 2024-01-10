package v1;

import battlecode.common.Direction;

import static v1.Constants.directions;

public class Random {

    static java.util.Random rng = null;

    static void initRandom(int seed) {
        rng = new java.util.Random(seed);
    }

    static int getDirectionOrderNum(Direction dir) {
        for (int i = 0; i < 8; ++i) {
            if (directions[i] == dir) return i;
        }
        return -1;
    }

    static Direction nextDir() {
        return directions[rng.nextInt(directions.length)];
    }

    static Direction nextDirWeighted(int[] weights, int totalWeight) {
        int idx = 0;
        for (int i = nextInt(totalWeight); idx < directions.length - 1; ++idx) {
            i -= weights[idx];
            if (i <= 0 && weights[idx] > 0) break;
        }
        return directions[idx];
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