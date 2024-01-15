package v2;

import battlecode.common.*;

import static v2.Constants.rc;

public class Micro {

    // ROUGH STRATEGY:
    // if you can see flag, move towards flag
    // if there are no enemys in vision radius, heal friendly (move toward and heal?)
    // if there is an enemy in attack range, attack, then move away or chase
    // if there is an enemy in vision radius, move away or closer, attack if possible
    // (friendly carrying flag?)
    // (group units?)

    // try not to move diagonally (messes up formation)

    private static RobotInfo[] visibleAllyRobots;
    private static RobotInfo[] visibleEnemyRobots;
    private static RobotInfo[] attackableEnemyRobots;
    private static RobotInfo[] healableAllyRobots;

    private static MapLocation lastLocUpdated;
    private static int lastRoundUpdated = 0;

    private static void lazySenseRobots() throws GameActionException {
        boolean locChanged =  lastLocUpdated == null || !lastLocUpdated.equals(rc.getLocation());
        boolean roundChanged = lastRoundUpdated != rc.getRoundNum();
        if (locChanged || roundChanged) {
            lastLocUpdated = rc.getLocation();
            lastRoundUpdated = rc.getRoundNum();
            senseRobots();
        }
    }

    private static void senseRobots() throws GameActionException {
        // TODO only call rc.senseNearbyRobots once
        Team ownTeam = rc.getTeam();
        visibleAllyRobots = rc.senseNearbyRobots(-1, ownTeam);
        healableAllyRobots = rc.senseNearbyRobots(GameConstants.HEAL_RADIUS_SQUARED, ownTeam);
        Team enemyTeam = ownTeam.opponent();
        visibleEnemyRobots = rc.senseNearbyRobots(-1, enemyTeam);
        attackableEnemyRobots = rc.senseNearbyRobots(GameConstants.ATTACK_RADIUS_SQUARED, enemyTeam);
    }

    // TODO: decide on more factors (ex. health, number of friendly/enemy units nearby, etc...)
    private static boolean shouldChase(RobotInfo target) {
        return target.hasFlag();
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

    /***
     * Moves away from moveAwayLoc, preferring cardinal movement over diagonal
     * @param moveAwayLoc
     */
    private static void moveAwayCardinal(MapLocation moveAwayLoc) throws GameActionException {
        if (!rc.isMovementReady()) return;
        MapLocation curLoc = rc.getLocation();
        Direction bestDir = null;
        MapLocation bestLoc = null;
        for (Direction dir : Direction.cardinalDirections()) {
            MapLocation newLoc = curLoc.add(dir);
            if ((bestDir == null || newLoc.distanceSquaredTo(moveAwayLoc) > bestLoc.distanceSquaredTo(moveAwayLoc)) &&
                    rc.canMove(dir)) {
                bestDir = dir;
                bestLoc = newLoc;
            }
        }

        if (bestDir == null) moveInDir(moveAwayLoc.directionTo(curLoc), 1);
        else rc.move(bestDir);
    }

    private static void tryMoveToFlag() throws GameActionException {
        // move towards dropped enemy flags and picked up friendly flags
        // TODO: move towards picked up enemy flags depending on macro
        // TODO: move towards dropped friendly flags not in default loc (how to tell default loc?)
        if (!rc.isMovementReady()) return;
        FlagInfo[] nearbyFlags = rc.senseNearbyFlags(-1);

        FlagInfo targetFlag = null;
        for (FlagInfo flag : nearbyFlags) {
            Team flagTeam = flag.getTeam();

            if (flagTeam == rc.getTeam() && flag.isPickedUp()
                    && rc.getRoundNum() > GameConstants.SETUP_ROUNDS) {
                targetFlag = flag;
                break;
            }

            if (flagTeam != rc.getTeam() && !flag.isPickedUp()) {
                targetFlag = flag;
            }
        }
        if (targetFlag == null) return;
        MapLocation flagLoc = targetFlag.getLocation();

        if (rc.canPickupFlag(flagLoc)) {
            rc.pickupFlag(flagLoc);
            FlagRecorder.setPickedUp(targetFlag.getID());
        } else {
            Direction moveDir = rc.getLocation().directionTo(flagLoc);
            if (moveDir != Direction.CENTER) moveInDir(moveDir, 1);
        }
    }

    private static void tryAttack() throws GameActionException {
        lazySenseRobots();

        // attack lowest health enemy
        RobotInfo target = null;
        for (RobotInfo enemy : attackableEnemyRobots) {
            if (target == null || enemy.getHealth() < target.getHealth()) {
                target = enemy;
            }

            if (enemy.hasFlag()) {
                target = enemy;
                break;
            }
        }

        if (target == null) return;
        MapLocation targetLoc = target.getLocation();
        while (rc.canAttack(targetLoc)) rc.attack(targetLoc);

        boolean allEnemiesDead = attackableEnemyRobots.length == 1 && target.getHealth() == 0;
        if (!rc.isMovementReady() || allEnemiesDead) return;

        // TODO: move away from centroid of remaining enemy positions
        MapLocation curLoc = rc.getLocation();
        if (shouldChase(target)) moveInDir(curLoc.directionTo(targetLoc), 1);
        else moveAwayCardinal(targetLoc);
    }

    private static void tryHeal() throws GameActionException {
        if (!rc.isActionReady()) return;
        lazySenseRobots();

        RobotInfo target = null;
        for (RobotInfo ally : healableAllyRobots) {
            if (target == null || ally.getHealth() < target.getHealth()) {
                target = ally;
            }
        }

        if (target == null) return;

        MapLocation targetLoc = target.getLocation();
        while (rc.canHeal(targetLoc)) rc.heal(targetLoc);
    }

    private static void tryPlaceTrap() throws GameActionException {
        if (!rc.isActionReady()) return;

        senseRobots();
        if (visibleEnemyRobots.length == 0 ||
                attackableEnemyRobots.length > 0 ||
                Random.nextInt(3) == 0) return;

        TrapType trapType = TrapType.EXPLOSIVE;
//        if (visibleAllyRobots.length > 3 || rc.getCrumbs() < TrapType.EXPLOSIVE.buildCost) {
//            trapType = TrapType.STUN;
//        } else trapType = TrapType.EXPLOSIVE;

        if (rc.canBuild(trapType, rc.getLocation())) rc.build(trapType, rc.getLocation());

    }
    public static void run() throws GameActionException {
        // TODO: prevent macro from moving closer to enemy for a few rounds after engaging
        if (rc.hasFlag()) return;
        tryAttack();
        tryMoveToFlag();
        tryPlaceTrap();
        tryAttack();
//        tryPlaceTrap();

        if (visibleEnemyRobots.length > 0) {
            // TODO: coordinated move-in on enemy position
        } else {
            tryHeal();
        }
    }

}
