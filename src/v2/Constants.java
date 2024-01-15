package v2;

import battlecode.common.*;

public class Constants {

    public static RobotController rc;

    static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
};
    static GlobalUpgrade FIRST_UPGRADE = GlobalUpgrade.ACTION;
    static GlobalUpgrade SECOND_UPGRADE = GlobalUpgrade.HEALING;

    // weights for explore
    static int EXPLORE_HIGH_WEIGHT_DIRECTION = 10;
    static int EXPLORE_MID_WEIGHT_DIRECTION = 4;
    static int EXPLORE_NUM_TRACKED_LOCATIONS = 10;
    static int EXPLORE_MOVES_TO_TRACK_LOCATION = 5;

    // Comms indices
    static int COMMS_ENEMY_FLAG_LOCS_START_IND = 0; // uses 0 - 2
    static int COMMS_ENEMY_FLAG_IDS_START_IND = 3; // uses 3 - 5
    static int COMMS_ENEMY_FLAG_LAST_ROUND_CARRYING_START_IND = 6; // uses 6 - 8
    static int COMMS_FLAG_RECORDER = 9; // uses 9
    static int COMMS_FLAG_DISTRESS_FLAGS = 10; // uses 10 - 12
    static int COMMS_FLAG_DISTRESS_LOCS = 13; // uses 13 - 15
    static int COMMS_FLAG_DISTRESS_LEVEL = 16; // uses 16 - 18

    // role enum
    public static enum Role {
        GENERAL,
        SIGNAL
    }
}
