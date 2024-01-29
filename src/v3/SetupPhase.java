package v3;

import battlecode.common.*;
import v3.Constants.Role;

import static v3.Constants.*;

// SETUP PHASE LOGIC GOES HERE (TENTATIVE)

public class SetupPhase extends Robot {

    public static boolean exploring = true;
    public static MapLocation meetLocation;
    private static int spawnIndex = -1;

    public static void writeMeetupLoc(MapLocation damLoc) throws GameActionException {
        int writeIndex = COMMS_MEETUP_LOCS + spawnIndex;
        // depending on the spawn location, read the corresponding slot in the shared array
        if (!hasMeetup()) {
            Comms.writeLoc(writeIndex, damLoc);
            meetLocation = damLoc;
        } else {
            // can consider doing something else when it sees a dam tile
            meetLocation = Comms.readLoc(writeIndex);
        }
    }

    public static void setupMeetup() throws GameActionException {
        int writeIndex = COMMS_MEETUP_LOCS + spawnIndex;
        if (hasMeetup()) {
            meetLocation = Comms.readLoc(writeIndex);
        }
    }

    public static boolean hasMeetup() throws GameActionException {
        int writeIndex = COMMS_MEETUP_LOCS + spawnIndex;
        MapLocation tryMeetup = Comms.readLoc(writeIndex);
        if (tryMeetup == null) return false;
        return !(tryMeetup.x == 0 && tryMeetup.y == 0);
    }

    public static void init() throws GameActionException {
        MapLocation position = rc.getLocation();

        MapLocation[] spawnCenters = Spawner.getSpawnCenters();
        if (spawnCenters[0].isAdjacentTo(position)) spawnIndex = 0;
        else if (spawnCenters[1].isAdjacentTo(position)) spawnIndex = 1;
        else if (spawnCenters[2].isAdjacentTo(position)) spawnIndex = 2;

        Comms.write(RESET_BIT, 1);
    }

    public static void clearComms() throws GameActionException {
        int dirty = Comms.read(RESET_BIT);

        if (dirty == 0) return;

        for (int i = 0; i < SETUP_COMMS_INDICES; i++) {
            Comms.write(i, 0);
        }
        Comms.write(RESET_BIT, 0);
    }


    public static void run() throws GameActionException {
        if (spawnIndex == -1) init();

        if (RobotPlayer.role == Role.SIGNAL) {
            SignalBot.run();
            return;
        }

        // for now just explore, try to path to crumbs, then if dam found, gather around dam
        // to prepare attack
        setupMeetup();

        MapLocation[] crumbLocs = rc.senseNearbyCrumbs(-1);
        if (hasMeetup() && rc.getRoundNum() > 100) {
            MapInfo[] adjMap = rc.senseNearbyMapInfos(2);

            MapInfo[] damTiles = Utils.filterMapInfoArr(adjMap, (i) -> i.isDam());
            boolean stopMoving = damTiles.length > 0;

            if (!stopMoving) moveTo(meetLocation);
            else {
                MapLocation[] damMapLocs = Utils.mapInfoToLocArr(damTiles);

                if (!rc.isActionReady()) return; 
                for (MapInfo info : adjMap) {
                    MapLocation adjMapLoc = info.getMapLocation();
                    int distSqNearest = adjMapLoc.distanceSquaredTo(damMapLocs[0]);

                    for (int i = 1; i < damMapLocs.length; ++i) {
                        distSqNearest = Math.min(adjMapLoc.distanceSquaredTo(damMapLocs[i]), distSqNearest);
                    }

                    if (info.isWater() && distSqNearest <= 2 && rc.canFill(adjMapLoc)) {
                        Robot.fill(adjMapLoc);
                        return;
                    }
                }
            }
        } else if (crumbLocs.length > 0) {
            moveTo(crumbLocs[0]);
        } else {
            MapInfo[] nearbyMap = rc.senseNearbyMapInfos();
            for (MapInfo info : nearbyMap) {
                if (info.isDam()) {
                    writeMeetupLoc(info.getMapLocation());
                    moveTo(meetLocation);
                    break;
                }
            }
            if (exploring) Explore.exploreNewArea();
        }
    }
}
