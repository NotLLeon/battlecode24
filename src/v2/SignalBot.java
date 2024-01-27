package v2;

import battlecode.common.*;
import v2.Constants.Role;

import static v2.Constants.*;

public class SignalBot {
    private static RobotInfo[] nearbyEnemies;
    private static MapLocation curLoc;

    public static void run() throws GameActionException {
        scanAndSignal();
    }

    public static void tryBecomeSignalBot() throws GameActionException {
        curLoc = rc.getLocation();
        FlagInfo[] flags = rc.senseNearbyFlags(0);
        if (flags.length > 0) {
            RobotPlayer.role = Role.SIGNAL;
        }
        scanAndSignal();
    }

    private static void setSignal(MapLocation loc) throws GameActionException {
        MapLocation[] commLocs = new MapLocation[GameConstants.NUMBER_FLAGS];
        for (int i = 0; i < GameConstants.NUMBER_FLAGS; ++i) {
            commLocs[i] = Comms.readLoc(COMMS_SIGNAL_BOT_DISTRESS_LOCS + i);
            if (loc.equals(commLocs[i])) return;
        }

        for (int i = 0; i < GameConstants.NUMBER_FLAGS; ++i) {
            if (commLocs[i] == null) {
                Comms.writeLoc(COMMS_SIGNAL_BOT_DISTRESS_LOCS + i, loc);
                return;
            }
        }
    }

    private static void clearSignal(MapLocation loc) throws GameActionException {
        for (int i = 0; i < GameConstants.NUMBER_FLAGS; ++i) {
            if (loc.equals(Comms.readLoc(COMMS_SIGNAL_BOT_DISTRESS_LOCS + i))) {
                Comms.write(COMMS_SIGNAL_BOT_DISTRESS_LOCS + i, 0);
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

    private static void placeTraps(RobotInfo[] nearbyBots) throws GameActionException {
        MapLocation[] closeEnemyLocs = Utils.robotInfoToLocArr(nearbyBots);
        MapLocation enemyCentroid = Utils.getCentroid(closeEnemyLocs);
        Direction[] dirsTowards = Utils.getDirOrdered(curLoc.directionTo(enemyCentroid));
        TrapType trapType = TrapType.STUN;

        // only consider the 3 directions towards the centroid
        for(int i = 0; i < 3; ++i) {
            MapLocation trapPoint = curLoc.add(dirsTowards[i]);
            if(rc.canBuild(trapType, trapPoint) && !adjacentToTrap(trapPoint)) {
                Robot.build(trapType, trapPoint);
                return;
            }
        }
    }

    private static void scanAndSignal() throws GameActionException {
        nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (nearbyEnemies.length == 0) clearSignal(curLoc);
        else {
            setSignal(curLoc);
            placeTraps(nearbyEnemies);
        }
    }
}
