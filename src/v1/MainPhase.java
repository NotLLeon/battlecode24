package v1;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.TrapType;

import static v1.Constants.rc;
import static v1.Constants.directions;
import static v1.Random.rng;

// MAIN PHASE STRATEGY HERE (TENTATIVE)
public class MainPhase {

    public static void run() throws GameActionException {
            if (rc.canPickupFlag(rc.getLocation())){
                rc.pickupFlag(rc.getLocation());
                rc.setIndicatorString("Holding a flag!");
            }
            // If we are holding an enemy flag, singularly focus on moving towards
            // an ally spawn zone to capture it! We use the check roundNum >= SETUP_ROUNDS
            // to make sure setup phase has ended.
            if (rc.hasFlag() && rc.getRoundNum() >= GameConstants.SETUP_ROUNDS){
                MapLocation[] spawnLocs = rc.getAllySpawnLocations();
                MapLocation firstLoc = spawnLocs[0];
                Direction dir = rc.getLocation().directionTo(firstLoc);
                if (rc.canMove(dir)) rc.move(dir);
            }
            // Move and attack randomly if no objective.
            Direction dir = directions[rng.nextInt(directions.length)];
            MapLocation nextLoc = rc.getLocation().add(dir);
            if (rc.canMove(dir)){
                rc.move(dir);
            }
            else if (rc.canAttack(nextLoc)){
                rc.attack(nextLoc);
                System.out.println("Take that! Damaged an enemy that was in our way!");
            }

            // Rarely attempt placing traps behind the robot.
            MapLocation prevLoc = rc.getLocation().subtract(dir);
            if (rc.canBuild(TrapType.EXPLOSIVE, prevLoc) && rng.nextInt() % 37 == 1)
                rc.build(TrapType.EXPLOSIVE, prevLoc);
            // We can also move our code into different methods or classes to better organize it!
            updateEnemyRobots();
    }

    public static void updateEnemyRobots() throws GameActionException{
        // Sensing methods can be passed in a radius of -1 to automatically 
        // use the largest possible value.
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (enemyRobots.length != 0){
            rc.setIndicatorString("There are nearby enemy robots! Scary!");
            // Save an array of locations with enemy robots in them for future use.
            MapLocation[] enemyLocations = new MapLocation[enemyRobots.length];
            for (int i = 0; i < enemyRobots.length; i++){
                enemyLocations[i] = enemyRobots[i].getLocation();
            }
            // Let the rest of our team know how many enemy robots we see!
            if (rc.canWriteSharedArray(0, enemyRobots.length)){
                rc.writeSharedArray(0, enemyRobots.length);
                int numEnemies = rc.readSharedArray(0);
            }
        }
    }
}
