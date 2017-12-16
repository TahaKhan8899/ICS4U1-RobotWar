package Summative;
import java.awt.Color;
import becker.robots.City;
import becker.robots.Direction;

/**
 * The fighter robot that sorts by health
 * @author Taha
 * @version Jan 2nd
 */
public class khanFighterRobot extends FighterRobot {

	int health; 
	int maxNumMoves;
	
	/**
	 * Constructor method for the health robot that initializs the robot and its stats
	 * @param c - city that the robot will spawn in
	 * @param a - the starting avenue
	 * @param s - the starting street
	 * @param d - the starting direction
	 * @param id - the ID of the player
	 * @param health - the initial health
	 */
	public khanFighterRobot (City c, int a, int s, Direction d, int id, int health){
		super(c,a,s,d,id,health, 4, 3, 3);
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
	 * @param energy - the energy that my robot has left over
	 * @param OppData - the array of all of the opponents information
	 */
	public TurnRequest takeTurn(int energy, OppData[] data) 
	{
		//the data array that is sent to me is the one that is sorted, the tahaoppdata array is kept the same
		TurnRequest tahaa;
		tahaOppData[] tahaOppData = new tahaOppData[data.length];
		int bestRobotID = 0;
		
		//load data into tahaoppdata
		for(int i=0; i<data.length; i++)
		{
			tahaOppData[i] = new tahaOppData(data[i].getID(), data[i].getAvenue(), data[i].getStreet(), data[i].getHealth());
			tahaOppData[i].findDistance(this.getAvenue(), this.getStreet());
		}
		
		//if any robot has 0 health, add 10000 to it so it goes to the end of the array
		for(int i=0; i<data.length; i++)
		{
			if(data[i].getHealth() == 0)
			{
				data[i].setHealth(10000);
			}
		}
		
		//change numMoves based on energy
		if(energy > 20)
		{
			this.maxNumMoves = this.getNumMoves();
		}
		else
		{
			this.maxNumMoves = energy/5;
		}
		
		//sort the health
		this.sortHealth(data);
		
		//in the sorted health array, find the lowest one that is not my ID
		for(int i=0; i<data.length; i++)
		{
			//if the ID is not mine, attack this one 
			if(data[i].getID() != this.getID())
			{
				bestRobotID = data[i].getID();
				break;
			}
		}
		
		//track the coordinates of the best robot
		int targetX = tahaOppData[bestRobotID].getAvenue();
		int targetY = tahaOppData[bestRobotID].getStreet();
		
		//if the best robot is within range of number of moves, go to it
		if(tahaOppData[bestRobotID].getDistance() <= this.maxNumMoves)
		{
			tahaa = new TurnRequest(tahaOppData[bestRobotID].getAvenue(), tahaOppData[bestRobotID].getStreet(), tahaOppData[bestRobotID].getID(), 2);
			return tahaa;
		}
		
		//go partial way to the target if it is too far
		else
		{
			//calculate the maximum possible x and y distances
			int goX = calculateXdistance(targetX);
			int goY = calculateYDistance(targetY);
			
			//reset max num moves
			this.maxNumMoves = this.getNumMoves();
			
			return tahaa = new TurnRequest(goX, goY, -1, 0);
		}
	}
	
	/**
	 * Sorts the health of all of the players from least to greatest using insertion sort
	 * @param data - the OppData array
	 */
	private void sortHealth(OppData[] data) {
		OppData placement;
		
		//sort health by insertion sort
		for(int i=0; i<data.length; i++)
		{
			placement = data[i];
			
			//loops back through the sorted portion
			for(int j=i-1; j>=0; j--)
			{
				//move the object to this position if the object behind it is smaller than it
				if(placement.getHealth() >= data[j].getHealth())
				{
					data[j+1] = placement;
					break;
				}
				
				//swap the position of the current object and the one behind it
				else
				{
					data[j+1] = data[j];
					data[j] = placement;
				}
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
		
		//checks if the robot is on the left
		if(nextX > targetX)
		{
			//count the number of moves I can make towards that avenue
			while(nextX > targetX && this.maxNumMoves > 0)
			{
				this.maxNumMoves -= 1;
				nextX -= 1;
			}
		}
		//the robot is on the right
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
	private int calculateYDistance(int targetY) {
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
	 * sets the color, health and ID of my robot
	 */
	public void setLabel()
	{
		this.setLabel(this.getID() + " " + this.health);

		//if the robot is dead, set the color to black
		if (this.health == 0)
		{	
			this.setColor(Color.BLACK);
		}
		else
		{
			this.setColor(Color.RED);
		}
	}

}
