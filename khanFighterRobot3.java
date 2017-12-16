package Summative;
import java.awt.Color;
import becker.robots.City;
import becker.robots.Direction;

/**
 * The smartest fighter robot that has three levels of determination
 * This is the one that will be used in the main fight
 * @author Taha
 * @version - January 22nd 2017
 */
public class khanFighterRobot3 extends FighterRobot{
	
	//initialize global variables
	private int health;
	private int maxNumMoves;
	private int count = 0;
	private tahaOppData[] previousArray;
	
	/**
	 * Constructor method for the robot that initializs the robot and its stats
	 * @param c - city that the robot will spawn in
	 * @param a - the starting avenue
	 * @param s - the starting street
	 * @param d - the starting direction
	 * @param id - the ID of the player
	 * @param health - the initial health
	 */
	public khanFighterRobot3 (City c, int a, int s, Direction d, int id, int health){
		super(c,a,s,d,id,health, 5, 2, 3);
		this.health = health;
		this.setColor(Color.BLACK);
		this.setLabel();
	}

	/**
	 * The method that allows the robot to travel to a certain coordinate
	 * @param s - the avenue that the robot wants to travel to
	 * @param a - the street that the robot wants to travel to
	 */
	public void goToLocation(int s, int a) 
	{
		int row = a;
		int col = s;
		
		//if the robot is below the row you want to move to, face it north and move it
		if(this.getStreet() > row)
		{
			//face it the right direction 
			while(this.isFacingNorth() == false)
				this.turnRight();
			
			//move until the street is reached
			while(this.getStreet() != row)
				this.move();
		}
		
		//if the robot is above the row you want to move to, face it south and move it
		else if(this.getStreet() < row)
		{
			//face it the right direction
			while(this.isFacingSouth() == false)
				this.turnRight();
			
			//move until the street is reached
			while(this.getStreet() != row)
				this.move();
		}
		
		//if the robot is on the left of the desired column, face it east and move
		if(this.getAvenue() < col)
		{
			//face it the right direction
			while(this.isFacingEast() == false)
				this.turnRight();
			
			//move until the street is reached
			while(this.getAvenue() != col)
				this.move();
		}
		
		//if the robot is on the right of the desired column, face it west and move
		else if(this.getAvenue() > col)
		{
			//face it the right direction 
			while(this.isFacingWest() == false)
				this.turnRight();
			
			//move until the street is reached
			while(this.getAvenue() != col)
				this.move();
		}
	}

	/**
	 * The method that where my robot makes all of its major attacking decisions
	 * @param energy - the energy that my robot has left 
	 * @param OppData - the array of all of the opponents information
	 */
	public TurnRequest takeTurn(int energy, OppData[] data) 
	{
		//initialize variables
		TurnRequest attack;
		tahaOppData[] tahaOppData = new tahaOppData[data.length];
		int targetX;
		int targetY;
		
		//update and load the current oppdata into my new tahaOppData and originalData file
		for(int i=0; i<data.length; i++)
		{
			//this array is the one that will be sorted
			tahaOppData[i] = new tahaOppData(data[i].getID(), data[i].getAvenue(), data[i].getStreet(), data[i].getHealth());
			tahaOppData[i].findDistance(this.getAvenue(), this.getStreet());
		}
		
		//if this is not my robots first turn, calculate the change in the enemies position
		if(count > 0)
			this.calculatePositionChange(tahaOppData);
		
		//sorts the sum of distance, health, and number of moves for each robot
		this.idealRobot(tahaOppData);
		int bestRobotID = 0;
		
		//finds the best robot to attack from the sorted list
		for(int i=0; i<tahaOppData.length; i++)
		{	
			//if the robot is not mine and it is not dead, attack this one
			if(tahaOppData[i].getID() != this.getID() && tahaOppData[i].getHealth() > 0)
			{
				bestRobotID = tahaOppData[i].getID();
				break;
			}
		}
		
		//rememebr the coordinates of the best robot
		this.sortID(tahaOppData);
		targetX = tahaOppData[bestRobotID].getAvenue();
		targetY = tahaOppData[bestRobotID].getStreet();	
		
		//set the number of moves based on energy
		if(energy > 20)
		{
			this.maxNumMoves = this.getNumMoves();
		}
		else
		{
			this.maxNumMoves = energy/5;
		}
		
		//if the best robot is within range of number of moves, go to it
		if(tahaOppData[bestRobotID].getDistance() <= this.maxNumMoves)
		{	
			//store the oppData in the global variable for reference in the next turn
			this.previousArray = tahaOppData;
			count++;
			attack = new TurnRequest(tahaOppData[bestRobotID].getAvenue(), tahaOppData[bestRobotID].getStreet(), tahaOppData[bestRobotID].getID(), 3);
			return attack;
		}
		
		//go partial way to the target if it is too far
		else
		{
			this.previousArray = tahaOppData;
			count++;
			
			//calculate the maximum possible x and y distances
			int goX = calculateXdistance(targetX);
			int goY = calculateYdistance(targetY);
			
			//reset the max number of moves
			this.maxNumMoves = this.getNumMoves();
			
			attack = new TurnRequest(goX, goY, -1, 0);
			return attack;
		}
		
	}

	/**
	 * Helper method that compares the previous positon and the current position of all the players to find how much they moved
	 * @param tahaOppData - the extended oppData array
	 */
	private void calculatePositionChange(tahaOppData[] tahaOppData) {
		//compare each robot to where they are and where they were to find the change in position
		for(int i=0; i<tahaOppData.length; i++)
		{
			//calculate the position change if the robot is not mine
			if(i != this.getID())
			{
				tahaOppData[i].findNumMoves(previousArray[i].getAvenue(), previousArray[i].getStreet(), previousArray[i].getMovesCount());
			}
		}
	}

	/**
	 * Finds the maximum number of moves in the X direction of the target
	 * @param targetX - the avenue of the desired robot
	 * @return - the maximum avenue that my robot can travel
	 */
	private int calculateXdistance(int targetX) {
		int nextX = this.getAvenue();
		
		//checks if the robot is behind me
		if(nextX > targetX)
		{
			//count the number of moves I can make towards that avenue
			while(nextX > targetX && this.maxNumMoves > 0)
			{
				this.maxNumMoves -= 1;
				nextX -= 1;
			}
		}
		else
		{
			//target is on the right, so add 1 to my x until I reach his x 
			while(nextX < targetX && this.maxNumMoves  > 0)
			{
				this.maxNumMoves -= 1;
				nextX += 1;
			}
		}
		return nextX;
	}
	
	/**
	 * Uses the remaining number of moves to travel in the Y direction
	 * @param targetY - the street of the target
	 * @return - the maximum row that I am able to travel
	 */
	private int calculateYdistance(int targetY) {
		int nextY = this.getStreet();
		
		//check if he is below me 
		if(nextY > targetY)
		{
			//subtract my Y until my number of moves run out
			while(nextY > targetY && this.maxNumMoves > 0)
			{
				this.maxNumMoves -= 1;
				nextY -= 1;
			}
		}
		else
		{
			//add my Y until my number of moves run out
			while(nextY < targetY && this.maxNumMoves  > 0)
			{
				this.maxNumMoves -= 1;
				nextY += 1;
			}
		}
		return nextY;
	}

	/**
	 * Helper method that finds the best robot to attack based on health, distance, and number of moves
	 * @param tahaOppData - the extended oppdata array
	 */
	private void idealRobot(tahaOppData[] tahaOppData) 
	{	
		tahaOppData placement;
		
		//sort sum by insertion sort
		for(int i=0; i<tahaOppData.length; i++)
		{
			placement = tahaOppData[i];
			
			//loops back through the sorted portion to find the right spot
			for(int j=i-1; j>=0; j--)
			{
				//move the object to this position if the object behind it is smaller than it
				if(placement.calculateSum() >= tahaOppData[j].calculateSum())
				{
					tahaOppData[j+1] = placement;
					break;
				}
				
				//swap the position of the current object and the one behind it
				else
				{
					tahaOppData[j+1] = tahaOppData[j];
					tahaOppData[j] = placement;
				}
			}
		}
	}
	
	/**
	 * Helper method that puts the robot back into order by ID
	 * Uses insertion sort
	 * @param tahaOppData - the oppData that needs to be sorted
	 */
	private void sortID(tahaOppData[] tahaOppData) {
		
		tahaOppData placement;
		
		//sort ID by insertion sort
		for(int i=0; i<tahaOppData.length; i++)
		{
			placement = tahaOppData[i];
			
			//loops back through the sorted portion to find the right spot
			for(int j=i-1; j>=0; j--)
			{
				//move the object to this position if the object behind it is smaller than it
				if(placement.getID() >= tahaOppData[j].getID())
				{
					tahaOppData[j+1] = placement;
					break;
				}
				
				//swap the position of the current object and the one behind it
				else
				{
					tahaOppData[j+1] = tahaOppData[j];
					tahaOppData[j] = placement;
				}
			}
		}
	}

	/**
	 * The results after a battle
	 * @param healthLost - the amount of health lost from a battle
	 * @param oppID - the ID of the player I just fought
	 * @param healthLost - the amount of health that the opponent lost
	 * @param healthLost - the number of rounds that were fought
	 */
	public void battleResult(int healthLost, int oppID, int oppHealthLost, int numRoundsFought) 
	{
		this.health -= healthLost;
	}
	
	/**
	 * sets the health, ID, and color of my robot
	 */
	public void setLabel()
	{
		this.setLabel(this.getID() + " " + this.health);

		//set the robot to black if it is dead
		if (this.health == 0)
		{	
			this.setColor(Color.BLACK);
		}
		
		//keep the color green
		else
		{
			this.setColor(Color.GREEN);
		}
	}
}
