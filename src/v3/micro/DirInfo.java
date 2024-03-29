package v3.micro;

import battlecode.common.*;
import v3.Utils;

import static v3.Constants.*;

// stores info about each valid direction for micro
// number of attackable enemies, highest prio attackable enemy, info about each attackable enemy

public class DirInfo {
    Direction dir;
    MapLocation dirLoc;
    EnemyInfo[] attackableEnemies;
    int highestPrio = -1;
    int minDisToEnemy = INF;
    int minDisToFriendly = INF;
    int disToFlagCarrier = INF;
    int dangerScore = 0;

    public DirInfo(Direction dir, MapLocation curLoc, RobotInfo[] enemies, RobotInfo[] friendlies) {
        dirLoc = curLoc.add(dir);
        this.dir = dir;
        RobotInfo[] enemyInfos = Utils.filterRobotInfoArr(
                enemies,
                (r) -> r.getLocation().isWithinDistanceSquared(dirLoc, GameConstants.ATTACK_RADIUS_SQUARED)
        );
        attackableEnemies = new EnemyInfo[enemyInfos.length];
        for (int i = attackableEnemies.length; --i >= 0;) {
            attackableEnemies[i] = new EnemyInfo(
                    enemyInfos[i],
                    friendlies,
                    Micro.isStunned(enemyInfos[i].getID())
            );
        }
        for (EnemyInfo enemy : attackableEnemies) {
            if (enemy.priority > highestPrio) highestPrio = enemy.priority;

            // what if you can oneshot the enemy? Also they might be seeing the same friendly
            if (enemy.numAttackableFriendlies == 0 && !enemy.isStunned) ++dangerScore;
        }

        for (RobotInfo enemy : enemies) {
            int dis = enemy.getLocation().distanceSquaredTo(dirLoc);
            if (dis < minDisToEnemy) minDisToEnemy = dis;
            if (enemy.hasFlag()) disToFlagCarrier = dis;
        }

        for (RobotInfo friendly : friendlies) {
            int dis = friendly.getLocation().distanceSquaredTo(dirLoc);
            if (dis < minDisToFriendly) minDisToFriendly = dis;
        }
    }

    public boolean isBetter(DirInfo other) {
        if (disToFlagCarrier < other.disToFlagCarrier) return true;
        if (disToFlagCarrier > other.disToFlagCarrier) return false;

        if (Micro.aggressive) {
            if (minDisToEnemy < other.minDisToEnemy) return true;
            if (minDisToEnemy > other.minDisToEnemy) return false;
        }

        if (dangerScore < other.dangerScore) return true;
        if (dangerScore > other.dangerScore) return false;

        if (rc.isActionReady()) {
            if (highestPrio > other.highestPrio) return true;
            if (highestPrio < other.highestPrio) return false;
        }

        if (minDisToFriendly < other.minDisToFriendly) return true;
        if (minDisToFriendly > other.minDisToFriendly) return false;

        return minDisToEnemy >= other.minDisToEnemy;
    }
}
