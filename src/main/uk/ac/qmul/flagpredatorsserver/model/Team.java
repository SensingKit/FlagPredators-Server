package uk.ac.qmul.flagpredatorsserver.model;
import java.util.*;

/**
 * Created by Ming-Jiun Huang on 15/6/1.
 * Contact me at m.huang@hss13.qmul.ac.uk
 * A class to store and handle team information.
 */

public class Team{
	private String teamName;
	private boolean isRedTeam;
	private int maximumPlayers;
	private int noOfFlagsToWin;
	private int holdingFlags = 0;
	private boolean hasWon = false;
	private ArrayList<Player> playersInThisTeam;

	public Team(boolean isRedTeam, int noOfPlayers, int noOfFlagsToWin){
		this.isRedTeam = isRedTeam;
		this.maximumPlayers = noOfPlayers/2;
		this.noOfFlagsToWin = noOfFlagsToWin;
		if(isRedTeam){
			this.teamName = "Red Team";
		}else{
			this.teamName = "Blue Team";
		}
		playersInThisTeam = new ArrayList<Player>(); 
	}

	//Setters
	public void setTeamName(String teamName){this.teamName = teamName;}
	public void setIsRedTeam(boolean isRedTeam){this.isRedTeam = isRedTeam;}
	public void setMaximumPlayers(int maximumPlayers){this.maximumPlayers = maximumPlayers;}
	public void setNoOfFlagsToWin(int noOfFlagsToWin){this.noOfFlagsToWin = noOfFlagsToWin;}


	//Getters
	public String getTeamName(){ return teamName; }
	public boolean isRedTeam(){ return isRedTeam; }
	public int getMaximumPlayers(){ return maximumPlayers; }
	public int getNoOfFlagsToWin(){ return noOfFlagsToWin; }
	public int getHoldingFlags(){ return holdingFlags;}
	public ArrayList<Player> getPlayersInThisTeam(){ return playersInThisTeam; }
	public int getNoOfPlayersInThisTeam(){ return playersInThisTeam.size(); }
	public boolean hasWon(){ return hasWon; }

//In The Game Room---------------------------------------------------------------------------
//Add a player into this team.
	public void addPlayer(Player player){
		if(playersInThisTeam.size() < maximumPlayers){ //check if this team is full.
			playersInThisTeam.add(player);
			player.notReady(); //Initiated the status of the player [isReady null >> false]
			System.out.println("Player: " + player.getName() + " is added into " + teamName + 
							"! [ PlayerID: " + player.getId() + " ]\n" + this.toString());
		}else{
			//**Team is full. How to respond!!!!!!
			//Already check when the server receives a JOIN_TEAM protocol(CTFServer)
		}
		System.out.println(this.toString());
	}

//Remove a player from this team by Player Object.
	public void removePlayerByPlayer(Player player){
		if(playersInThisTeam.contains(player)){
			playersInThisTeam.remove(player);
		}
	}
//Remove a player from this team by Player id.
	public void removePlayerById(String id){
		for(int i = 0; i < playersInThisTeam.size(); i++){
			if(playersInThisTeam.get(i).getId().equals(id)){
				String leavingPlayerName = playersInThisTeam.get(i).getName();
				String leavingPlayerId = playersInThisTeam.get(i).getId();
				playersInThisTeam.get(i).releaseGameInfo();	//Release player status
				playersInThisTeam.remove(i);
				//**Respond to server and client
				System.out.println("The player: " + leavingPlayerName + " [ " + 
								leavingPlayerId + " ] is removed from the team.");
			}
		}
		System.out.println(this.toString());
	}

//During The Game----------------------------------------------------------------------------
//Obtain a flag and add one flag in holding flags and check if this team is won the game.
	public void obtainFlag(){
		holdingFlags += 1;
		if(holdingFlags >= noOfFlagsToWin){
			hasWon = true;
		}
		System.out.println(this.toString());
	}

//Return a description of a Team.
	public String toString(){
		String msg = this.teamName + "=======================================================================\n" +
					"The Players: ";
		for(Player p : playersInThisTeam){
			msg += " |" + p.getName() + "|";
		}
		msg += " HoldingFlags: " + holdingFlags;
		return msg;
	}
}