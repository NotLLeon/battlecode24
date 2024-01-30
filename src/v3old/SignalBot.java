package v3old;

import battlecode.common.*;
import v3old.Constants.Role;

import static v3old.Constants.*;

public class SignalBot {
    private static RobotInfo[] nearbyEnemies;
    private static MapLocation curLoc;
    private static final Direction[] diagonals = {Direction.NORTHEAST, Direction.NORTHWEST, Direction.SOUTHEAST, Direction.SOUTHWEST};

    public static void run() throws GameActionException {
        scanAndSignal();
        RobotInfo target = null;
        int minHp = INF;
        for (RobotInfo bot : nearbyEnemies) {
            if (bot.getHealth() < minHp) {
                target = bot;
                minHp = bot.getHealth();
            }
        }
        if (target == null) return;
        if (rc.canAttack(target.getLocation())) Robot.attack(target.getLocation());
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

    private static MapLocation getClosestDiagonal(MapLocation loc) throws GameActionException {
        MapLocation closest = curLoc.add(Direction.NORTHWEST);
        for (Direction dir : diagonals) {
            if (curLoc.add(dir).distanceSquaredTo(loc) < closest.distanceSquaredTo(loc)) {
                closest = curLoc.add(dir);
            }
        }
        return closest;
    } 

    private static void placeTraps(RobotInfo[] nearbyBots) throws GameActionException {
        MapLocation[] closeEnemyLocs = Utils.robotInfoToLocArr(nearbyBots);
        MapLocation enemyCentroid = Utils.getCentroid(closeEnemyLocs);
        Direction[] dirsTowards = Utils.getDirOrdered(curLoc.directionTo(enemyCentroid));
        
        TrapType trapType = TrapType.STUN;

        // if (rc.canBuild(TrapType.WATER, getClosestDiagonal(enemyCentroid))) {
        //     Robot.build(TrapType.WATER, getClosestDiagonal(enemyCentroid));
        // }

        // only consider the 3 directions towards the centroid
        for(int i = 0; i < 3; ++i) {
            MapLocation trapPoint = curLoc.add(dirsTowards[i]);
            if(rc.canBuild(trapType, trapPoint)) {
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
        FlagInfo[] flag = rc.senseNearbyFlags(-1, rc.getTeam());
        FlagInfo[] ontopFlag = rc.senseNearbyFlags(0);
        if (flag.length == 0) clearSignal(curLoc);
        if (ontopFlag.length == 0) RobotPlayer.role = Role.GENERAL;
    }
}
