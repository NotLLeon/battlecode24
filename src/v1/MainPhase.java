package v1;

import static v1.Constants.directions;
import static v1.Constants.rc;
import static v1.Random.nextDir;
import static v1.Random.nextInt;
import static v1.Random.rng;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.TrapType;
import battlecode.common.MapInfo;

// MAIN PHASE STRATEGY HERE (TENTATIVE)
public class MainPhase extends Robot {

    static MapLocation[] spawnLocs = rc.getAllySpawnLocations();
    static MapLocation patrolLoc = spawnLocs[Math.abs(rng.nextInt() % spawnLocs.length)];

    static int lastSymState = -1;
    private static MapLocation[] targets;
    private static int targetInd = 0;
    static SYMMETRY_CHECK[] locsyms;
    static boolean[] locIgnore;
    private static boolean knowSymmetry = false;
    private static int roundsNearTarget = 0;

    enum SYMMETRY_CHECK {
        HORIZONTAL(1), VERTICAL(2), ROTATIONAL(4), BASE(0);

        private int corres;
        private int getVal() {
            return corres;
        }
        private SYMMETRY_CHECK(int corres) {
            this.corres = corres;
        }
    }

    public static void run() throws GameActionException {
        if (rc.canPickupFlag(rc.getLocation())){
            rc.pickupFlag(rc.getLocation());
            rc.setIndicatorString("Holding a flag!");
        }
        // If we are holding an enemy flag, singularly focus on moving towards
        // an ally spawn zone to capture it!
        if (rc.hasFlag()){
            moveTo(rc, patrolLoc);
        }

        generateTargets(rc);
        pruneSymmetries(rc);
        runLauncherPatrol(rc);

        updateEnemyRobots();

        healAllies();

        // Move and attack randomly if no objective.
        rc.setIndicatorDot(patrolLoc, 0, 0, 0);
        moveToOutsideRadius(rc, patrolLoc, 2);

        // if (rc.canAttack(nextLoc)){
        //     rc.attack(nextLoc);
        //     System.out.println("Take that! Damaged an enemy that was in our way!");
        // }

        // Rarely attempt placing traps behind the robot.
        MapLocation prevLoc = rc.getLocation().subtract(nextDir());
        if (rc.canBuild(TrapType.EXPLOSIVE, prevLoc) && nextInt(2) == 0)
            rc.build(TrapType.EXPLOSIVE, prevLoc);
        // We can also move our code into different methods or classes to better organize it!
    }

    public static void updateEnemyRobots() throws GameActionException{
        // Sensing methods can be passed in a radius of -1 to automatically
        // use the largest possible value.
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (enemyRobots.length != 0){
            rc.setIndicatorString("There are nearby enemy robots! Scary!");
            // Save an array of locations with enemy robots in them for future use.
            MapLocation[] enemyLocations = new MapLocation[enemyRobots.length];
            for (int i = 0; i < enemyRobots.length; i++){
                enemyLocations[i] = enemyRobots[i].getLocation();
                if (rc.canAttack(enemyLocations[i])) rc.attack(enemyLocations[i]);
            }
            // Let the rest of our team know how many enemy robots we see!
            if (rc.canWriteSharedArray(0, enemyRobots.length)){
                rc.writeSharedArray(0, enemyRobots.length);
                int numEnemies = rc.readSharedArray(0);
            }
        }
    }

    public static void healAllies() throws GameActionException{
        // Sensing methods can be passed in a radius of -1 to automatically
        // use the largest possible value.
        RobotInfo[] allyRobots = rc.senseNearbyRobots(-1, rc.getTeam());
        if (allyRobots.length != 0){
            rc.setIndicatorString("There are nearby enemy robots! Scary!");
            // Save an array of locations with enemy robots in them for future use.
            MapLocation[] allyLocations = new MapLocation[allyRobots.length];
            for (int i = 0; i < allyRobots.length; i++){
                allyLocations[i] = allyRobots[i].getLocation();
                if (rc.canHeal(allyLocations[i])) rc.heal(allyLocations[i]);
            }
            // Let the rest of our team know how many enemy robots we see!
            if (rc.canWriteSharedArray(0, allyRobots.length)){
                rc.writeSharedArray(0, allyRobots.length);
                int numEnemies = rc.readSharedArray(0);
            }
        }
    }

    private static void generateTargets(RobotController rc) throws GameActionException {
        int syms = Comms.getPossibleSyms(rc);
        //        rc.setIndicatorString("SYMS: " + syms + " SET: " + lastSymState);
        if (syms == lastSymState) { return; }

        // % 2 for horz, /2 %2 for vert, /4 for rotational
        targetInd = 0;
        MapLocation [] Hqs = Comms.getHQs(rc);

        lastSymState = syms;
        int mapW = rc.getMapWidth();
        int mapH = rc.getMapHeight();

        MapLocation closestHorz = new MapLocation(0, 0);
        MapLocation closestVert = new MapLocation(0, 0);
        MapLocation closestRot = new MapLocation(0, 0);

        int horMax = 2 * 60 * 60;
        int vertMax = 2 * 60 * 60;
        int rotMax = 2 * 60 * 60;
        for (MapLocation hq : Hqs) {
            MapLocation vertTr = new MapLocation(
                    mapW-hq.x-1,
                    hq.y
            );
            MapLocation horTr = new MapLocation(
                    hq.x,
                    mapH-hq.y-1
            );
            MapLocation rotTr = new MapLocation(
                    mapW-hq.x-1,
                    mapH-hq.y-1
            );
            if (patrolLoc.distanceSquaredTo(vertTr) < vertMax) {
                vertMax = patrolLoc.distanceSquaredTo(vertTr);
                closestVert = vertTr;
            }
            if (patrolLoc.distanceSquaredTo(horTr) < horMax) {
                horMax = patrolLoc.distanceSquaredTo(horTr);
                closestHorz = horTr;
            }
            if (patrolLoc.distanceSquaredTo(rotTr) < rotMax) {
                rotMax = patrolLoc.distanceSquaredTo(rotTr);
                closestRot = rotTr;
            }
        }

        if (syms == 1) {
            MapLocation[] temp = new MapLocation[Hqs.length + 1];
            for (int i = 0; i < Hqs.length; i++) {
                temp[i+1] = new MapLocation(Hqs[i].x, mapH-Hqs[i].y-1);
            }
            temp[0] = new MapLocation(patrolLoc.x, mapH-patrolLoc.y - 1);
            targets = temp;
            locsyms = new SYMMETRY_CHECK[]{};
            locIgnore = new boolean[]{};
            knowSymmetry = true;
        } else if (syms == 2) {
            MapLocation[] temp = new MapLocation[Hqs.length + 1];
            for (int i = 0; i < Hqs.length; i++) {
                temp[i+1] = new MapLocation(mapW-Hqs[i].x-1, Hqs[i].y);
            }
            temp[0] = new MapLocation(mapW-patrolLoc.x-1, patrolLoc.y);
            targets = temp;
            locsyms = new SYMMETRY_CHECK[]{};
            locIgnore = new boolean[]{};
            knowSymmetry = true;
        } else if (syms == 4) {
            MapLocation[] temp = new MapLocation[Hqs.length + 1];
            for (int i = 0; i < Hqs.length; i++) {
                temp[i+1] = new MapLocation(mapW-Hqs[i].x-1, mapH-Hqs[i].y-1);
            }
            temp[0] = new MapLocation(mapW-patrolLoc.x-1, mapH-patrolLoc.y-1);
            targets = temp;
            locsyms = new SYMMETRY_CHECK[]{};
            locIgnore = new boolean[]{};
            knowSymmetry = true;
        } else if (syms == 7) {
            targets = new MapLocation[4];
            locsyms = new SYMMETRY_CHECK[4];
            locIgnore = new boolean[4];

            targets[0] = closestVert;
            targets[1] = closestHorz;
            targets[2] = closestRot;
            targets[3] = patrolLoc;

            int t1 = 2 * 60 * 60;
            int t2 = 2 * 60 * 60;
            for (MapLocation hq : Hqs) {
                int t1dist = hq.distanceSquaredTo(targets[0]);
                int t2dist = hq.distanceSquaredTo(targets[1]);
                if (t1dist < t1) {
                    t1 = t1dist;
                }
                if (t2dist < t2) {
                    t2 = t2dist;
                }
            }
            if (t2 < t1) {
                MapLocation temp = targets[0];
                targets[0] = targets[1];
                targets[1] = targets[2];
                targets[2] = temp;
                locsyms[0] = SYMMETRY_CHECK.HORIZONTAL;
                locsyms[1] = SYMMETRY_CHECK.ROTATIONAL;
                locsyms[2] = SYMMETRY_CHECK.VERTICAL;
                locsyms[3] = SYMMETRY_CHECK.BASE;
            } else {
                MapLocation temp = targets[1];
                targets[1] = targets[2];
                targets[2] = temp;
                locsyms[0] = SYMMETRY_CHECK.VERTICAL;
                locsyms[1] = SYMMETRY_CHECK.ROTATIONAL;
                locsyms[2] = SYMMETRY_CHECK.HORIZONTAL;
                locsyms[3] = SYMMETRY_CHECK.BASE;
            }
        } else {
            boolean horz = syms % 2 == 1;
            boolean vert = (syms / 2) % 2 == 1;
            boolean rot = (syms / 4) == 1;

            targets = new MapLocation[3];
            locsyms = new SYMMETRY_CHECK[3];
            locIgnore = new boolean[3];

            if (horz && vert) {
                // compare the 2 to see what is longer
                targets[0] = closestVert;
                targets[1] = closestHorz;
                targets[2] = patrolLoc;

                int t1 = 60 * 60;
                int t2 = 60 * 60;
                for (MapLocation hq : Hqs) {
                    int t1dist = hq.distanceSquaredTo(targets[0]);
                    int t2dist = hq.distanceSquaredTo(targets[1]);
                    if (t1dist < t1) {
                        t1 = t1dist;
                    }
                    if (t2dist < t2) {
                        t2 = t2dist;
                    }
                }
                if (t2 > t1) {
                    MapLocation temp = targets[0];
                    targets[0] = targets[1];
                    targets[1] = temp;
                    locsyms[0] = SYMMETRY_CHECK.HORIZONTAL;
                    locsyms[1] = SYMMETRY_CHECK.VERTICAL;
                    locsyms[2] = SYMMETRY_CHECK.BASE;
                } else {
                    locsyms[0] = SYMMETRY_CHECK.VERTICAL;
                    locsyms[1] = SYMMETRY_CHECK.HORIZONTAL;
                    locsyms[2] = SYMMETRY_CHECK.BASE;
                }
            } else {
                if (horz) {
                    targets[0] = closestHorz;
                    locsyms[0] = SYMMETRY_CHECK.HORIZONTAL;
                } else if (vert) {
                    targets[0] = closestVert;
                    locsyms[0] = SYMMETRY_CHECK.VERTICAL;
                }

                targets[1] = closestRot;
                targets[2] = patrolLoc;
                locsyms[1] = SYMMETRY_CHECK.ROTATIONAL;
                locsyms[2] = SYMMETRY_CHECK.BASE;
            }
        }
    }

    private static void pruneSymmetries(RobotController rc) throws GameActionException {
        if (rc.canWriteSharedArray(Constants.IDX_POSSIBLE_SYMS, lastSymState) && locsyms.length > 0 && lastSymState > 0) {
            int sym = Comms.getPossibleSyms(rc);
            for (int i = 0; i < locsyms.length; i++) {
                if (locIgnore[i]) {
                    int dec = locsyms[i].getVal();
                    if (dec != 0 && (sym / dec) % 2 == 1) {
                        sym -= locsyms[i].getVal();
                    }
                }
            }
            Comms.writePossibleSyms(rc, sym);
        }
    }

    private static void runLauncherPatrol(RobotController rc) throws GameActionException {
        //        rc.setIndicatorString(""+onTarget);
        MapLocation curTarget = targets[targetInd];

        if (!knowSymmetry) {
            moveToRadius(rc, curTarget, 4);
        } else {
            moveToOutsideRadius(rc, curTarget, 12);
        }
        MapLocation curLoc = rc.getLocation();

        if(rc.canSenseLocation(curTarget)) {
            if (!canSeeHq(rc)) {
                locIgnore[targetInd] = true;
                nextTarget();
            }
        } else if(curLoc.isWithinDistanceSquared(curTarget, 16)) {
            roundsNearTarget++;
        }
        // if(!isReachable(rc, curTarget) || roundsNearTarget > 20) {
        //     if (!isReachable(rc, curTarget)) { locIgnore[targetInd] = true; }
        //     nextTarget();
        // }
    }
    private static void nextTarget() {
        targetInd = (targetInd+1)%targets.length;
    }

    private static boolean canSeeHq(RobotController rc) throws GameActionException {
        MapLocation curTarget = targets[targetInd];
        MapInfo targetFeatures = rc.senseMapInfo(curTarget);
        return targetFeatures.isSpawnZone();
    }
}
