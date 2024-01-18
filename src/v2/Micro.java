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
    private static boolean shouldMoveTowards(RobotInfo target) {
        if (target.hasFlag()) return true;
        if (rc.getHealth() <= RETREAT_HEALTH_THRESHOLD) return false;
        int numHealthyAllies = 0;
        for (RobotInfo ally : visibleAllyRobots) {
            if (ally.getHealth() > RETREAT_HEALTH_THRESHOLD) numHealthyAllies++;
        }
        return numHealthyAllies >= 2 * visibleEnemyRobots.length;
    }

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
    }

    private static RobotInfo selectAttackTarget() {
        RobotInfo target = null;
        for (RobotInfo enemy : closeEnemyRobots) {
            if (target == null || enemy.getHealth() < target.getHealth()) {
                target = enemy;
            }

            if (enemy.hasFlag()) {
                target = enemy;
                break;
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

        if (!rc.isMovementReady()) return;

        MapLocation curLoc = rc.getLocation();
        if (target != null && shouldMoveTowards(target)) {
            Direction dirToTarget = curLoc.directionTo(target.getLocation());
            moveInDirMinEnemies(dirToTarget);
        } else moveAwayFromEnemy();
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

        TrapType trapType = TrapType.EXPLOSIVE;
//        if (visibleAllyRobots.length > 3 || rc.getCrumbs() < TrapType.EXPLOSIVE.buildCost) {
//            trapType = TrapType.STUN;
//        } else trapType = TrapType.EXPLOSIVE;

        if (rc.canBuild(trapType, rc.getLocation())) rc.build(trapType, rc.getLocation());

    }

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
        tryAttack();

        // TODO: remove from micro?
        tryMoveToFlag();

        sense(); // might have moved
        tryPlaceTrap();

        tryAttack();
        tryHeal();
    }
}
