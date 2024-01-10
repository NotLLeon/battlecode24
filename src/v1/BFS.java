package v1;

import battlecode.common.*;


/**
 * "BFS"
 */
public class BFS {

    static Direction bestDir;

    public static Direction getDir(RobotController rc, MapLocation dest) throws GameActionException {
        MapLocation curLoc = rc.getLocation();
        bestDir = curLoc.directionTo(dest);
        return getDetourDir(rc);
    }

    private static Direction getDetourDir(RobotController rc) throws GameActionException {

        MapLocation curLoc = rc.getLocation();
        Direction firstDir;
        Direction curDir;
        MapLocation loc;

        curDir = rotateInt(bestDir, 0);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if(isMoveable(rc, loc, curDir, true)){
            return firstDir;
        }
        curDir = rotateInt(bestDir, 1);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, -1);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                return firstDir;
            }
        }
        curDir = rotateInt(bestDir, -1);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, 1);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                return firstDir;
            }
        }
        curDir = rotateInt(bestDir, 1);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, 0);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, -1);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    return firstDir;
                }
            }
        }
        curDir = rotateInt(bestDir, -1);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, 0);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, 1);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    return firstDir;
                }
            }
        }
        curDir = rotateInt(bestDir, -2);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, 0);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, 2);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    return firstDir;
                }
            }
        }
        curDir = rotateInt(bestDir, 2);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, 0);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, -2);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    return firstDir;
                }
            }
        }
        curDir = rotateInt(bestDir, 1);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, 0);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    curDir = rotateInt(bestDir, -1);
                    loc = loc.add(curDir);
                    if(isMoveable(rc, loc, curDir, false)){
                        return firstDir;
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, -1);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, 0);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    curDir = rotateInt(bestDir, 1);
                    loc = loc.add(curDir);
                    if(isMoveable(rc, loc, curDir, false)){
                        return firstDir;
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, 1);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, 1);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, -1);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    curDir = rotateInt(bestDir, -1);
                    loc = loc.add(curDir);
                    if(isMoveable(rc, loc, curDir, false)){
                        return firstDir;
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, -1);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, -1);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, 1);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    curDir = rotateInt(bestDir, 1);
                    loc = loc.add(curDir);
                    if(isMoveable(rc, loc, curDir, false)){
                        return firstDir;
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, 2);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, 0);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    curDir = rotateInt(bestDir, -2);
                    loc = loc.add(curDir);
                    if(isMoveable(rc, loc, curDir, false)){
                        return firstDir;
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, -2);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, 0);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    curDir = rotateInt(bestDir, 2);
                    loc = loc.add(curDir);
                    if(isMoveable(rc, loc, curDir, false)){
                        return firstDir;
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, 2);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, 1);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, -1);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    curDir = rotateInt(bestDir, -2);
                    loc = loc.add(curDir);
                    if(isMoveable(rc, loc, curDir, false)){
                        return firstDir;
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, -2);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, -1);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, 1);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    curDir = rotateInt(bestDir, 2);
                    loc = loc.add(curDir);
                    if(isMoveable(rc, loc, curDir, false)){
                        return firstDir;
                    }
                }
            }
        }
        if(Clock.getBytecodesLeft() < 3000) return Direction.CENTER;
        curDir = rotateInt(bestDir, 1);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, 0);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    curDir = rotateInt(bestDir, 0);
                    loc = loc.add(curDir);
                    if(isMoveable(rc, loc, curDir, false)){
                        curDir = rotateInt(bestDir, -1);
                        loc = loc.add(curDir);
                        if(isMoveable(rc, loc, curDir, false)){
                            return firstDir;
                        }
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, -1);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, 0);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    curDir = rotateInt(bestDir, 0);
                    loc = loc.add(curDir);
                    if(isMoveable(rc, loc, curDir, false)){
                        curDir = rotateInt(bestDir, 1);
                        loc = loc.add(curDir);
                        if(isMoveable(rc, loc, curDir, false)){
                            return firstDir;
                        }
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, 2);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, 0);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    curDir = rotateInt(bestDir, 0);
                    loc = loc.add(curDir);
                    if(isMoveable(rc, loc, curDir, false)){
                        curDir = rotateInt(bestDir, -2);
                        loc = loc.add(curDir);
                        if(isMoveable(rc, loc, curDir, false)){
                            return firstDir;
                        }
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, -2);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, 0);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    curDir = rotateInt(bestDir, 0);
                    loc = loc.add(curDir);
                    if(isMoveable(rc, loc, curDir, false)){
                        curDir = rotateInt(bestDir, 2);
                        loc = loc.add(curDir);
                        if(isMoveable(rc, loc, curDir, false)){
                            return firstDir;
                        }
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, 1);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, 1);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    curDir = rotateInt(bestDir, -1);
                    loc = loc.add(curDir);
                    if(isMoveable(rc, loc, curDir, false)){
                        curDir = rotateInt(bestDir, -1);
                        loc = loc.add(curDir);
                        if(isMoveable(rc, loc, curDir, false)){
                            return firstDir;
                        }
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, -1);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, -1);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    curDir = rotateInt(bestDir, 1);
                    loc = loc.add(curDir);
                    if(isMoveable(rc, loc, curDir, false)){
                        curDir = rotateInt(bestDir, 1);
                        loc = loc.add(curDir);
                        if(isMoveable(rc, loc, curDir, false)){
                            return firstDir;
                        }
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, 2);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, 1);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    curDir = rotateInt(bestDir, -1);
                    loc = loc.add(curDir);
                    if(isMoveable(rc, loc, curDir, false)){
                        curDir = rotateInt(bestDir, -2);
                        loc = loc.add(curDir);
                        if(isMoveable(rc, loc, curDir, false)){
                            return firstDir;
                        }
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, -2);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, -1);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    curDir = rotateInt(bestDir, 1);
                    loc = loc.add(curDir);
                    if(isMoveable(rc, loc, curDir, false)){
                        curDir = rotateInt(bestDir, 2);
                        loc = loc.add(curDir);
                        if(isMoveable(rc, loc, curDir, false)){
                            return firstDir;
                        }
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, 2);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, 2);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    curDir = rotateInt(bestDir, -2);
                    loc = loc.add(curDir);
                    if(isMoveable(rc, loc, curDir, false)){
                        curDir = rotateInt(bestDir, -2);
                        loc = loc.add(curDir);
                        if(isMoveable(rc, loc, curDir, false)){
                            return firstDir;
                        }
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, -2);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, -2);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    curDir = rotateInt(bestDir, 2);
                    loc = loc.add(curDir);
                    if(isMoveable(rc, loc, curDir, false)){
                        curDir = rotateInt(bestDir, 2);
                        loc = loc.add(curDir);
                        if(isMoveable(rc, loc, curDir, false)){
                            return firstDir;
                        }
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, 3);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, 1);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    curDir = rotateInt(bestDir, -1);
                    loc = loc.add(curDir);
                    if(isMoveable(rc, loc, curDir, false)){
                        curDir = rotateInt(bestDir, -2);
                        loc = loc.add(curDir);
                        if(isMoveable(rc, loc, curDir, false)){
                            return firstDir;
                        }
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, -3);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, -1);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    curDir = rotateInt(bestDir, 1);
                    loc = loc.add(curDir);
                    if(isMoveable(rc, loc, curDir, false)){
                        curDir = rotateInt(bestDir, 2);
                        loc = loc.add(curDir);
                        if(isMoveable(rc, loc, curDir, false)){
                            return firstDir;
                        }
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, 3);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, 1);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    curDir = rotateInt(bestDir, -1);
                    loc = loc.add(curDir);
                    if(isMoveable(rc, loc, curDir, false)){
                        curDir = rotateInt(bestDir, -1);
                        loc = loc.add(curDir);
                        if(isMoveable(rc, loc, curDir, false)){
                            return firstDir;
                        }
                    }
                }
            }
        }
        curDir = rotateInt(bestDir, -3);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, -1);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    curDir = rotateInt(bestDir, 1);
                    loc = loc.add(curDir);
                    if(isMoveable(rc, loc, curDir, false)){
                        curDir = rotateInt(bestDir, 1);
                        loc = loc.add(curDir);
                        if(isMoveable(rc, loc, curDir, false)){
                            return firstDir;
                        }
                    }
                }
            }
        }
        if(Clock.getBytecodesLeft() < 5000) return Direction.CENTER;
        curDir = rotateInt(bestDir, 1);
        firstDir = curDir;
        loc = curLoc.add(curDir);
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, 0);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    curDir = rotateInt(bestDir, 0);
                    loc = loc.add(curDir);
                    if(isMoveable(rc, loc, curDir, false)){
                        curDir = rotateInt(bestDir, 0);
                        loc = loc.add(curDir);
                        if(isMoveable(rc, loc, curDir, false)){
                            curDir = rotateInt(bestDir, -1);
                            loc = loc.add(curDir);
                            if(isMoveable(rc, loc, curDir, false)){
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
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, 0);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    curDir = rotateInt(bestDir, 0);
                    loc = loc.add(curDir);
                    if(isMoveable(rc, loc, curDir, false)){
                        curDir = rotateInt(bestDir, 0);
                        loc = loc.add(curDir);
                        if(isMoveable(rc, loc, curDir, false)){
                            curDir = rotateInt(bestDir, 1);
                            loc = loc.add(curDir);
                            if(isMoveable(rc, loc, curDir, false)){
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
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, 0);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    curDir = rotateInt(bestDir, 0);
                    loc = loc.add(curDir);
                    if(isMoveable(rc, loc, curDir, false)){
                        curDir = rotateInt(bestDir, 0);
                        loc = loc.add(curDir);
                        if(isMoveable(rc, loc, curDir, false)){
                            curDir = rotateInt(bestDir, -2);
                            loc = loc.add(curDir);
                            if(isMoveable(rc, loc, curDir, false)){
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
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, 0);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    curDir = rotateInt(bestDir, 0);
                    loc = loc.add(curDir);
                    if(isMoveable(rc, loc, curDir, false)){
                        curDir = rotateInt(bestDir, 0);
                        loc = loc.add(curDir);
                        if(isMoveable(rc, loc, curDir, false)){
                            curDir = rotateInt(bestDir, 2);
                            loc = loc.add(curDir);
                            if(isMoveable(rc, loc, curDir, false)){
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
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, 1);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    curDir = rotateInt(bestDir, 0);
                    loc = loc.add(curDir);
                    if(isMoveable(rc, loc, curDir, false)){
                        curDir = rotateInt(bestDir, -1);
                        loc = loc.add(curDir);
                        if(isMoveable(rc, loc, curDir, false)){
                            curDir = rotateInt(bestDir, -1);
                            loc = loc.add(curDir);
                            if(isMoveable(rc, loc, curDir, false)){
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
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, -1);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    curDir = rotateInt(bestDir, 0);
                    loc = loc.add(curDir);
                    if(isMoveable(rc, loc, curDir, false)){
                        curDir = rotateInt(bestDir, 1);
                        loc = loc.add(curDir);
                        if(isMoveable(rc, loc, curDir, false)){
                            curDir = rotateInt(bestDir, 1);
                            loc = loc.add(curDir);
                            if(isMoveable(rc, loc, curDir, false)){
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
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, 1);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    curDir = rotateInt(bestDir, 0);
                    loc = loc.add(curDir);
                    if(isMoveable(rc, loc, curDir, false)){
                        curDir = rotateInt(bestDir, -1);
                        loc = loc.add(curDir);
                        if(isMoveable(rc, loc, curDir, false)){
                            curDir = rotateInt(bestDir, -2);
                            loc = loc.add(curDir);
                            if(isMoveable(rc, loc, curDir, false)){
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
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, -1);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    curDir = rotateInt(bestDir, 0);
                    loc = loc.add(curDir);
                    if(isMoveable(rc, loc, curDir, false)){
                        curDir = rotateInt(bestDir, 1);
                        loc = loc.add(curDir);
                        if(isMoveable(rc, loc, curDir, false)){
                            curDir = rotateInt(bestDir, 2);
                            loc = loc.add(curDir);
                            if(isMoveable(rc, loc, curDir, false)){
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
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, 1);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, 1);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    curDir = rotateInt(bestDir, -1);
                    loc = loc.add(curDir);
                    if(isMoveable(rc, loc, curDir, false)){
                        curDir = rotateInt(bestDir, -1);
                        loc = loc.add(curDir);
                        if(isMoveable(rc, loc, curDir, false)){
                            curDir = rotateInt(bestDir, -2);
                            loc = loc.add(curDir);
                            if(isMoveable(rc, loc, curDir, false)){
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
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, -1);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, -1);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    curDir = rotateInt(bestDir, 1);
                    loc = loc.add(curDir);
                    if(isMoveable(rc, loc, curDir, false)){
                        curDir = rotateInt(bestDir, 1);
                        loc = loc.add(curDir);
                        if(isMoveable(rc, loc, curDir, false)){
                            curDir = rotateInt(bestDir, 2);
                            loc = loc.add(curDir);
                            if(isMoveable(rc, loc, curDir, false)){
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
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, 2);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    curDir = rotateInt(bestDir, 0);
                    loc = loc.add(curDir);
                    if(isMoveable(rc, loc, curDir, false)){
                        curDir = rotateInt(bestDir, -2);
                        loc = loc.add(curDir);
                        if(isMoveable(rc, loc, curDir, false)){
                            curDir = rotateInt(bestDir, -2);
                            loc = loc.add(curDir);
                            if(isMoveable(rc, loc, curDir, false)){
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
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, -2);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, 0);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    curDir = rotateInt(bestDir, 0);
                    loc = loc.add(curDir);
                    if(isMoveable(rc, loc, curDir, false)){
                        curDir = rotateInt(bestDir, 2);
                        loc = loc.add(curDir);
                        if(isMoveable(rc, loc, curDir, false)){
                            curDir = rotateInt(bestDir, 2);
                            loc = loc.add(curDir);
                            if(isMoveable(rc, loc, curDir, false)){
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
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, 1);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, 1);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    curDir = rotateInt(bestDir, -1);
                    loc = loc.add(curDir);
                    if(isMoveable(rc, loc, curDir, false)){
                        curDir = rotateInt(bestDir, -1);
                        loc = loc.add(curDir);
                        if(isMoveable(rc, loc, curDir, false)){
                            curDir = rotateInt(bestDir, -1);
                            loc = loc.add(curDir);
                            if(isMoveable(rc, loc, curDir, false)){
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
        if(isMoveable(rc, loc, curDir, true)){
            curDir = rotateInt(bestDir, -1);
            loc = loc.add(curDir);
            if(isMoveable(rc, loc, curDir, false)){
                curDir = rotateInt(bestDir, -1);
                loc = loc.add(curDir);
                if(isMoveable(rc, loc, curDir, false)){
                    curDir = rotateInt(bestDir, 1);
                    loc = loc.add(curDir);
                    if(isMoveable(rc, loc, curDir, false)){
                        curDir = rotateInt(bestDir, 1);
                        loc = loc.add(curDir);
                        if(isMoveable(rc, loc, curDir, false)){
                            curDir = rotateInt(bestDir, 1);
                            loc = loc.add(curDir);
                            if(isMoveable(rc, loc, curDir, false)){
                                return firstDir;
                            }
                        }
                    }
                }
            }
        }
        return Direction.CENTER;
    }

    private static Direction rotateInt(Direction dir, int rotate) {
        switch(rotate) {
            case 0: return dir;
            case 1: return dir.rotateRight();
            case -1: return dir.rotateLeft();
            case 2: return dir.rotateRight().rotateRight();
            case -2: return dir.rotateLeft().rotateLeft();
            case 3: return dir.rotateLeft().opposite();
            case -3: return dir.rotateRight().opposite();
            default: return dir.opposite();
        }
    }

    private static boolean isMoveable(RobotController rc, MapLocation loc, Direction dir, boolean firstMove) throws GameActionException {
        if(!rc.canSenseLocation(loc)) return false;
        MapInfo info = rc.senseMapInfo(loc);
        return info.isPassable()
                && (!firstMove || !rc.canSenseRobotAtLocation(loc));
    }
}