package v1;

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
    static int HIGH_WEIGHT_DIRECTION = 10;
    static int MID_WEIGHT_DIRECTION = 4;


    // limits for explore
    static int NUM_TRACKED_LOCATIONS = 10;

    static int MOVES_TO_TRACK_LOCATION = 5;

}
