package v0;

import battlecode.common.*;

public class Constants {

    static RobotController rc;

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

    // weights for explore
    static int EXPLORE_HIGH_WEIGHT_DIRECTION = 10;
    static int EXPLORE_MID_WEIGHT_DIRECTION = 4;
    static int EXPLORE_NUM_TRACKED_LOCATIONS = 10;
    static int EXPLORE_MOVES_TO_TRACK_LOCATION = 5;

}
