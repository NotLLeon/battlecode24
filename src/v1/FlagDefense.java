package v1;

import battlecode.common.*;

import static v1.Constants.rc;
import v1.Constants.Role;

public class FlagDefense extends Robot{
    
    public static void scanAndSignal() throws GameActionException {
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam());
        if (nearbyEnemies.length > 1) {
            Comms.setDistress(rc.getLocation());
        } else {
            Comms.stopDistress();
        }
    }

    public static void checkDistress() throws GameActionException {
        MapLocation distress = Comms.readDistress();
        if (distress != null) {
            moveTo(distress);
        }
    }
}
