package testplayer1;

import battlecode.common.*;

public strictfp class RobotPlayer {
    static RobotController rc;
    
    /*
     * BROADCAST ARRAY KEY
     * 0 - Does leading Archon exist (0=false, 1=true);
     * 1 - ArchonX coordinate
     * 2 - ArchonY coordinate
     * 
     */
    static int IS_MAIN_ARCHON_CHANNEL = 0;
    static int ARCHON_X_CHANNEL = 1;
    static int ARCHON_Y_CHANNEL = 2;
    
    
    //important numbers
    static int MAX_GARDNERS = 5;							//per archon...
    
    
    

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
    **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        // Here, we've separated the controls into a different method for each RobotType.
        // You can add the missing ones or rewrite this into your own control structure.
        switch (rc.getType()) {
            case ARCHON:
                runArchon();
                break;
            case GARDENER:
                runGardener();
                break;
            case SOLDIER:
                runSoldier();
                break;
            case LUMBERJACK:
                runLumberjack();
                break;
            case TANK:
            	runTank();
            	break;
            case SCOUT:
            	runScout();
            	break;
        }
	}
    
    

    
    
    
    static void runArchon() throws GameActionException{
        // The code you want your robot to perform every round should be in this loop
    	
    	boolean isMainArchon = false;
    	Direction movingDir;
    	int numGardners = 0;									//TODO: Doesn't account for garnder death
    	
    	MapLocation targetLocation = rc.getLocation();
    	boolean reachedDestination = true;
    	int numStepped = 0;
    	
    	
    	//tells the first archon spawned to be the main archon (who broadcasts location). Other archons don't broadcast
    	if (rc.readBroadcast(IS_MAIN_ARCHON_CHANNEL) == 0){
    		isMainArchon = true;
    		rc.broadcast(IS_MAIN_ARCHON_CHANNEL, 1);
    	}
    	


        while (true) {

        	
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	
            	//broadcasts archon location
            	if (isMainArchon){
            		rc.broadcast(ARCHON_X_CHANNEL, (int)rc.getLocation().x);
            		rc.broadcast(ARCHON_Y_CHANNEL, (int)rc.getLocation().y);
            	}
            	
            	//moves archon
            	

            	wander();

            	/*	
            	 * ALGORITHM FOR GOING AFTER BROADCASTING ENEMIES
            	MapLocation[] BroadcastingRobots = rc.senseBroadcastingRobotLocations();
            	if (reachedDestination == true){
            		for(MapLocation m : BroadcastingRobots){
            			if (m.distanceTo(rc.getLocation()) > 30){					//if it is an enemy (since all friendlies stay with in "20" as of now
            				targetLocation = m;
            				reachedDestination = false;
            				break;
            			}
            		}
            		if( !tryMove(rc.getLocation().directionTo(targetLocation)) ){
            			wander();
            		}
            	}
            	else{
            		if( !tryMove(rc.getLocation().directionTo(targetLocation)) ){
            			wander();
            		}
            	}
            	
            	if (targetLocation.isWithinDistance(rc.getLocation(), 5)){
            		reachedDestination = true;
            	}
            	*/
            	
            	
            	
            	
            	
            	
            	//hires gardners

            	
            	if (numGardners < MAX_GARDNERS && rc.canHireGardener(Direction.getEast())){
            		
            		rc.hireGardener(Direction.getEast());
            		numGardners += 1;
            		
            	}

            	
            	Clock.yield();
            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }
    	
    }
    
    
    static void runGardener() throws GameActionException{
        // The code you want your robot to perform every round should be in this loop
    	
    	MapLocation myLocation = rc.getLocation();
    	Direction movingDir = new Direction(0);
    	MapLocation archonLocation = new MapLocation(rc.readBroadcast(0),rc.readBroadcast(1));
    	final int RADIUS_TO_ARCHON = 10;



    	
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	
            	//updates locations
            	archonLocation = new MapLocation(rc.readBroadcast(ARCHON_X_CHANNEL),rc.readBroadcast(ARCHON_Y_CHANNEL));
            	myLocation = rc.getLocation();
            	
            	
            	
            	//attempts to water nearby tree
            	TreeInfo[] nearbyTrees = rc.senseNearbyTrees(2, rc.getTeam());
            	for (TreeInfo t : nearbyTrees){

           			if (  rc.canWater(t.getID())  ){
           				rc.water(t.getID());

           				break;
            		}

            	}
            	
            	if(rc.hasTreeBuildRequirements()){
	            	if (myLocation.x % 4 > 0 && myLocation.x % 4 < 0.5){
	            		if (myLocation.y %4 > 0 && myLocation.y % 4< 0.5){
	            			if (rc.canPlantTree(Direction.getEast())){
	            				rc.plantTree(Direction.getEast());

	            			}
	            		}
	            	}
            	}
            	
            	if(rc.getTeamBullets() > 120){
            		if (  rc.canBuildRobot(RobotType.SOLDIER, Direction.getWest())  ){
            			rc.buildRobot(RobotType.SOLDIER, Direction.getWest());
            		}
            		
            	}
            	
            	
            	
            	
            	//makes sure Gardener movement is within RADIUS of main archon
            	if (!rc.hasMoved()){
            		if (!myLocation.isWithinDistance(archonLocation, RADIUS_TO_ARCHON)){
            			movingDir = myLocation.directionTo(archonLocation);
            			if (rc.canMove(movingDir)){
            				rc.move(movingDir);
            			}
            			else{
            				movingDir = randomDirection();
            				if (rc.canMove(movingDir)){
            					rc.move(movingDir);
            				}
            			}
            		}else{
            			movingDir = randomDirection();
            			if (archonLocation.distanceTo(myLocation.add(movingDir, rc.getType().strideRadius)) > RADIUS_TO_ARCHON){
            				
            			}
            			else{
            				if (rc.canMove(movingDir)){
            					rc.move(movingDir);
            				}
            			}
            		}
            	}
            	
            	
            	
            	
            	Clock.yield();

            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }
    	
    }
    
    static void runSoldier() throws GameActionException{
       
    	MapLocation myLocation = rc.getLocation();
    	Direction movingDir = new Direction(0);
    	MapLocation archonLocation = new MapLocation(rc.readBroadcast(ARCHON_X_CHANNEL),rc.readBroadcast(ARCHON_Y_CHANNEL));
    	final int RADIUS_TO_ARCHON = 20;
    	
    	Direction towards;
    	
    	// The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	
            	
            	//updates locations
            	archonLocation = new MapLocation(rc.readBroadcast(ARCHON_X_CHANNEL),rc.readBroadcast(ARCHON_Y_CHANNEL));
            	myLocation = rc.getLocation();
            	
            	//makes sure Gardener movement is within RADIUS of main archon
            	if (!rc.hasMoved()){
            		if (!myLocation.isWithinDistance(archonLocation, RADIUS_TO_ARCHON)){
            			movingDir = myLocation.directionTo(archonLocation);
            			if (rc.canMove(movingDir)){
            				rc.move(movingDir);
            			}
            			else{
            				movingDir = randomDirection();
            				if (rc.canMove(movingDir)){
            					rc.move(movingDir);
            				}
            			}
            		}else{
            			movingDir = randomDirection();
            			if (archonLocation.distanceTo(myLocation.add(movingDir, rc.getType().strideRadius)) > RADIUS_TO_ARCHON){
            				
            			}
            			else{
            				if (rc.canMove(movingDir)){
            					rc.move(movingDir);
            				}
            			}
            		}
            	}
            	
            	RobotInfo[] bots = rc.senseNearbyRobots();
                for (RobotInfo b : bots) {
                    if (b.getTeam() != rc.getTeam()) {
                    	towards = rc.getLocation().directionTo(b.getLocation());
                    	rc.fireSingleShot(towards);
                    	break;
                    }
                }
            	
            	
            	Clock.yield();

            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }
    	
    }
    
    static void runLumberjack() throws GameActionException{
        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

            	Clock.yield();

            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }
    	
    }
    
    static void runTank() throws GameActionException{
        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

            	Clock.yield();

            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }
    	
    }
    static void runScout() throws GameActionException{
        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

            	Clock.yield();

            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }
    	
    }
    
    //-------------------------------------------------------------------------------------------------------
    
    /**
     * Returns a random Direction
     * @return a random Direction
     */
    static Direction randomDirection() {
        return new Direction((float)Math.random() * 2 * (float)Math.PI);
    }
    
    
    
    
    
    static boolean tryMove(Direction dir) throws GameActionException {
        return tryMove(dir,20,3);
    }

    /**
     * Attempts to move in a given direction, while avoiding small obstacles direction in the path.
     *
     * @param dir The intended direction of movement
     * @param degreeOffset Spacing between checked directions (degrees)
     * @param checksPerSide Number of extra directions checked on each side, if intended direction was unavailable
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {

        // First, try intended direction
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }

        // Now try a bunch of similar angles
        boolean moved = false;
        int currentCheck = 1;

        while(currentCheck<=checksPerSide) {
            // Try the offset of the left side
            if(rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateLeftDegrees(degreeOffset*currentCheck));
                return true;
            }
            // Try the offset on the right side
            if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateRightDegrees(degreeOffset*currentCheck));
                return true;
            }
            // No move performed, try slightly further
            currentCheck++;
        }

        // A move never happened, so return false.
        return false;
    }
    
    static void wander() throws GameActionException{
    	Direction movingDir = randomDirection();
    	if (rc.canMove(movingDir)){
    		rc.move(movingDir);
    	}
    }
    
}