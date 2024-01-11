package v0;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.TrapType;

import static v0.Constants.rc;
import static v0.Random.nextDir;
import static v0.Random.rng;

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
                Direction dir = rc.getLocation().directionTo(patrolLoc);
                if (rc.canMove(dir)) rc.move(dir);
            }
            
            updateEnemyRobots();

            healAllies();

            // Move and attack randomly if no objective.
            rc.setIndicatorDot(patrolLoc, 0, 0, 0);
            moveToOutsideRadius(rc, patrolLoc, 2);

            // if (rc.canAttack(nextLoc)){
            //     rc.attack(nextLoc);
            //     System.out.println("Take that! Damaged an enemy that was in our way!");
            // }

            // Rarely attempt placing traps behind the robot.
            MapLocation prevLoc = rc.getLocation().subtract(nextDir());
            if (rc.canBuild(TrapType.EXPLOSIVE, prevLoc))
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
                if (rc.canAttack(enemyLocations[i])) rc.attack(enemyLocations[i]);
            }
            // Let the rest of our team know how many enemy robots we see!
            if (rc.canWriteSharedArray(0, enemyRobots.length)){
                rc.writeSharedArray(0, enemyRobots.length);
                int numEnemies = rc.readSharedArray(0);
            }
        }
    }

    public static void healAllies() throws GameActionException{
        // Sensing methods can be passed in a radius of -1 to automatically 
        // use the largest possible value.
        RobotInfo[] allyRobots = rc.senseNearbyRobots(-1, rc.getTeam());
        if (allyRobots.length != 0){
            rc.setIndicatorString("There are nearby enemy robots! Scary!");
            // Save an array of locations with enemy robots in them for future use.
            MapLocation[] allyLocations = new MapLocation[allyRobots.length];
            for (int i = 0; i < allyRobots.length; i++){
                allyLocations[i] = allyRobots[i].getLocation();
                if (rc.canHeal(allyLocations[i])) rc.heal(allyLocations[i]);
            }
            // Let the rest of our team know how many enemy robots we see!
            if (rc.canWriteSharedArray(0, allyRobots.length)){
                rc.writeSharedArray(0, allyRobots.length);
                int numEnemies = rc.readSharedArray(0);
            }
        }
    }
}
