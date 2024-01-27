package v2;

import battlecode.common.*;

public class Constants {

    public static RobotController rc;

    public static final Direction[] DIRECTIONS = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };
    public static final int FLAG_PICKUP_DIS_SQUARED = 2;
    public static final int ATTACK_RADIUS_PLUS_ONE_SQUARED = 9;
    public static final int BASE_ATTACK_DMG = 150;
    public static final int INF = 999999999;

    public static final GlobalUpgrade FIRST_UPGRADE = GlobalUpgrade.ATTACK;
    public static final GlobalUpgrade SECOND_UPGRADE = GlobalUpgrade.HEALING;
    public static final GlobalUpgrade THIRD_UPGRADE = GlobalUpgrade.CAPTURING;

    // weights for explore
    public static final int EXPLORE_HIGH_WEIGHT_DIRECTION = 10;
    public static final int EXPLORE_MID_WEIGHT_DIRECTION = 4;
    public static final int EXPLORE_NUM_TRACKED_LOCATIONS = 10;
    public static final int EXPLORE_MOVES_TO_TRACK_LOCATION = 5;

    // SETUP PHASE Comms indices
    public static final int COMMS_MEETUP_LOCS = 0; // uses 0 - 2

    public static final int RESET_BIT = 63; // uses 63 

    // number of indices that we need to reset from 0
    public static final int SETUP_COMMS_INDICES = 3;
    
    // MAIN PHASE Comms indices
    public static final int COMMS_ENEMY_FLAG_LOCS_START_IND = 0; // uses 0 - 2
    public static final int COMMS_ENEMY_FLAG_IDS_START_IND = 3; // uses 3 - 5
    public static final int COMMS_ENEMY_FLAG_LAST_ROUND_CARRYING_START_IND = 6; // uses 6 - 8
    public static final int COMMS_FLAG_RECORDER = 9; // uses 9
    public static final int COMMS_FLAG_DISTRESS_FLAGS = 10; // uses 10 - 12
    public static final int COMMS_FLAG_DISTRESS_LOCS = 13; // uses 13 - 15
    public static final int COMMS_SIGNAL_BOT_DISTRESS_LOCS = 16; // uses 16 - 19
    public static final int COMMS_FLAG_ESCORT_REQUEST = 20; // uses 20 - 22
    public static final int COMMS_FLAG_ESCORT_REQUEST_COUNTER = 23; // 23 - 25

    // role enum
    public static enum Role {
        GENERAL,
        SIGNAL
    }
}
