package v1;

import battlecode.common.GameActionException;

// SETUP PHASE LOGIC GOES HERE (TENTATIVE)

public class SetupPhase {
    public static void run() throws GameActionException {
        // for now just explore, naturally gather crumbs
        Explore.exploreNewArea();
    }
}
