package v2;

import battlecode.common.*;

public class Constants {

    public static RobotController rc;

    static final Direction[] DIRECTIONS = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };
    static final int FLAG_PICKUP_DIS_SQUARED = 2;
    static final int ATTACK_RADIUS_PLUS_ONE_SQUARED = 9;
    static final int BASE_ATTACK_DMG = 150;

    static final GlobalUpgrade FIRST_UPGRADE = GlobalUpgrade.ATTACK;
    static final GlobalUpgrade SECOND_UPGRADE = GlobalUpgrade.HEALING;
    static final GlobalUpgrade THIRD_UPGRADE = GlobalUpgrade.CAPTURING;

    // weights for explore
    static final int EXPLORE_HIGH_WEIGHT_DIRECTION = 10;
    static final int EXPLORE_MID_WEIGHT_DIRECTION = 4;
    static final int EXPLORE_NUM_TRACKED_LOCATIONS = 10;
    static final int EXPLORE_MOVES_TO_TRACK_LOCATION = 5;

    // SETUP PHASE Comms indices
    static final int COMMS_MEETUP_LOCS = 0; // uses 0 - 2
    static final int COMMS_SETUP_CLEARED = 63; // uses 63
    static final int COMMS_UNIT_IDS = 62; // uses 13 - 62
    
    // MAIN PHASE Comms indices
    static final int COMMS_ENEMY_FLAG_LOCS_START_IND = 0; // uses 0 - 2
    static final int COMMS_ENEMY_FLAG_IDS_START_IND = 3; // uses 3 - 5
    static final int COMMS_ENEMY_FLAG_LAST_ROUND_CARRYING_START_IND = 6; // uses 6 - 8
    static final int COMMS_FLAG_RECORDER = 9; // uses 9
    static final int COMMS_FLAG_DISTRESS_FLAGS = 10; // uses 10 - 12
    static final int COMMS_FLAG_DISTRESS_LOCS = 13; // uses 13 - 15

    // role enum
    public static enum Role {
        GENERAL,
        SIGNAL
    }
}
