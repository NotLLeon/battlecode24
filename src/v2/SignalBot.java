package v2;

import battlecode.common.*;

import static v2.Constants.rc;
public class SignalBot extends Robot{

    public static MapLocation signalSpawnLoc = null;

    public static void tryBecomeSignalBot() throws GameActionException {
        FlagInfo[] flags = rc.senseNearbyFlags(0);
        System.out.println(rc.getLocation());
        if (flags.length > 0) {
            role = Constants.Role.SIGNAL;
            rc.setIndicatorString("SIGNAL");
            signalSpawnLoc = rc.getLocation();
        }
    }
    public static void scanAndSignal() {

    }
}
