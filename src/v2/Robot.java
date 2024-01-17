package v2;


import battlecode.common.*;
import v2.Pathfinding.*;
import static v2.Constants.rc;

public abstract class Robot {

    public static void moveTo(MapLocation dest) throws GameActionException {
        Pathfinding.moveTo(dest, false, -1);
    }

    public static void moveToRadius(MapLocation dest, int radius) throws GameActionException {
        Pathfinding.moveTo(dest, false, radius);
    }

    public static void moveToAdjacent(MapLocation dest) throws GameActionException {
        Pathfinding.moveTo(dest, true, -1);
    }
    
    /***
     * Moves in direction with some leniency
     * @param dir general direction to move in
     * @param strictness between 0 and 2
     * @throws GameActionException
     */
    public static void moveInDir(Direction dir, int strictness) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.move(dir);
            return;
        }
        if (strictness >= 2) return;

        Direction dirL = dir.rotateLeft();
        Direction dirR = dir.rotateRight();
        if (rc.canMove(dirL)) rc.move(dirL);
        else if (rc.canMove(dirR)) rc.move(dirR);

        if (strictness == 1) return;

        Direction dirLL = dirL.rotateLeft();
        Direction dirRR = dirR.rotateRight();
        if (rc.canMove(dirLL)) rc.move(dirLL);
        else if (rc.canMove(dirRR)) rc.move(dirRR);
    }
}
