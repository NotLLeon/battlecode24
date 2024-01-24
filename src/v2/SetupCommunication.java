package v2;

import battlecode.common.*;
import v2.Comms;

import static v2.Constants.COMMS_MEETUP_LOCS;
import static v2.Constants.rc;

public class SetupCommunication {
    public static MapLocation meetLocation;
    private static int index = -1;

    public static void writeMeetupLoc(MapLocation damLoc) throws GameActionException {
        int writeIndex = COMMS_MEETUP_LOCS + index;
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
        int writeIndex = COMMS_MEETUP_LOCS + index;
        if (hasMeetup()) {
            meetLocation = Comms.readLoc(writeIndex);
        }
    }

    public static boolean hasMeetup() throws GameActionException {
        try {
            int writeIndex = COMMS_MEETUP_LOCS + index;
            MapLocation tryMeetup = Comms.readLoc(writeIndex);
            return !(tryMeetup.x == 0 && tryMeetup.y == 0);
        } catch (Exception e) {
            return false;
        }
    }

    public static void init() throws GameActionException {
        if (index != -1) return;

        MapLocation position = rc.getLocation();

        MapLocation[] spawnCenters = Spawner.getSpawnCenters();
        if (spawnCenters[0].isAdjacentTo(position)) index = 0;
        else if (spawnCenters[1].isAdjacentTo(position)) index = 1;
        else if (spawnCenters[2].isAdjacentTo(position)) index = 2;
        else {
            System.out.println("Invalid spawn center");
        }
    }

}
