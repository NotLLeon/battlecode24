package v1;

import static v1.Constants.directions;
import static v1.Constants.rc;
import static v1.Random.nextDir;
import static v1.Random.nextInt;
import static v1.Random.rng;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.TrapType;
import battlecode.common.MapInfo;

// MAIN PHASE STRATEGY HERE (TENTATIVE)
public class MainPhase extends Robot {

    static MapLocation[] spawnLocs = rc.getAllySpawnLocations();
    static MapLocation patrolLoc = spawnLocs[Math.abs(rng.nextInt() % spawnLocs.length)];

    public static void run() throws GameActionException {
        if (rc.canPickupFlag(rc.getLocation())){
            rc.pickupFlag(rc.getLocation());
            rc.setIndicatorString("Holding a flag!");
        }
        // If we are holding an enemy flag, singularly focus on moving towards
        // an ally spawn zone to capture it!
        if (rc.hasFlag()){
            moveTo(patrolLoc);
        }

        // Move and attack randomly if no objective.
        rc.setIndicatorDot(patrolLoc, 0, 0, 0);
        moveToOutsideRadius(patrolLoc, 2);

        // if (rc.canAttack(nextLoc)){
        //     rc.attack(nextLoc);
        //     System.out.println("Take that! Damaged an enemy that was in our way!");
        // }

        // Rarely attempt placing traps behind the robot.
        MapLocation prevLoc = rc.getLocation().subtract(nextDir());
        if (rc.canBuild(TrapType.EXPLOSIVE, prevLoc) && nextInt(2) == 0)
            rc.build(TrapType.EXPLOSIVE, prevLoc);
        // We can also move our code into different methods or classes to better organize it!
    }
}