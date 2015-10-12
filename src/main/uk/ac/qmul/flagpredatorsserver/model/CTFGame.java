package uk.ac.qmul.flagpredatorsserver.model;

import java.util.ArrayList;
import java.net.Socket;

/**
 * Created by Ming-Jiun Huang on 15/6/1.
 * Contact me at m.huang@hss13.qmul.ac.uk
 * A class to store and handle game information.
 */

public class CTFGame{
	private double FLAG_RADIUS = 3.0;
    private double BASE_RADIUS = 3.0;
	private String gameId;
	private String initiatorId;	//The game builder's playerId.
	private String gameName;	//Initiator's name.
	private double[] gameLocation; //**How to handle properly?
	private int noOfPlayers;
	private int noOfFlags;
	private int gameBoundary;
	private boolean hasJail;
	private Team teamRed;
	private Team teamBlue;
	private ArrayList<Player> allPlayers;
	private boolean isAlive = true; //Inactive >> the game was over.
	private ArrayList<Socket> allSockets;
	private BoundingBox redBox;
	private BoundingBox blueBox;
	private BoundingBox groundBox;
	private Team winnerTeam;

	public CTFGame(String gameId, String initiatorId, String gameName, double[] gameLocation,
					int noOfPlayers, int noOfFlags, int gameBoundary, boolean hasJail){
		this.gameId = gameId;
		this.initiatorId = initiatorId;
		this.gameName = gameName;
		this.gameLocation = new double[2]; 
		for (int i = 0; i < gameLocation.length ; i++) {
			this.gameLocation[i] = gameLocation[i];
		}
		this.noOfPlayers = noOfPlayers;
		this.noOfFlags = noOfFlags;
		this.gameBoundary = gameBoundary; 
		this.hasJail = hasJail;

		//Create two teams(Red and Blue)
		teamRed = new Team(true, noOfPlayers, noOfFlags);
		teamBlue = new Team(false, noOfPlayers, noOfFlags);
		//Create a list to store all players
		allPlayers = new ArrayList<Player>();
		//Create a list to store all sockets
		allSockets = new ArrayList<Socket>(); 
		redBox = null;
		blueBox = null;
		groundBox = null;
		winnerTeam = null;
		System.out.println(this.toString()); //Print out info when creating a game.
	}

//Setters
	public void setGameId(String gameId){ this.gameId = gameId; } //**Need it?
	public void setInitiatorId(String initiatorId){ this.initiatorId = initiatorId; }
	public void setGameName(String gameName){ this.gameName = gameName; }
	public void setGameLocation(double latitude, double longitude){
		this.gameLocation[0] = latitude;
		this.gameLocation[1] = longitude; 
	}
	public void setNoOfPlayers(int noOfPlayers){ this.noOfPlayers = noOfPlayers; }
	public void setNoOfFlags(int noOfFlags){ this.noOfFlags = noOfFlags; }
	//**Need it?
	public void setGameBoundry(int gameBoundary){ this.gameBoundary = gameBoundary; }	
	//**Haven't implemtented yet.
	public void setHasJail(boolean hasJail){ this.hasJail = hasJail; } 
	public void setAlive(boolean isAlive){ this.isAlive = isAlive; }

//Getters
	public String getGameId(){ return gameId; }
	public String getInitiatorId(){ return initiatorId; }
	public String getGameName(){ return gameName; }
	public double[] getGameLocation(){ return gameLocation; }
	public int getNoOfPlayers(){ return noOfPlayers; }
	public int getNoOfFlags(){ return noOfFlags; }
	public int getGameBoundary(){ return gameBoundary; } //**Need it?
	public boolean hasJail(){ return hasJail; }
	public Team getTeamRed(){ return teamRed; }
	public Team getTeamBlue(){ return teamBlue; }
	public int getNoOfPlayersInRed(){ return teamRed.getNoOfPlayersInThisTeam(); }
	public int getNoOfPlayersInBlue(){ return teamBlue.getNoOfPlayersInThisTeam(); }
	public boolean isAlive(){ return isAlive; }

	public double[] getRedBoxLocation(){ return redBox.getBoxLocation(); }
	public double[] getBlueBoxLocation(){ return blueBox.getBoxLocation(); }
	//**Testing
	public BoundingBox getRedBox(){ return redBox; }
	public BoundingBox getBlueBox(){ return blueBox; }
	public Team getWinnerTeam(){ return winnerTeam; }

	public ArrayList<Player> getAllPlayers(){
		allPlayers.clear(); //Clear the list.
		//Update the list of all the players.
		for(int i = 0; i < teamRed.getPlayersInThisTeam().size(); i++){
				allPlayers.add(teamRed.getPlayersInThisTeam().get(i));
		}
		for(int i = 0; i < teamBlue.getPlayersInThisTeam().size(); i++){
				allPlayers.add(teamBlue.getPlayersInThisTeam().get(i));
		}
		return allPlayers;
	}

//In The Game Room---------------------------------------------------------------------------
	//Check whether each player is ready or not.
	public boolean isAllReady(){
		this.getAllPlayers(); //Update the list first.
		if(allPlayers.size() == noOfPlayers){
			for( int i = 0; i < allPlayers.size(); i++){
				if(!allPlayers.get(i).getReady()){
					System.out.println(
						"This player is not ready: " + allPlayers.get(i).getName());
					return false;
				}
			}
		}else if(allPlayers.size() < noOfPlayers){
			System.out.println("This game is not full");
			return false;
		}
		System.out.println("All players are ready");
		return true;
	}

	//Return the number of players joined the game.
	public int getJoinedPlayers(){
		this.getAllPlayers(); //Update the list first.
		return allPlayers.size();
	}

	//Check whether it is available to join this game.
	public boolean checkAvailability(){
		this.getAllPlayers(); //Update the list first.
		if(allPlayers.size() < noOfPlayers){
			return true;
		}else{
			return false;
		}
	}

//During The Game-----------------------------------------------------------------------------
	//Get all Sockets in this game.	
	public ArrayList<Socket> getAllSockets(){
		allSockets.clear(); //Clear the list
		for(Player player : this.getAllPlayers()){
			allSockets.add(player.getSocket());
		}
		return allSockets;
	}

	//Start the game and create the bounding boxes for playground, bases and flags.
	public void startGame(){
		if(groundBox == null){
			groundBox = new BoundingBox(gameLocation[0], gameLocation[1], gameBoundary);
			redBox = new BoundingBox(gameLocation[0], gameLocation[1], true, gameBoundary);
			blueBox = new BoundingBox(gameLocation[0], gameLocation[1], false, gameBoundary);
		}
	}

	//Check whether a player is in bounds of enemy's base ot not.
	public boolean checkWithFlag(boolean isRed, double lat, double lng){
		if(isRed){
			if(blueBox.getAccurateDistance(lat, lng) <= FLAG_RADIUS ){
				return true;
			}
		}else{
			if(redBox.getAccurateDistance(lat, lng) <= FLAG_RADIUS){
				return true;
			}
		}
		return false;
	}

	//Check whether a player is in bounds of own base or not.
	public boolean checkWithBase(boolean isRed, double lat, double lng){
		if(isRed){
			if(redBox.getAccurateDistance(lat, lng) <= BASE_RADIUS ){
				return true;
			}
		}else{
			if(blueBox.getAccurateDistance(lat, lng) <= BASE_RADIUS){
				return true;
			}
		}
		return false;
	}

	//Check whether a player is out of bounds or not.
	public boolean checkOutOfBoundary(double lat, double lng){
		if(groundBox.checkInBoundsByCoordinate(lat, lng)){
			return false;
		}else{
			return true;
		}
	}

	//Make this game inactive(isAlive = false) when a game is over.
	public boolean isGameOver(){
		if(teamRed.hasWon()){
			winnerTeam = teamRed;
			isAlive = false;
			return true;
		}else if (teamBlue.hasWon()){
			winnerTeam = teamBlue;
			isAlive = false;
			return true;
		}
		return false;
	}

	//Release all the game information of each player.
	public void releaseAllPlayersInfo(){
		for(Player player : this.getAllPlayers()){
			player.releaseGameInfo();
		}
	}

	public String toString(){
		String msg = "Game=======================================================================\n" +
					"The CTF game: " + gameName + "'s game  (GAME ID: " + gameId + ")\n" + 
					"initiator ID: " + initiatorId + "\n" +
					"The Location: Latitude<" + gameLocation[0] + 
					"> Longitude<" + gameLocation[1] + ">\n" + 
					"No of Players: " + noOfPlayers + "\n" + 
					"No of Flags: " + noOfFlags + "\n" + 
					"The Length of Game Boundary: " + gameBoundary + "\n" + 
					"Has a Jail: " + hasJail + "\n" + 
					teamRed.toString() + "\n" + 
					teamBlue.toString() + "\n";
		return msg;
	}
}