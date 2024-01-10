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

    // indices for comms
    static int IDX_NUM_HQS = 63;
    static int IDX_NUM_MANA_WELLS = 61;
    static int IDX_NUM_AD_WELLS = 62;
    static int IDX_NUM_ISLANDS = 60;
    static int IDX_DISTRESS_SIGNAL = 59;
    static int IDX_TOTAL_MANA = 58;
    static int IDX_TOTAL_AD = 57;

    static int IDX_AVERAGE_MANA_REVENUE = 56;
    static int IDX_AVERAGE_AD_REVENUE = 55;
    static int IDX_POSSIBLE_SYMS = 54;

    //Temporary
    static int MAX_WELLS_STORED=20;
    static int MAX_AD_WELLS_STORED = 10;
    static int MAX_MANA_WELLS_STORED = 10;
    static int MAX_HQS_STORED=4;

    // Number of slots in the shared array is 2 times this number
    static int MAX_ISLANDS_STORED=15;

    //Resource Management
    static double ideal_ratio = 0.3; //Ideal quotient of Ad/(Ad+Mana)

}
