package uk.ac.qmul.flagpredatorsserver.model;

import java.net.Socket;

/**
 * Created by Ming-Jiun Huang on 15/6/1.
 * Contact me at m.huang@hss13.qmul.ac.uk
 * A class to store and handle player information.
 */

public class Player{
	private Socket clientSocket;
	private String id;
	private String username;
	private double[] currentLocation; 
	private Boolean isRed = null;
	private	Boolean isReady = null;
	private Boolean isHoldingFlag = null;

//Constructor
	public Player(String id, String username, double[] currentLocation){
		this.id = id; 
		this.username = username;
		this.currentLocation = new double[2]; 
		for (int i = 0; i < currentLocation.length ; i++) {
			this.currentLocation[i] = currentLocation[i];
		}
		this.clientSocket = null;
		System.out.println(toString());
	}

//Setters
	public void setId(String id){ this.id = id; } //**
	public void setName(String username){ this.username = username; }
	public void setCurrentLocation(double latitude, double longitude){ 
		this.currentLocation[0] = latitude; 
		this.currentLocation[1] = longitude; 
	}
	public void setTeam(Boolean isRed){ this.isRed = isRed; }
	public void setFlag(Boolean isHoldingFlag){ this.isHoldingFlag = isHoldingFlag; }
	
	/*	To Store the ClientSocket this player used in order to reuse in broadcasting 
	 *	information to other players.
	 */
	public void setSocket(Socket clientSocket){ this.clientSocket = clientSocket; } 
	
//Getters
	public Socket getSocket(){ return clientSocket; }
	public String getId(){ return id; }
	public String getName(){return username; }
	public double[] getCurrentLocation(){ return currentLocation; }
	public Boolean getReady(){ return isReady; }
	public Boolean isRed(){ return isRed; }
	public Boolean isHoldingFlag(){ return isHoldingFlag; }


//In The Game Room---------------------------------------------------------------------------
	//Release game information when leaving a game.
	public void releaseGameInfo(){
		clientSocket = null;
		isRed = null;
		isReady = null;
		isHoldingFlag = null;
		System.out.println(this.toString());
	}

	//Change player's ready status.
	public void readyToGo(){ isReady = true; }
	public void notReady(){ isReady = false; }

//During The Game----------------------------------------------------------------------------
	//This player captures a flag.
	public void captureFlag(){
		isHoldingFlag = true;
		String msg = "\n[Capture A Flag]";
		for(int i = 0; i < 10; i++){ msg += "\n[Capture A Flag]"; }
		System.out.println(msg + "\n");
		System.out.println(toString());
	}

	//This player loses a flag.
	public void releaseFlag(){
		isHoldingFlag = false;
	}

//Print out the information of this player
	public String toString(){
		String msg = "Player=====================================================================\n" +
					"Player: " + this.username + "[ ID: " + id + " ]\n";
		if(clientSocket != null){
			msg +=	"Socket: " + this.clientSocket.toString() + "\n";
		}		 
			msg +=	"The Location: Latitude<" + currentLocation[0] + 
					"> Longitude<" + currentLocation[1] + ">\n" +
					"Status: " + this.getStatusInString() + " ]\n" + 
					"Team(isRed): " + this.isRed() + " ]\n" + 
					"Ready to start? " + this.getReady()+ " ]\n" + 
					"Holding flag? " + this.isHoldingFlag;
		return msg;
	}
}