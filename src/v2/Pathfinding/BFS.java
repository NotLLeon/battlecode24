package v2.Pathfinding;

import battlecode.common.*;

import static v2.Constants.rc;

/**
 * "BFS"
 */
public class BFS {

    static Direction bestDir;
    static boolean fillAllowed = false;

    public static Direction getDir(MapLocation dest, boolean allowFill) throws GameActionException {
        fillAllowed = allowFill;
        MapLocation curLoc = rc.getLocation();
        bestDir = curLoc.directionTo(dest);
        return getDetourDir();
    }

    private static boolean isMoveable(MapLocation loc, boolean firstMove) throws GameActionException {
        if (!rc.canSenseLocation(loc))
            return false;
        MapInfo info = rc.senseMapInfo(loc);
        boolean canFill = fillAllowed && info.isWater();
        return (info.isPassable() || canFill)
                && (!firstMove || !rc.canSenseRobotAtLocation(loc));
    }

    private static Direction rotateInt(Direction dir, int rotate) {
        switch (rotate) {
            case 0:
                return dir;
            case 1:
                return dir.rotateRight();
            case -1:
                return dir.rotateLeft();
            case 2:
                return dir.rotateRight().rotateRight();
            case -2:
                return dir.rotateLeft().rotateLeft();
            case 3:
                return dir.rotateLeft().opposite();
            case -3:
                return dir.rotateRight().opposite();
            default:
                return dir.opposite();
        }
    }

    private static Direction getDetourDir() throws GameActionException {

        MapLocation curLoc = rc.getLocation();
        Direction firstDir;
        Direction curDir;
        MapLocation loc;
        curDir = rotateInt(bestDir, 0);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if (isMoveable(loc, true)) {
            return firstDir;
        }
        curDir = rotateInt(bestDir, 0);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if (isMoveable(loc, true)) {
            return firstDir;
        }
        curDir = rotateInt(bestDir, 1);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if (isMoveable(loc, true)) {
            curDir = rotateInt(bestDir, -1);
            loc = loc.add(curDir);
            if (isMoveable(loc, false)) {
                return firstDir;
            }
        }
        curDir = rotateInt(bestDir, -1);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if (isMoveable(loc, true)) {
            curDir = rotateInt(bestDir, 1);
            loc = loc.add(curDir);
            if (isMoveable(loc, false)) {
                return firstDir;
            }
        }
        curDir = rotateInt(bestDir, 1);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if (isMoveable(loc, true)) {
            curDir = rotateInt(bestDir, 0);
            loc = loc.add(curDir);
            if (isMoveable(loc, false)) {
                curDir = rotateInt(bestDir, -1);
                loc = loc.add(curDir);
                if (isMoveable(loc, false)) {
                    return firstDir;
                }
            }
        }
        curDir = rotateInt(bestDir, -1);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if (isMoveable(loc, true)) {
            curDir = rotateInt(bestDir, 0);
            loc = loc.add(curDir);
            if (isMoveable(loc, false)) {
                curDir = rotateInt(bestDir, 1);
                loc = loc.add(curDir);
                if (isMoveable(loc, false)) {
                    return firstDir;
                }
            }
        }
        curDir = rotateInt(bestDir, -2);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if (isMoveable(loc, true)) {
            curDir = rotateInt(bestDir, 0);
            loc = loc.add(curDir);
            if (isMoveable(loc, false)) {
                curDir = rotateInt(bestDir, 2);
                loc = loc.add(curDir);
                if (isMoveable(loc, false)) {
                    return firstDir;
                }
            }
        }
        curDir = rotateInt(bestDir, 2);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if (isMoveable(loc, true)) {
            curDir = rotateInt(bestDir, 0);
            loc = loc.add(curDir);
            if (isMoveable(loc, false)) {
                curDir = rotateInt(bestDir, -2);
                loc = loc.add(curDir);
                if (isMoveable(loc, false)) {
                    return firstDir;
                }
            }
        }
        curDir = rotateInt(bestDir, 1);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if (isMoveable(loc, true)) {
            curDir = rotateInt(bestDir, 0);
            loc = loc.add(curDir);
            if (isMoveable(loc, false)) {
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if (isMoveable(loc, false)) {
                    curDir = rotateInt(bestDir, -1);
                    loc = loc.add(curDir);
                    if (isMoveable(loc, false)) {
                        return firstDir;
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, -1);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if (isMoveable(loc, true)) {
            curDir = rotateInt(bestDir, 0);
            loc = loc.add(curDir);
            if (isMoveable(loc, false)) {
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if (isMoveable(loc, false)) {
                    curDir = rotateInt(bestDir, 1);
                    loc = loc.add(curDir);
                    if (isMoveable(loc, false)) {
                        return firstDir;
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, 1);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if (isMoveable(loc, true)) {
            curDir = rotateInt(bestDir, 1);
            loc = loc.add(curDir);
            if (isMoveable(loc, false)) {
                curDir = rotateInt(bestDir, -1);
                loc = loc.add(curDir);
                if (isMoveable(loc, false)) {
                    curDir = rotateInt(bestDir, -1);
                    loc = loc.add(curDir);
                    if (isMoveable(loc, false)) {
                        return firstDir;
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, -1);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if (isMoveable(loc, true)) {
            curDir = rotateInt(bestDir, -1);
            loc = loc.add(curDir);
            if (isMoveable(loc, false)) {
                curDir = rotateInt(bestDir, 1);
                loc = loc.add(curDir);
                if (isMoveable(loc, false)) {
                    curDir = rotateInt(bestDir, 1);
                    loc = loc.add(curDir);
                    if (isMoveable(loc, false)) {
                        return firstDir;
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, 2);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if (isMoveable(loc, true)) {
            curDir = rotateInt(bestDir, 0);
            loc = loc.add(curDir);
            if (isMoveable(loc, false)) {
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if (isMoveable(loc, false)) {
                    curDir = rotateInt(bestDir, -2);
                    loc = loc.add(curDir);
                    if (isMoveable(loc, false)) {
                        return firstDir;
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, -2);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if (isMoveable(loc, true)) {
            curDir = rotateInt(bestDir, 0);
            loc = loc.add(curDir);
            if (isMoveable(loc, false)) {
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if (isMoveable(loc, false)) {
                    curDir = rotateInt(bestDir, 2);
                    loc = loc.add(curDir);
                    if (isMoveable(loc, false)) {
                        return firstDir;
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, 1);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if (isMoveable(loc, true)) {
            curDir = rotateInt(bestDir, 0);
            loc = loc.add(curDir);
            if (isMoveable(loc, false)) {
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if (isMoveable(loc, false)) {
                    curDir = rotateInt(bestDir, 0);
                    loc = loc.add(curDir);
                    if (isMoveable(loc, false)) {
                        curDir = rotateInt(bestDir, -1);
                        loc = loc.add(curDir);
                        if (isMoveable(loc, false)) {
                            return firstDir;
                        }
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, -1);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if (isMoveable(loc, true)) {
            curDir = rotateInt(bestDir, 0);
            loc = loc.add(curDir);
            if (isMoveable(loc, false)) {
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if (isMoveable(loc, false)) {
                    curDir = rotateInt(bestDir, 0);
                    loc = loc.add(curDir);
                    if (isMoveable(loc, false)) {
                        curDir = rotateInt(bestDir, 1);
                        loc = loc.add(curDir);
                        if (isMoveable(loc, false)) {
                            return firstDir;
                        }
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, 2);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if (isMoveable(loc, true)) {
            curDir = rotateInt(bestDir, 0);
            loc = loc.add(curDir);
            if (isMoveable(loc, false)) {
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if (isMoveable(loc, false)) {
                    curDir = rotateInt(bestDir, 0);
                    loc = loc.add(curDir);
                    if (isMoveable(loc, false)) {
                        curDir = rotateInt(bestDir, -2);
                        loc = loc.add(curDir);
                        if (isMoveable(loc, false)) {
                            return firstDir;
                        }
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, -2);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if (isMoveable(loc, true)) {
            curDir = rotateInt(bestDir, 0);
            loc = loc.add(curDir);
            if (isMoveable(loc, false)) {
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if (isMoveable(loc, false)) {
                    curDir = rotateInt(bestDir, 0);
                    loc = loc.add(curDir);
                    if (isMoveable(loc, false)) {
                        curDir = rotateInt(bestDir, 2);
                        loc = loc.add(curDir);
                        if (isMoveable(loc, false)) {
                            return firstDir;
                        }
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, 1);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if (isMoveable(loc, true)) {
            curDir = rotateInt(bestDir, 1);
            loc = loc.add(curDir);
            if (isMoveable(loc, false)) {
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if (isMoveable(loc, false)) {
                    curDir = rotateInt(bestDir, -1);
                    loc = loc.add(curDir);
                    if (isMoveable(loc, false)) {
                        curDir = rotateInt(bestDir, -1);
                        loc = loc.add(curDir);
                        if (isMoveable(loc, false)) {
                            return firstDir;
                        }
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, -1);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if (isMoveable(loc, true)) {
            curDir = rotateInt(bestDir, -1);
            loc = loc.add(curDir);
            if (isMoveable(loc, false)) {
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if (isMoveable(loc, false)) {
                    curDir = rotateInt(bestDir, 1);
                    loc = loc.add(curDir);
                    if (isMoveable(loc, false)) {
                        curDir = rotateInt(bestDir, 1);
                        loc = loc.add(curDir);
                        if (isMoveable(loc, false)) {
                            return firstDir;
                        }
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, 2);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if (isMoveable(loc, true)) {
            curDir = rotateInt(bestDir, 2);
            loc = loc.add(curDir);
            if (isMoveable(loc, false)) {
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if (isMoveable(loc, false)) {
                    curDir = rotateInt(bestDir, -2);
                    loc = loc.add(curDir);
                    if (isMoveable(loc, false)) {
                        curDir = rotateInt(bestDir, -2);
                        loc = loc.add(curDir);
                        if (isMoveable(loc, false)) {
                            return firstDir;
                        }
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, -2);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if (isMoveable(loc, true)) {
            curDir = rotateInt(bestDir, -2);
            loc = loc.add(curDir);
            if (isMoveable(loc, false)) {
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if (isMoveable(loc, false)) {
                    curDir = rotateInt(bestDir, 2);
                    loc = loc.add(curDir);
                    if (isMoveable(loc, false)) {
                        curDir = rotateInt(bestDir, 2);
                        loc = loc.add(curDir);
                        if (isMoveable(loc, false)) {
                            return firstDir;
                        }
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, 3);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if (isMoveable(loc, true)) {
            curDir = rotateInt(bestDir, 1);
            loc = loc.add(curDir);
            if (isMoveable(loc, false)) {
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if (isMoveable(loc, false)) {
                    curDir = rotateInt(bestDir, -1);
                    loc = loc.add(curDir);
                    if (isMoveable(loc, false)) {
                        curDir = rotateInt(bestDir, -2);
                        loc = loc.add(curDir);
                        if (isMoveable(loc, false)) {
                            return firstDir;
                        }
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, -3);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if (isMoveable(loc, true)) {
            curDir = rotateInt(bestDir, -1);
            loc = loc.add(curDir);
            if (isMoveable(loc, false)) {
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if (isMoveable(loc, false)) {
                    curDir = rotateInt(bestDir, 1);
                    loc = loc.add(curDir);
                    if (isMoveable(loc, false)) {
                        curDir = rotateInt(bestDir, 2);
                        loc = loc.add(curDir);
                        if (isMoveable(loc, false)) {
                            return firstDir;
                        }
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, 3);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if (isMoveable(loc, true)) {
            curDir = rotateInt(bestDir, 1);
            loc = loc.add(curDir);
            if (isMoveable(loc, false)) {
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if (isMoveable(loc, false)) {
                    curDir = rotateInt(bestDir, -1);
                    loc = loc.add(curDir);
                    if (isMoveable(loc, false)) {
                        curDir = rotateInt(bestDir, -1);
                        loc = loc.add(curDir);
                        if (isMoveable(loc, false)) {
                            return firstDir;
                        }
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, -3);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if (isMoveable(loc, true)) {
            curDir = rotateInt(bestDir, -1);
            loc = loc.add(curDir);
            if (isMoveable(loc, false)) {
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if (isMoveable(loc, false)) {
                    curDir = rotateInt(bestDir, 1);
                    loc = loc.add(curDir);
                    if (isMoveable(loc, false)) {
                        curDir = rotateInt(bestDir, 1);
                        loc = loc.add(curDir);
                        if (isMoveable(loc, false)) {
                            return firstDir;
                        }
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, 1);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if (isMoveable(loc, true)) {
            curDir = rotateInt(bestDir, 0);
            loc = loc.add(curDir);
            if (isMoveable(loc, false)) {
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if (isMoveable(loc, false)) {
                    curDir = rotateInt(bestDir, 0);
                    loc = loc.add(curDir);
                    if (isMoveable(loc, false)) {
                        curDir = rotateInt(bestDir, 0);
                        loc = loc.add(curDir);
                        if (isMoveable(loc, false)) {
                            curDir = rotateInt(bestDir, -1);
                            loc = loc.add(curDir);
                            if (isMoveable(loc, false)) {
                                return firstDir;
                            }
                        }
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, -1);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if (isMoveable(loc, true)) {
            curDir = rotateInt(bestDir, 0);
            loc = loc.add(curDir);
            if (isMoveable(loc, false)) {
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if (isMoveable(loc, false)) {
                    curDir = rotateInt(bestDir, 0);
                    loc = loc.add(curDir);
                    if (isMoveable(loc, false)) {
                        curDir = rotateInt(bestDir, 0);
                        loc = loc.add(curDir);
                        if (isMoveable(loc, false)) {
                            curDir = rotateInt(bestDir, 1);
                            loc = loc.add(curDir);
                            if (isMoveable(loc, false)) {
                                return firstDir;
                            }
                        }
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, 2);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if (isMoveable(loc, true)) {
            curDir = rotateInt(bestDir, 0);
            loc = loc.add(curDir);
            if (isMoveable(loc, false)) {
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if (isMoveable(loc, false)) {
                    curDir = rotateInt(bestDir, 0);
                    loc = loc.add(curDir);
                    if (isMoveable(loc, false)) {
                        curDir = rotateInt(bestDir, 0);
                        loc = loc.add(curDir);
                        if (isMoveable(loc, false)) {
                            curDir = rotateInt(bestDir, -2);
                            loc = loc.add(curDir);
                            if (isMoveable(loc, false)) {
                                return firstDir;
                            }
                        }
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, -2);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if (isMoveable(loc, true)) {
            curDir = rotateInt(bestDir, 0);
            loc = loc.add(curDir);
            if (isMoveable(loc, false)) {
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if (isMoveable(loc, false)) {
                    curDir = rotateInt(bestDir, 0);
                    loc = loc.add(curDir);
                    if (isMoveable(loc, false)) {
                        curDir = rotateInt(bestDir, 0);
                        loc = loc.add(curDir);
                        if (isMoveable(loc, false)) {
                            curDir = rotateInt(bestDir, 2);
                            loc = loc.add(curDir);
                            if (isMoveable(loc, false)) {
                                return firstDir;
                            }
                        }
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, 1);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if (isMoveable(loc, true)) {
            curDir = rotateInt(bestDir, 1);
            loc = loc.add(curDir);
            if (isMoveable(loc, false)) {
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if (isMoveable(loc, false)) {
                    curDir = rotateInt(bestDir, 0);
                    loc = loc.add(curDir);
                    if (isMoveable(loc, false)) {
                        curDir = rotateInt(bestDir, -1);
                        loc = loc.add(curDir);
                        if (isMoveable(loc, false)) {
                            curDir = rotateInt(bestDir, -1);
                            loc = loc.add(curDir);
                            if (isMoveable(loc, false)) {
                                return firstDir;
                            }
                        }
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, -1);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if (isMoveable(loc, true)) {
            curDir = rotateInt(bestDir, -1);
            loc = loc.add(curDir);
            if (isMoveable(loc, false)) {
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if (isMoveable(loc, false)) {
                    curDir = rotateInt(bestDir, 0);
                    loc = loc.add(curDir);
                    if (isMoveable(loc, false)) {
                        curDir = rotateInt(bestDir, 1);
                        loc = loc.add(curDir);
                        if (isMoveable(loc, false)) {
                            curDir = rotateInt(bestDir, 1);
                            loc = loc.add(curDir);
                            if (isMoveable(loc, false)) {
                                return firstDir;
                            }
                        }
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, 2);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if (isMoveable(loc, true)) {
            curDir = rotateInt(bestDir, 2);
            loc = loc.add(curDir);
            if (isMoveable(loc, false)) {
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if (isMoveable(loc, false)) {
                    curDir = rotateInt(bestDir, 0);
                    loc = loc.add(curDir);
                    if (isMoveable(loc, false)) {
                        curDir = rotateInt(bestDir, -2);
                        loc = loc.add(curDir);
                        if (isMoveable(loc, false)) {
                            curDir = rotateInt(bestDir, -2);
                            loc = loc.add(curDir);
                            if (isMoveable(loc, false)) {
                                return firstDir;
                            }
                        }
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, -2);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if (isMoveable(loc, true)) {
            curDir = rotateInt(bestDir, -2);
            loc = loc.add(curDir);
            if (isMoveable(loc, false)) {
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if (isMoveable(loc, false)) {
                    curDir = rotateInt(bestDir, 0);
                    loc = loc.add(curDir);
                    if (isMoveable(loc, false)) {
                        curDir = rotateInt(bestDir, 2);
                        loc = loc.add(curDir);
                        if (isMoveable(loc, false)) {
                            curDir = rotateInt(bestDir, 2);
                            loc = loc.add(curDir);
                            if (isMoveable(loc, false)) {
                                return firstDir;
                            }
                        }
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, 1);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if (isMoveable(loc, true)) {
            curDir = rotateInt(bestDir, 1);
            loc = loc.add(curDir);
            if (isMoveable(loc, false)) {
                curDir = rotateInt(bestDir, 1);
                loc = loc.add(curDir);
                if (isMoveable(loc, false)) {
                    curDir = rotateInt(bestDir, -1);
                    loc = loc.add(curDir);
                    if (isMoveable(loc, false)) {
                        curDir = rotateInt(bestDir, -1);
                        loc = loc.add(curDir);
                        if (isMoveable(loc, false)) {
                            curDir = rotateInt(bestDir, -1);
                            loc = loc.add(curDir);
                            if (isMoveable(loc, false)) {
                                return firstDir;
                            }
                        }
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, -1);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if (isMoveable(loc, true)) {
            curDir = rotateInt(bestDir, -1);
            loc = loc.add(curDir);
            if (isMoveable(loc, false)) {
                curDir = rotateInt(bestDir, -1);
                loc = loc.add(curDir);
                if (isMoveable(loc, false)) {
                    curDir = rotateInt(bestDir, 1);
                    loc = loc.add(curDir);
                    if (isMoveable(loc, false)) {
                        curDir = rotateInt(bestDir, 1);
                        loc = loc.add(curDir);
                        if (isMoveable(loc, false)) {
                            curDir = rotateInt(bestDir, 1);
                            loc = loc.add(curDir);
                            if (isMoveable(loc, false)) {
                                return firstDir;
                            }
                        }
                    }
                }
            }
        }
        return Direction.CENTER;
    }
}