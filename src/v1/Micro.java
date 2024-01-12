package v1;

import battlecode.common.*;

import static v1.Constants.rc;

public class Micro {

    // ROUGH STRATEGY:
    // if you can see flag, move towards flag
    // if there are no enemys in vision radius, heal friendly (move toward and heal?)
    // if there is an enemy in attack range, attack, then move away or chase
    // if there is an enemy in vision radius, move away or closer, attack if possible
    // (friendly carrying flag?)
    // (group units?)

    // in all micro movements, try not to move diagonally (messes up formation)
    //
    private static RobotInfo[] allyRobots;
    private static RobotInfo[] enemyRobots;
    private static MapLocation lastLocUpdated;
    private static int lastRoundUpdated;

    private static void lazySenseRobots() throws GameActionException {
        if (!lastLocUpdated.equals(rc.getLocation()) || lastRoundUpdated != rc.getRoundNum()) {
            lastLocUpdated = rc.getLocation();
            lastRoundUpdated = rc.getRoundNum();
            senseRobots();
        }
    }

    private static void senseRobots() throws GameActionException {
        allyRobots = rc.senseNearbyRobots(-1, rc.getTeam());
        enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
    }

    // TODO: decide on more factors (ex. health, number of friendly/enemy units nearby, etc...)
    private static boolean shouldChase(RobotInfo target) {
        return target.hasFlag();
    }

    private static void tryMoveToFlag() throws GameActionException {
        // move towards dropped enemy flags and picked up friendly flags
        // TODO: move towards picked up enemy flags depending on macro
        // TODO: move towards dropped friendly flags not in default loc (how to tell default loc?)
        if (!rc.isMovementReady()) return;
        FlagInfo[] nearbyFlags = rc.senseNearbyFlags(-1);

        MapLocation flagLoc = null;
        for (FlagInfo flag : nearbyFlags) {
            Team flagTeam = flag.getTeam();

            if (flagTeam == rc.getTeam() && flag.isPickedUp()
                    && rc.getRoundNum() > GameConstants.SETUP_ROUNDS) {
                flagLoc = flag.getLocation();
                break;
            }

            if (flagTeam != rc.getTeam() && !flag.isPickedUp()) {
                flagLoc = flag.getLocation();
            }
        }
        if (flagLoc == null) return;

        Direction moveDir = rc.getLocation().directionTo(flagLoc);
        if (rc.canMove(moveDir)) rc.move(moveDir);

    }

    private static void tryAttack() throws GameActionException {
        lazySenseRobots();

        // attack lowest health enemy
        RobotInfo target = null;
        for (RobotInfo enemy : enemyRobots) {
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

        if (!rc.isMovementReady()) return;

        // TODO: try not to move diagonally since it might mess up formation
        MapLocation curLoc = rc.getLocation();
        Direction moveDir;
        if (shouldChase(target)) {
            moveDir = curLoc.directionTo(targetLoc);
        } else {
            moveDir = targetLoc.directionTo(curLoc);
        }

        if (rc.canMove(moveDir)) rc.move(moveDir);
    }

    private static void tryHeal() throws GameActionException {
        if (!rc.isActionReady()) return;
        lazySenseRobots();

        RobotInfo target = null;
        for (RobotInfo ally : allyRobots) {
            if (target == null || ally.getHealth() < target.getHealth()) {
                target = ally;
            }
        }

        if (target == null) return;

        MapLocation targetLoc = target.getLocation();
        while (rc.canHeal(targetLoc)) rc.heal(targetLoc);
    }

    public static void run() throws GameActionException {
        // TODO: allow more lenient micro movement.
        //  If we want to go N but can't, moving NW or NE is prob fine
        tryAttack();
        tryMoveToFlag();
        tryAttack();
        if (enemyRobots.length > 0) {
            // TODO: coordinated move-in on enemy position
        } else {
            tryHeal();
        }

    }

}
