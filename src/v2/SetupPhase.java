package v2;

import battlecode.common.*;

import static v2.Constants.*;

// SETUP PHASE LOGIC GOES HERE (TENTATIVE)

public class SetupPhase extends Robot {

    private static final int GOTO_MEETUP_ROUND = 100;
    private static final int READ_IDS_ROUND = 100;
    private static boolean exploring = true;
    private static MapLocation meetLocation;
    private static boolean firstRun = true;
    private static int index = -1;

    private static void firstRun() throws GameActionException {
        Comms.write(COMMS_SETUP_CLEARED, 1);

        MapLocation position = rc.getLocation();

        // determine spawn center you spawned at
        MapLocation[] spawnCenters = Spawner.getSpawnCenters();
        if (spawnCenters[0].isAdjacentTo(position)) index = 0;
        else if (spawnCenters[1].isAdjacentTo(position)) index = 1;
        else index = 2;

        // write your ID to the shared array (backwards)
        for (int i = COMMS_UNIT_IDS + 1; --i >= 0;) {
            if (Comms.read(i) == 0) {
                Comms.write(i, rc.getID());
                break;
            }
        }

    }

    private static void readIDs() throws GameActionException {
        int[] orderedIDs = new int[GameConstants.ROBOT_CAPACITY];
        for (int i = 0; i < GameConstants.ROBOT_CAPACITY; ++i) {
            int commsInd = COMMS_UNIT_IDS - i;
            orderedIDs[i] = Comms.read(commsInd);
        }
        MainPhase.setTurnOrder(orderedIDs);
    }

    private static void endSetup() throws GameActionException {
        // only one unit should wipe comms
        if (Comms.read(COMMS_SETUP_CLEARED) == 0) return;


        // VERY VERY VERY EXPENSIVE - 75 * 64 = 4800 BYTECODE
        for (int i = GameConstants.SHARED_ARRAY_LENGTH; --i >= 0;) Comms.write(i, 0);
    }

    public static void run() throws GameActionException {
        if (firstRun) {
            firstRun();
            firstRun = false;
        }
        int curRound = rc.getRoundNum();

        if (curRound == READ_IDS_ROUND) readIDs();

        MapLocation[] crumbLocs = rc.senseNearbyCrumbs(-1);
        if (hasMeetupLoc() && curRound > GOTO_MEETUP_ROUND) {
            MapInfo[] adjMap = rc.senseNearbyMapInfos(2);
            boolean stopMoving = false;
            for (MapInfo info : adjMap) {
                if (info.isDam()) {
                    stopMoving = true;
                    break;
                }
            }
            if (!stopMoving) moveTo(meetLocation);
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
        if (curRound == GameConstants.SETUP_ROUNDS) endSetup();
    }

    public static void writeMeetupLoc(MapLocation damLoc) throws GameActionException {
        if (!hasMeetupLoc()) {
            Comms.writeLoc(COMMS_MEETUP_LOCS + index, damLoc);
            meetLocation = damLoc;
        }
    }

    public static boolean hasMeetupLoc() throws GameActionException {
        if (meetLocation != null) return true;
        MapLocation tryMeetup = Comms.readLoc(COMMS_MEETUP_LOCS + index);
        if (tryMeetup != null) {
            meetLocation = tryMeetup;
            return true;
        }
        return false;
    }

}
