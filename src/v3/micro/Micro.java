package v3.micro;

import battlecode.common.*;
import v3.*;

import static v3.Constants.*;

public class Micro {

    private static final int FLAG_ESCORT_RADIUS_SQUARED = 4;
    private static RobotInfo[] visibleFriendlyRobots;
    private static RobotInfo[] visibleEnemyRobots;
    private static RobotInfo[] immediateEnemyRobots;
    private static RobotInfo[] immediateFriendlyRobots;
    private static RobotInfo[] closeEnemyRobots;
    private static RobotInfo[] closeFriendlyRobots;
    private static FlagInfo[] nearbyFlags;
    private static int lastRoundRun = 0;
    private static MapLocation lastLocRun = null;
    private static final int RETREAT_HEALTH_THRESHOLD = 250;

    private static void attack(MapLocation loc) throws GameActionException {
        Robot.attack(loc);
        senseEnemies();
    }

    private static void move(Direction dir) throws GameActionException {
        rc.move(dir);
        sense();
        tryPickupFlag(); // do we always want to try pick up?
    }

    private static void heal(MapLocation loc) throws GameActionException {
        Robot.heal(loc);
        senseFriendlies();
    }

    private static void senseEnemies() throws GameActionException {
        Team enemyTeam = rc.getTeam().opponent();
        visibleEnemyRobots = rc.senseNearbyRobots(-1, enemyTeam);
        closeEnemyRobots = rc.senseNearbyRobots(ATTACK_RADIUS_PLUS_ONE_SQUARED, enemyTeam);
        immediateEnemyRobots = rc.senseNearbyRobots(GameConstants.ATTACK_RADIUS_SQUARED, enemyTeam);
    }

    private static void senseFriendlies() throws GameActionException {
        Team ownTeam = rc.getTeam();
        visibleFriendlyRobots = rc.senseNearbyRobots(-1, ownTeam);
        closeFriendlyRobots = rc.senseNearbyRobots(ATTACK_RADIUS_PLUS_ONE_SQUARED, ownTeam);
        immediateFriendlyRobots = rc.senseNearbyRobots(GameConstants.HEAL_RADIUS_SQUARED, ownTeam);
    }

    private static void sense() throws GameActionException {
        // TODO only call rc.senseNearbyRobots once
        senseFriendlies();
        senseEnemies();
        nearbyFlags = rc.senseNearbyFlags(-1);
    }

    // TODO: take location as parameter instead and only move laterally if its closer
    /***
     * Moves in direction with some leniency
     * @param dir general direction to move in
     * @param strictness between 0 and 2
     * @throws GameActionException
     */
    private static void moveInDir(Direction dir, int strictness) throws GameActionException {
        if (rc.canMove(dir)) {
            move(dir);
            return;
        }
        if (strictness >= 2) return;

        Direction dirL = dir.rotateLeft();
        Direction dirR = dir.rotateRight();
        if (rc.canMove(dirL)) move(dirL);
        else if (rc.canMove(dirR)) move(dirR);

        if (strictness == 1) return;

        Direction dirLL = dirL.rotateLeft();
        Direction dirRR = dirR.rotateRight();
        if (rc.canMove(dirLL)) move(dirLL);
        else if (rc.canMove(dirRR)) move(dirRR);
    }

    static boolean isStunned(int id) {
        return rc.getRoundNum() - TrapTracker.getLastStunnedRound(id) <= 2;
    }

    private static void escortFlag(FlagInfo flag) throws GameActionException {
        MapLocation flagLoc = flag.getLocation();
        MapLocation curLoc = rc.getLocation();
        Direction moveDir;
        if (curLoc.isWithinDistanceSquared(flagLoc, FLAG_ESCORT_RADIUS_SQUARED)) moveDir = flagLoc.directionTo(curLoc);
        else {
            moveDir = curLoc.directionTo(flagLoc);
            MapLocation[] fillDirs = {curLoc.add(moveDir)};
            for (MapLocation loc : fillDirs) {
                if (rc.canFill(loc)) rc.fill(loc);
            }
        }

        moveInDir(moveDir, 1);
    }

    private static void tryPickupFlag() throws GameActionException {
        if (!rc.isActionReady() || !MainPhase.getShouldPickUpFlag()) return;
        FlagInfo pickupFlag = null;
        for (FlagInfo flag : nearbyFlags) {
            MapLocation flagLoc = flag.getLocation();
            if (flag.getTeam() != rc.getTeam() && rc.canPickupFlag(flagLoc)) {
                pickupFlag = flag;
                break;
            }
        }
        if (pickupFlag == null) return;
        Robot.pickupFlag(pickupFlag.getLocation());
        int flagID = pickupFlag.getID();
        FlagRecorder.setPickedUp(flagID);
        if (!rc.hasFlag()) {
            FlagRecorder.setCaptured(flagID); // in case you capture by picking up
            MainPhase.clearSignal(pickupFlag);
        }
    }

    private static void tryMoveToFlag() throws GameActionException {
        // move towards dropped enemy flags and picked up friendly flags
        if (!rc.isMovementReady()) return;

        // changing this makes it worse
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
        Direction moveDir = rc.getLocation().directionTo(flagLoc);
        if (moveDir != Direction.CENTER) moveInDir(moveDir, 1);
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

        int damage = (int) Math.round(mult * BASE_ATTACK_DMG);

        // just assume everyone gets attack upgrade first
        if (rc.getRoundNum() >= GameConstants.GLOBAL_UPGRADE_ROUNDS) damage += GlobalUpgrade.ATTACK.baseAttackChange;

        return damage;
    }

    private static int getLevelSum(RobotInfo robot) {
        return robot.getAttackLevel() + robot.getHealLevel();
    }

    // TODO: try different algo
    private static RobotInfo selectAttackTarget() {

        // pick target we think we can kill in 1 hit,
        // if there are multiple, break ties with sum of attack and heal spec
        // if there are none, pick the enemy that we think we can kill in the fewest turns
        //  considering position of friendly units and their attack lvl. Break ties with spec again.
        RobotInfo target = null;
        boolean canOneShot = false;
        int minKillTime = INF;
        for (RobotInfo enemy : immediateEnemyRobots) {
            if (enemy.hasFlag()) { // is this correct? Another unit can immediately pick up flag
                target = enemy;
                break;
            }
            if (target == null) {
                target = enemy;
                continue;
            }

            if (enemy.getHealth() <= rc.getAttackDamage()) {
                if (canOneShot) {
                    if (getLevelSum(enemy) > getLevelSum(target)) target = enemy;
                } else {
                    target = enemy;
                    canOneShot = true;
                }
            }

            if (canOneShot) continue;

            MapLocation enemyLoc = enemy.getLocation();
            int damageSum = rc.getAttackDamage();
            int numFriendlyRobots = 1;
            for (RobotInfo friendly : visibleFriendlyRobots) {
                if (enemyLoc.isWithinDistanceSquared(friendly.getLocation(), GameConstants.ATTACK_RADIUS_SQUARED)) {
                    damageSum += getAttackDamage(friendly);
                    numFriendlyRobots++;
                }
            }
            // this is wrong but fixing it makes it worse
            int avgDmg = damageSum / numFriendlyRobots;
            int killTime = (enemy.getHealth() + avgDmg - 1) / avgDmg;
            if (killTime < minKillTime || (killTime == minKillTime && getLevelSum(enemy) > getLevelSum(target))) {
                minKillTime = killTime;
                target = enemy;
            }
        }

        return target;
    }

    private static void tryAttack() throws GameActionException {
        while (rc.isActionReady()) {
            RobotInfo target = selectAttackTarget();
            if (target == null) break;
            attack(target.getLocation());
        }
    }

    private static void tryHeal() throws GameActionException {
        if (!rc.isActionReady() || immediateEnemyRobots.length > 0) return;
        if (closeEnemyRobots.length > 0 && rc.getID() % 3 != 0) return;

        RobotInfo target = null;
        int minBaseHits = INF;
        for (RobotInfo friendly : immediateFriendlyRobots) {
            int baseHits = friendly.getHealth() / BASE_ATTACK_DMG;
            if (baseHits < minBaseHits
                    || (baseHits == minBaseHits && getLevelSum(friendly) > getLevelSum(target))) {
                target = friendly;
                minBaseHits = baseHits;
            }
        }

        if (target == null) return;

        MapLocation targetLoc = target.getLocation();
        if (rc.canHeal(targetLoc)) heal(targetLoc);
    }

    private static void tryPlaceTrap() throws GameActionException {
        if (!rc.isActionReady()) return;

        if (closeEnemyRobots.length == 0 || immediateEnemyRobots.length > 0 || visibleEnemyRobots.length < 6) return;

        MapLocation[] closeEnemyLocs = Utils.robotInfoToLocArr(closeEnemyRobots);
        MapLocation enemyCentroid = Utils.getCentroid(closeEnemyLocs);

        MapLocation curLoc = rc.getLocation();
        trapInDir(curLoc, curLoc.directionTo(enemyCentroid));
    }

    private static void trapInDir(MapLocation loc, Direction dir) throws GameActionException {
        Direction[] dirsTowards = Utils.getDirOrdered(dir);
        TrapType trapType = TrapType.STUN;

        // only consider the 3 directions towards the centroid
        for(int i = 0; i < 3; ++i) {
            MapLocation trapPoint = loc.add(dirsTowards[i]);
            if(rc.canBuild(trapType, trapPoint) && !adjacentToTrap(trapPoint)) {
                Robot.build(trapType, trapPoint);
                return;
            }
        }
    }

    private static boolean adjacentToTrap(MapLocation loc) throws GameActionException {
        for (Direction dir : DIRECTIONS) {
            MapLocation adjLoc = loc.add(dir);
            if (rc.canSenseLocation(adjLoc) && rc.senseMapInfo(adjLoc).getTrapType() != TrapType.NONE) return true;
        }
        return false;
    }

    static boolean aggressive;
    private static boolean shouldBeAggressive() {
        if (immediateEnemyRobots.length > 0
                || rc.getHealth() <= RETREAT_HEALTH_THRESHOLD || !rc.isActionReady()) return false;
        RobotInfo[] unstunnedVisibleEnemyRobots = Utils.filterRobotInfoArr(
                visibleEnemyRobots,
                (r) -> !isStunned(r.getID())
        );

        RobotInfo[] unstunnedCloseEnemyRobots = Utils.filterRobotInfoArr(
                closeEnemyRobots,
                (r) -> !isStunned(r.getID())
        );

        if (unstunnedCloseEnemyRobots.length > 0 && rc.getRoundNum() % 2 != 0 ) return false;

        int sumVisibleFriendlyHealth = 0;
        int sumVisibleEnemyHealth = 0;
        for (RobotInfo friendly : visibleFriendlyRobots) sumVisibleFriendlyHealth += friendly.getHealth();
        for (RobotInfo enemy : unstunnedVisibleEnemyRobots) sumVisibleEnemyHealth += enemy.getHealth();

        double avgFriendlyHealth = sumVisibleFriendlyHealth / (double) visibleFriendlyRobots.length;
        double avgEnemyHealth = sumVisibleEnemyHealth / (double) unstunnedVisibleEnemyRobots.length;

        boolean healthCond = visibleFriendlyRobots.length >= unstunnedVisibleEnemyRobots.length
                && avgFriendlyHealth >= 2 * avgEnemyHealth;
        boolean longRangeCond = visibleFriendlyRobots.length >= 2 * unstunnedVisibleEnemyRobots.length;
        boolean closeRangeCond = closeFriendlyRobots.length >= unstunnedCloseEnemyRobots.length + 2;

        return longRangeCond || closeRangeCond || healthCond;
    }

    private static final Direction[] dirOrder = {Direction.CENTER, Direction.NORTH, Direction.EAST, Direction.SOUTH,
            Direction.WEST, Direction.NORTHEAST, Direction.NORTHWEST, Direction.SOUTHWEST, Direction.SOUTHEAST};
    private static void tryMove() throws GameActionException {
        if (!rc.isMovementReady()) return;
        if (visibleEnemyRobots.length == 0) return;
        aggressive = shouldBeAggressive();
        MapLocation curLoc = rc.getLocation();
        DirInfo[] dirInfos = new DirInfo[9];
        for (int i = 9; --i >= 0;) {
            if (!rc.canMove(dirOrder[i])) continue;
            dirInfos[i] = new DirInfo(dirOrder[i], curLoc, visibleEnemyRobots, visibleFriendlyRobots);
        }

        DirInfo bestDirInfo = dirInfos[0];
        for (DirInfo dirInfo : dirInfos) {
            if (dirInfo == null) continue;
            if (bestDirInfo == null || dirInfo.isBetter(bestDirInfo)) bestDirInfo = dirInfo;
        }

        if (bestDirInfo == null) return;
        Direction moveDir = bestDirInfo.dir;
        if (moveDir != null && rc.canMove(moveDir)) move(moveDir);
    }

    // TODO: rethink this
    public static boolean inCombat() throws GameActionException {
        senseEnemies();
        // can prob add more conditions

        // do we think any enemies can move into attack radius (full check too expensive)
        MapLocation curLoc = rc.getLocation();
        for (RobotInfo enemy : closeEnemyRobots) {
            Direction dirToEnemy = curLoc.directionTo(enemy.getLocation());
            if (rc.senseMapInfo(curLoc.add(dirToEnemy)).isPassable()) return true;
        }
        return false;
    }

    private static void pickupCrumbs() throws GameActionException {
        if (visibleEnemyRobots.length > 0) return;
        MapLocation[] crumbLocs = rc.senseNearbyCrumbs(2);
        if (crumbLocs.length == 0) return;

        MapLocation curLoc = rc.getLocation();

        MapLocation nearestCrumb = crumbLocs[0];
        int minDist = curLoc.distanceSquaredTo(nearestCrumb);

        for (MapLocation crumb : crumbLocs) {
            int crumbDist =curLoc.distanceSquaredTo(crumb);
            if (crumbDist < minDist) {
                minDist = crumbDist;
                nearestCrumb = crumb;
            }
        }

        moveInDir(curLoc.directionTo(nearestCrumb), 2);
    }

    static boolean isDefaultFlagLoc(FlagInfo flag) throws GameActionException {
        MapLocation flagLoc = flag.getLocation();
        if (flag.getTeam() == rc.getTeam()) {
            for (MapLocation spawnCenter : Spawner.getSpawnCenters()) { // assumes we dont move flags
                if (flagLoc.equals(spawnCenter)) return true;
            }
            return false;
        } else {
            int flagInd = FlagRecorder.getFlagIdInd(flag.getID());
            MapLocation defaultLoc = FlagRecorder.getFlagLoc(flagInd);
            return flagLoc.equals(defaultLoc);
        }
    }

    public static void run() throws GameActionException {
        // if we already ran this round and didnt move, we dont need to run again
        MapLocation mapLoc = rc.getLocation();
        int roundNum = rc.getRoundNum();
        if (mapLoc.equals(lastLocRun) && roundNum == lastRoundRun) return;

        lastLocRun = mapLoc;
        lastRoundRun = roundNum;

        // must be called even if rest of micro doesnt run in order to have accurate stun timestamps
        TrapTracker.senseTriggeredTraps();

        if (!rc.isMovementReady() && !rc.isActionReady()) {
            TrapTracker.sensePlacedTraps();
            return;
        }

        // TODO: wtf? clean this up

        sense();
        tryPickupFlag();
        if (rc.hasFlag()) return;

        pickupCrumbs();

        if (Robot.getCooldown() + Robot.getBuildCooldown() < GameConstants.COOLDOWN_LIMIT) tryPlaceTrap();
        tryAttack();

        // TODO: seperate the deciding dir and actually moving so we can tell if theres attackable/healable units in new loc
        tryMoveToFlag();
        tryMove();

        if (Robot.getCooldown() + Robot.getBuildCooldown() < GameConstants.COOLDOWN_LIMIT) tryPlaceTrap();
        tryAttack();
        tryHeal();

        tryPlaceTrap();

        TrapTracker.sensePlacedTraps();
    }
}
