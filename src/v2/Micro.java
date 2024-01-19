package v2;

import battlecode.common.*;

import static v2.Constants.*;

public class Micro {

    // ROUGH STRATEGY:
    // if you can see flag, move towards flag
    // if there are no enemys in vision radius, heal friendly (move toward and heal?)
    // if there is an enemy in attack range, attack, then move away or chase
    // if there is an enemy in vision radius, move away or closer, attack if possible
    // (friendly carrying flag?)
    // (group units?)

    // try not to move diagonally (messes up formation)
    private static final int FLAG_ESCORT_RADIUS_SQUARED = 4;
    private static final int RETREAT_HEALTH_THRESHOLD = 200;
    private final static int BASE_ATTACK_DAMAGE = 150;

    private static RobotInfo[] visibleAllyRobots;
    private static RobotInfo[] visibleEnemyRobots;
    private static RobotInfo[] closeEnemyRobots;
    private static RobotInfo[] dangerousEnemyRobots;
    private static RobotInfo[] closeAllyRobots;

    private static void sense() throws GameActionException {
        // TODO only call rc.senseNearbyRobots once
        Team ownTeam = rc.getTeam();
        visibleAllyRobots = rc.senseNearbyRobots(-1, ownTeam);
        closeAllyRobots = rc.senseNearbyRobots(GameConstants.HEAL_RADIUS_SQUARED, ownTeam);
        Team enemyTeam = ownTeam.opponent();
        visibleEnemyRobots = rc.senseNearbyRobots(-1, enemyTeam);
        closeEnemyRobots = rc.senseNearbyRobots(GameConstants.ATTACK_RADIUS_SQUARED, enemyTeam);
        dangerousEnemyRobots = rc.senseNearbyRobots(ATTACK_RADIUS_PLUS_ONE_SQUARED, enemyTeam);
    }

    // TODO: decide on more factors (ex. health, number of friendly/enemy units nearby, etc...)
//    private static boolean shouldChase(RobotInfo target) {
//        if (target.hasFlag()) return true;
//        if (rc.getHealth() <= RETREAT_HEALTH_THRESHOLD) return false;
//        if (target.getHealth() <= rc.getAttackDamage()) return true;
//        int numHealthyAllies = 0;
//        for (RobotInfo ally : visibleAllyRobots) {
//            if (ally.getHealth() > RETREAT_HEALTH_THRESHOLD) numHealthyAllies++;
//        }
//        return numHealthyAllies >= 1.6 * visibleEnemyRobots.length;
//    }

    // TODO: take location as parameter instead and only move laterally if its closer
    /***
     * Moves in direction with some leniency
     * @param dir general direction to move in
     * @param strictness between 0 and 2
     * @throws GameActionException
     */
    private static void moveInDir(Direction dir, int strictness) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.move(dir);
            return;
        }
        if (strictness >= 2) return;

        Direction dirL = dir.rotateLeft();
        Direction dirR = dir.rotateRight();
        if (rc.canMove(dirL)) rc.move(dirL);
        else if (rc.canMove(dirR)) rc.move(dirR);

        if (strictness == 1) return;

        Direction dirLL = dirL.rotateLeft();
        Direction dirRR = dirR.rotateRight();
        if (rc.canMove(dirLL)) rc.move(dirLL);
        else if (rc.canMove(dirRR)) rc.move(dirRR);
    }

    private static int getNumAttackableEnemies(MapLocation loc) {
        int numAttackableEnemies = 0;
        for (RobotInfo enemy : visibleEnemyRobots) {
            if (enemy.getLocation().isWithinDistanceSquared(loc, GameConstants.ATTACK_RADIUS_SQUARED)) {
                numAttackableEnemies++;
            }
        }
        return numAttackableEnemies;
    }

    // moves in first direction in dirs that minimizes number of attackable enemies
    private static void moveMinEnemies(Direction[] dirs) throws GameActionException {
        if (!rc.isMovementReady()) return;
        Direction bestDir = null;
        int minAttackableEnemies = 10000;
        MapLocation curLoc = rc.getLocation();
        for (Direction dir : dirs) {
            if(!rc.canMove(dir) && dir != Direction.CENTER) continue;
            int numAttackableEnemies = getNumAttackableEnemies(curLoc.add(dir));
            if (bestDir == null || numAttackableEnemies < minAttackableEnemies) {
                minAttackableEnemies = numAttackableEnemies;
                bestDir = dir;
            }
        }
        if (bestDir != null && bestDir != Direction.CENTER) rc.move(bestDir);
    }

    private static Direction[] dirOrder = {Direction.CENTER, Direction.NORTH, Direction.EAST, Direction.SOUTH,
            Direction.WEST, Direction.NORTHEAST, Direction.NORTHWEST, Direction.SOUTHWEST, Direction.SOUTHEAST};
    private static void moveAwayFromEnemy() throws GameActionException {
        moveMinEnemies(dirOrder);
    }

    private static void moveInDirMinEnemies(Direction dir) throws GameActionException {
        Direction[] dirs = {dir, dir.rotateLeft(), dir.rotateRight()};
        moveMinEnemies(dirs);
    }

    private static void escortFlag(FlagInfo flag) throws GameActionException {
        MapLocation flagLoc = flag.getLocation();
        MapLocation curLoc = rc.getLocation();
        Direction moveDir;
        if (curLoc.isWithinDistanceSquared(flagLoc, FLAG_ESCORT_RADIUS_SQUARED)) moveDir = flagLoc.directionTo(curLoc);
        else moveDir = curLoc.directionTo(flagLoc);

        moveInDir(moveDir, 1);
    }

    private static void tryMoveToFlag() throws GameActionException {
        // move towards dropped enemy flags and picked up friendly flags
        if (!rc.isMovementReady()) return;
        FlagInfo[] nearbyFlags = rc.senseNearbyFlags(-1);

        FlagInfo targetFlag = null;
        for (FlagInfo flag : nearbyFlags) {
            Team flagTeam = flag.getTeam();

            // chase picked up friendly flags
            if (flagTeam == rc.getTeam() && flag.isPickedUp()
                    && rc.getRoundNum() > GameConstants.SETUP_ROUNDS) {
                targetFlag = flag;
                break;
            }

            // move towards enemy flags
            if (flagTeam != rc.getTeam()) targetFlag = flag;
        }

        if (targetFlag == null) return;

        if (targetFlag.getTeam() != rc.getTeam() && targetFlag.isPickedUp()) {
            escortFlag(targetFlag);
            return;
        }

        MapLocation flagLoc = targetFlag.getLocation();

        if (rc.canPickupFlag(flagLoc)) {
            rc.pickupFlag(flagLoc);
            FlagRecorder.setPickedUp(targetFlag.getID());
        } else {
            Direction moveDir = rc.getLocation().directionTo(flagLoc);
            if (moveDir != Direction.CENTER) moveInDir(moveDir, 1);
        }
        sense();
    }

    private static int getAttackDamage(RobotInfo robot) {
        double mult = 1.0;
        switch (robot.getAttackLevel()) {
            case 0:
                break;
            case 1:
                mult = 1.05;
                break;
            case 2:
                mult = 1.07;
                break;
            case 3:
                mult = 1.1;
                break;
            case 4:
                mult = 1.3;
                break;
            case 5:
                mult = 1.35;
                break;
            case 6:
                mult = 1.6;
                break;
        }
        return (int) Math.round(mult * BASE_ATTACK_DAMAGE);
    }

    private static RobotInfo selectAttackTarget() {

        // pick target we think we can kill in 1 hit,
        // if there are multiple, break ties with sum of attack and heal spec
        // if there are none, pick the enemy that we think we can kill in the fewest turns
        //  considering position of friendly units and their attack lvl. Break ties with spec again.
        RobotInfo target = null;
        boolean canOneShot = false;
        double minKillTime = 999999;
        for (RobotInfo enemy : closeEnemyRobots) {
            if (enemy.hasFlag()) { // is this correct? Another unit can immediately pick up flag
                target = enemy;
                break;
            }
            if (target == null) {
                target = enemy;
                continue;
            }

            if (enemy.getHealLevel() <= rc.getAttackDamage()) {
                if (canOneShot) {
                    int enemyLvlSum = enemy.getAttackLevel() + enemy.getHealLevel();
                    int tarLvlSum = target.getAttackLevel() + target.getHealLevel();
                    if (enemyLvlSum > tarLvlSum) target = enemy;
                } else {
                    target = enemy;
                    canOneShot = true;
                }
            }

            if (canOneShot) continue;

            MapLocation enemyLoc = enemy.getLocation();
            int damageSum = 0;
            for (RobotInfo friendly : visibleAllyRobots) {
                if (enemyLoc.isWithinDistanceSquared(friendly.getLocation(), GameConstants.ATTACK_RADIUS_SQUARED)) {
                    damageSum += getAttackDamage(friendly);
                }
            }
            double killTime = enemy.getHealth() / (double) damageSum;
            int enemyLvlSum = enemy.getAttackLevel() + enemy.getHealLevel();
            int tarLvlSum = target.getAttackLevel() + target.getHealLevel();
            if (killTime < minKillTime || (killTime == minKillTime && enemyLvlSum > tarLvlSum)) {
                minKillTime = killTime;
                target = enemy;
            }
        }

        return target;
    }

    private static void tryAttack() throws GameActionException {
        // attack lowest health enemy
        RobotInfo target = null;
        while (rc.isActionReady()) {
            target = selectAttackTarget();
            if (target == null) break;
            rc.attack(target.getLocation());
            sense();
        }

        if (rc.isMovementReady()) {
            moveAwayFromEnemy();
            sense();
        }
    }

    private static void tryHeal() throws GameActionException {
        if (!rc.isActionReady()) return;
        if (closeEnemyRobots.length > 0) return;
        if (dangerousEnemyRobots.length > 0 && rc.getID() % 3 != 0) return;

        RobotInfo target = null;
        for (RobotInfo ally : closeAllyRobots) {
            if (target == null || ally.getHealth() < target.getHealth()) {
                target = ally;
            }
        }

        if (target == null) return;

        MapLocation targetLoc = target.getLocation();
        if (rc.canHeal(targetLoc)) rc.heal(targetLoc);
    }

    private static void tryPlaceTrap() throws GameActionException {
        if (!rc.isActionReady()) return;

        // TODO: use ID instead of random?
        if (visibleEnemyRobots.length == 0 ||
                closeEnemyRobots.length > 0 ||
                Random.nextInt(3) == 0) return;

        MapLocation[] visibleEnemyRobotLocations = Utils.robotInfoToMapLocArr(visibleEnemyRobots);
        MapLocation enemyCentroid = Utils.getCentroid(visibleEnemyRobotLocations);

        if (visibleEnemyRobots.length >= 4) return;
        trapInDir(rc.getLocation(), rc.getLocation().directionTo(enemyCentroid));

    }

    private static boolean trapInDir(MapLocation loc, Direction dir) throws GameActionException {
        Direction[] dirsTowards = Utils.getDirOrdered(dir);
        TrapType trapType = TrapType.STUN;

        // only consider the 3 directions towards the centroid
        for(int i = 0; i < 3; ++i) {
            MapLocation trapPoint = loc.add(dirsTowards[i]);
            if(rc.canBuild(trapType, trapPoint) && !adjacentToTrap(trapPoint)) {
                rc.build(trapType, trapPoint);
                return true;
            }
        }
        return false;
    }

    private static boolean adjacentToTrap(MapLocation loc) throws GameActionException {
        MapInfo[] adjacentToLoc = rc.senseNearbyMapInfos(loc, 2);
        for (MapInfo info : adjacentToLoc) {
            if (info.getTrapType() != TrapType.NONE) return true;
        }
        return false;
    }

    private static void tryAdvance() throws GameActionException {
        if (rc.getRoundNum() % 2 != 0 || closeEnemyRobots.length > 0
                || visibleEnemyRobots.length ==0 || !rc.isActionReady()) return;

        int numHealthyAllies = 0;
        int numHealthyCloseAllies = 0;

        for (RobotInfo ally : visibleAllyRobots) {
            if (ally.getHealth() < RETREAT_HEALTH_THRESHOLD) continue;
            numHealthyAllies++;
            if (ally.getLocation().isWithinDistanceSquared(rc.getLocation(), ATTACK_RADIUS_PLUS_ONE_SQUARED)) {
                numHealthyCloseAllies++;
            }
        }

        MapLocation curLoc = rc.getLocation();
        MapLocation[] triggeredTraps = TrapTracker.updateAndReturnTriggeredTraps();

        if (triggeredTraps.length > 0) {
            moveInDir(curLoc.directionTo(triggeredTraps[0]), 1);
        }

        // we only move forward if we slightly outnumber the enemy at close range
        // or if we greatly outnumber them at long range
        // TODO: try tuning these a bit
        if (numHealthyAllies < 2 * visibleEnemyRobots.length
                && numHealthyCloseAllies < dangerousEnemyRobots.length + 2) return;

        MapLocation[] enemyLocs = new MapLocation[visibleEnemyRobots.length];
        for (int i = 0; i < visibleEnemyRobots.length; ++i) {
            enemyLocs[i] = visibleEnemyRobots[i].getLocation();
        }
        MapLocation enemyCentroid = Utils.getCentroid(enemyLocs);
        Direction dirToCentroid = rc.getLocation().directionTo(enemyCentroid);
        moveMinEnemies(new Direction[] {dirToCentroid, dirToCentroid.rotateLeft(), dirToCentroid.rotateRight()});
        sense();

    }

//    public static void tryFollowLeader(MapLocation rushLoc) throws GameActionException {
//        sense();
//
//        if (closeAllyRobots.length >= 3) return;
//
//        RobotInfo leader = null;
//        int minDisSq = 999999;
//        for (RobotInfo friendly : visibleAllyRobots) {
//            int disSq = friendly.getLocation().distanceSquaredTo(rushLoc);
//            if (disSq < minDisSq) {
//                leader = friendly;
//            }
//        }
//        if (leader == null) return;
//
//        moveInDir(rc.getLocation().directionTo(leader.getLocation()), 1);
//
//    }

    public static boolean inCombat() throws GameActionException {
        sense();

        // can prob add more conditions

        // do we think any enemies can move into attack radius (full check uses too much bytecode)
        MapLocation curLoc = rc.getLocation();
        for (RobotInfo enemy : dangerousEnemyRobots) {
            Direction dirToEnemy = curLoc.directionTo(enemy.getLocation());
            if (rc.senseMapInfo(curLoc.add(dirToEnemy)).isPassable()) return true;
        }
        return false;
    }

    public static void run() throws GameActionException {
        if (rc.hasFlag()) return;

        sense();
        tryAdvance();
        tryAttack();

        // TODO: remove from micro?
        tryMoveToFlag();

        tryPlaceTrap();
        tryAttack();
        tryHeal();
    }
}
