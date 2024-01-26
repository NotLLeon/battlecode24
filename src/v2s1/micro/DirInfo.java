package v2s1.micro;

import battlecode.common.*;
import v2s1.Utils;

// stores info about each valid direction for micro
// number of attackable enemies, highest prio attackable enemy, info about each attackable enemy

public class DirInfo {
    Direction dir;
    MapLocation dirLoc;
    EnemyInfo[] attackableEnemies;
    int highestPrio = -1;
    int minDisToEnemy = 99999999;
    int minDisToFriendly = 99999999;

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
                    friendlies
//                    isStunned(enemyInfos[i].getID())
            );
        }
        for (EnemyInfo enemy : attackableEnemies) {
            if (enemy.priority > highestPrio) highestPrio = enemy.priority;
        }

        for (RobotInfo enemy : enemies) {
            int dis = enemy.getLocation().distanceSquaredTo(dirLoc);
            if (dis < minDisToEnemy) minDisToEnemy = dis;
        }

        for (RobotInfo friendly : friendlies) {
            int dis = friendly.getLocation().distanceSquaredTo(dirLoc);
            if (dis < minDisToFriendly) minDisToFriendly = dis;
        }
    }

    public boolean isSafe() {
        for (EnemyInfo enemy : attackableEnemies) {
            // what if you can oneshot the enemy?
            if (enemy.numAttackableFriendlies == 0/* && !enemy.isStunned*/) return false;
        }
        return true;
    }
}
