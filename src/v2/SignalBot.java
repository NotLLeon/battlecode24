package v2;

import battlecode.common.*;
import v2.Constants.Role;

import static v2.Constants.*;
public class SignalBot{

    public static void run() throws GameActionException {
        SignalBot.scanAndSignal();

        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        RobotInfo target = null;
        for (RobotInfo bot : nearbyRobots) {
            if (target == null || bot.getHealth() < target.getHealth()) {
                target = bot;
            }

            if (bot.hasFlag()) {
                target = bot;
                break;
            }
        }
    }

    public static void tryBecomeSignalBot() throws GameActionException {
        FlagInfo[] flags = rc.senseNearbyFlags(0);
        if (flags.length > 0) Robot.role = Role.SIGNAL;
    }

    private static void scanAndSignal() throws GameActionException {
        RobotInfo[] nearbyBots = rc.senseNearbyRobots(-1, rc.getTeam());
        int sev = 0;
        int numEnemies = 0;
        for (RobotInfo bot : nearbyBots) {
            if (bot.getTeam() != rc.getTeam()) {
                numEnemies++;
            }
        }

        if ((float)numEnemies/(nearbyBots.length - numEnemies) > ENEMY_DISTRESS_RATIO) {
            sev = 1;
        }

        MapLocation curLoc = rc.getLocation();
        if (numEnemies == 0) clearSignal(curLoc);
        else setSignal(curLoc, sev);
    }

    public static boolean isDistressLoc(MapLocation loc) throws GameActionException {
        for (int i = 0; i < GameConstants.NUMBER_FLAGS; ++i) {
            if (loc.equals(Comms.readLoc(COMMS_SIGNAL_BOT_DISTRESS_LOCS + i))) return true;
        }
        return false;
    }

    public static MapLocation getHighestPrioDistress(MapLocation loc) throws GameActionException {
        MapLocation nearestLoc = null;
        int highestSev = 0;
        for (int i = 0; i < GameConstants.NUMBER_FLAGS; ++i) {
            MapLocation curLoc = Comms.readLoc(COMMS_SIGNAL_BOT_DISTRESS_LOCS + i);
            int sev = Comms.read(COMMS_SIGNAL_BOT_DISTRESS_LEVEL + i);
            if (nearestLoc == null || 
                (nearestLoc.distanceSquaredTo(loc) < curLoc.distanceSquaredTo(loc) && sev >= highestSev)) {
                nearestLoc = curLoc;
            } else if (sev > highestSev) {
                nearestLoc = curLoc;
                highestSev = sev;
            }
        }
        return nearestLoc;
    }

    public static int readSignalDistressLevel(MapLocation loc) throws GameActionException {
        for (int i = 0; i < GameConstants.NUMBER_FLAGS; ++i) {
            MapLocation curLoc = Comms.readLoc(COMMS_SIGNAL_BOT_DISTRESS_LOCS + i);
            if (curLoc.equals(loc)) return Comms.read(COMMS_SIGNAL_BOT_DISTRESS_LEVEL + i);
        }
        return 0;
    }

    private static void setSignal(MapLocation loc, int sev) throws GameActionException {
        MapLocation[] commLocs = new MapLocation[GameConstants.NUMBER_FLAGS];
        for (int i = 0; i < GameConstants.NUMBER_FLAGS; ++i) {
            commLocs[i] = Comms.readLoc(COMMS_SIGNAL_BOT_DISTRESS_LOCS + i);
            if (loc.equals(commLocs[i])) return;
        }

        for (int i = 0; i < GameConstants.NUMBER_FLAGS; ++i) {
            if (commLocs[i] == null) {
                Comms.writeLoc(COMMS_SIGNAL_BOT_DISTRESS_LOCS + i, loc);
                Comms.write(COMMS_SIGNAL_BOT_DISTRESS_LEVEL + i, sev);
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

}
