package uk.ac.qmul.flagpredatorsserver.model;

/**
 * Created by Ming-Jiun Huang on 15/6/1.
 * Contact me at m.huang@hss13.qmul.ac.uk
 * This class is not used in the game server yet.
 */

public class Flag{
	private double[] location;
	private boolean isCaught;

	public Flag(double[] location){
		this.location[0] = location[0];
		this.location[1] = location[1];
	}

//Setters
	public void isCaught(){
		isCaught = true;
	}

	public void isReleased(){
		isCaught = false;
	}
//Getters
	public double[] getLocation(){ return location; }
	public boolean getFlagStatus(){ return isCaught; }
}