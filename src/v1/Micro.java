package v1;

import battlecode.common.RobotInfo;

import static v1.Constants.rc;

public class Micro {

    // ROUGH STRATEGY:
    // if you see enemy carrying a flag, FOLLOW THEM
    // if there are no enemys in vision radius, heal friendly (move toward and heal?)
    // if there is an enemy in attack range, attack, then move away or chase
    // if there is an enemy in vision radius, move away or closer, attack if possible
    // (friendly carrying flag?)

    // in all micro movements, try not to move diagonally (messes up formation)
    //
    public static void run() {
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();

        for(RobotInfo info : nearbyRobots) {

        }

    }
}
