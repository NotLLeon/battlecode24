package v2.micro;

import battlecode.common.*;

public class EnemyInfo {
    RobotInfo info;
    MapLocation loc;
    boolean isStunned;
    int numAttackableFriendlies;
    int priority;
    public EnemyInfo(RobotInfo info, RobotInfo[] friendlies, boolean isStunned) {
        this.info = info;
        loc = info.getLocation();
        this.isStunned = isStunned;
        priority = GameConstants.DEFAULT_HEALTH - info.getHealth(); // TODO: consider other factors
        numAttackableFriendlies = 0;
        for (RobotInfo friendly : friendlies) {
            if (friendly.getLocation().isWithinDistanceSquared(this.loc, GameConstants.ATTACK_RADIUS_SQUARED)) {
                numAttackableFriendlies++;
            }
        }
    }

}
