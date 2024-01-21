package v2;

import battlecode.common.*;

import static v2.Constants.rc;

public class Action {

    private static int cooldown = 0;
    private final static int BUILD_COOLDOWN = 5;

    public static int getCooldown() {
        return cooldown;
    }

    public static void decCooldown() {
        cooldown = Math.max(0,  cooldown - GameConstants.COOLDOWNS_PER_TURN);
    }

    public static void attack(MapLocation loc) throws GameActionException {
        cooldown += getAttackCooldown();
        rc.attack(loc);
    }

    public static void heal(MapLocation loc) throws GameActionException {
        cooldown += getHealCooldown();
        rc.heal(loc);
    }

    public static void build(TrapType trap, MapLocation loc) throws GameActionException {
        cooldown += getBuildCooldown();
        rc.build(trap, loc);
    }

    public static void fill(MapLocation loc) throws GameActionException {
        cooldown += (int) Math.round(GameConstants.FILL_COOLDOWN * getBuildCooldownMult());
        rc.fill(loc);
    }

    public static void dig(MapLocation loc) throws GameActionException {
        cooldown += (int) Math.round(GameConstants.DIG_COOLDOWN * getBuildCooldownMult());
        rc.dig(loc);
    }

    public static void pickupFlag(MapLocation loc) throws GameActionException {
        cooldown += GameConstants.PICKUP_DROP_COOLDOWN;
        rc.pickupFlag(loc);
    }

    public static void dropFlag(MapLocation loc) throws GameActionException {
        cooldown += GameConstants.PICKUP_DROP_COOLDOWN;
        rc.dropFlag(loc);
    }

    public static int getAttackCooldown() {
        return (int) Math.round(GameConstants.ATTACK_COOLDOWN * getAttackCooldownMult());
    }

    public static int getHealCooldown() {
        return (int) Math.round(GameConstants.HEAL_COOLDOWN * getHealCooldownMult());
    }

    public static int getBuildCooldown() {
        return (int) Math.round(BUILD_COOLDOWN * getBuildCooldownMult());
    }

    private static double getAttackCooldownMult() {
        switch (rc.getLevel(SkillType.ATTACK)) {
            case 0:
                return 1.0;
            case 1:
                return 0.95;
            case 2:
                return 0.93;
            case 3:
                return 0.9;
            case 4:
                return 0.8;
            case 5:
                return 0.65;
            default:
                return 0.4;
        }
    }

    private static double getBuildCooldownMult() {
        switch (rc.getLevel(SkillType.BUILD)) {
            case 0:
                return 1.0;
            case 1:
                return 0.95;
            case 2:
                return 0.9;
            case 3:
                return 0.85;
            case 4:
                return 0.8;
            case 5:
                return 0.7;
            default:
                return 0.5;
        }
    }

    private static double getHealCooldownMult() {
        switch (rc.getLevel(SkillType.HEAL)) {
            case 0:
                return 1.0;
            case 1:
                return 0.95;
            case 2:
                return 0.9;
            case 3:
            case 4:
            case 5:
                return 0.85;
            default:
                return 0.75;
        }
    }



}
