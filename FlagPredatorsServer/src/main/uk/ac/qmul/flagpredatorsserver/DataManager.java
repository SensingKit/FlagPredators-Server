package uk.ac.qmul.flagpredatorsserver;

import java.util.ArrayList;
import java.net.Socket;
import uk.ac.qmul.flagpredatorsserver.model.*;

/**
 * Created by Ming-Jiun Huang on 15/6/27.
 * Contact me at m.huang@hss13.qmul.ac.uk
 * DataManager is responsible for storing the temporary data during the import or export of 
 * data. There are two constructors dealing with input data and output data respectively. 
 * Input constructor extracts the values that are read by ConnectionManager. Output 
 * constructor passes the values to ConnectionManger to create the String for data 
 * transmission.
 */

public class DataManager{
	private ConnectionManager connectionManagerIn;
	private ConnectionManager connectionManagerOut;
	private boolean isInput;
	private Protocol protocol; 
	private RespondingProtocol respondingProtocol;
	private String respondingJson;
	private String id;
	private String username;
	//private Socket clientSocket;
	private String gameId;
	private String gameName;
	private double[] location = new double[2];
	private int noOfPlayers;
	private int noOfFlags;
	private int gameBoundary;
	private int joinedPlayers;
	private int noOfPlayersInRed;
	private int noOfPlayersInBlue;
	private Boolean hasJail;
	private Boolean isRed;
	private Boolean isReady; 
	private ArrayList<Player> allPlayers;
	private ArrayList<CTFGame> liveGames;
	private String error;

//Data needed during the game
	private double distance;
	private String information;
	private int holdingFlags;

/** The constructor for Input Data.
 * Parametre is a String data called json, which will be converted into values by 
 * ConnetionManager.
 */
	public DataManager(String json){
		isInput = true;
		connectionManagerIn = new ConnectionManager(json);
		this.getData();
	}

/** The constructor for Output Data.
 * Parametre is a RespondingProtocol, which is the protocol the game server are using in 
 * responding a request. Each value will be initiated. This DataManager will only store data
 * after putting data into it by using Putters(See Below).
 */
	public DataManager(RespondingProtocol respondingProtocol){
		isInput = false; 
		connectionManagerOut = new ConnectionManager(respondingProtocol);
		this.respondingProtocol = respondingProtocol;
		this.protocol = null;
		this.id = null;
		this.username = null;
		//**this.clientSocket
		this.gameId = null;
		this.gameName = null;
		this.location[0] = -1.0; //**bugs
		this.location[1] = -1.0;
		this.noOfPlayers = -1;
		this.noOfFlags = -1;
		this.gameBoundary = -1;
		this.joinedPlayers = -1;
		this.noOfPlayersInRed = -1;
		this.noOfPlayersInBlue = -1;
		this.hasJail = false; 
		this.isRed = null;
		this.isReady = null;
		this.error = null;

		this.distance = -1.0;
		this.information = null;
		this.holdingFlags = -1;
	}

//Set up the data for input data by using Setters.
	void getData(){
		this.setProtocol();
		if(!connectionManagerIn.isNull("_playerid")){ 	
			this.setId(); }else{ id = null; 
		}
		if(!connectionManagerIn.isNull("_username")){ 
			this.setName(); }else{ username = null; 
		}
		if(!connectionManagerIn.isNull("_gameid")){ 
			this.setGameId(); }else{ gameId = null; 
		}
		if(!connectionManagerIn.isNull("_player_no")){ 
			this.setNoOfPlayers(); }else{ noOfPlayers = -1; 
		}
		if(!connectionManagerIn.isNull("_flag_no")){ 
			this.setNoOfFlags(); }else{ noOfFlags = -1; 
		}
		if(!connectionManagerIn.isNull("_boundary")){ 
			this.setGameBoundry(); }else{ gameBoundary = -1; 
		}
		if(!connectionManagerIn.isNull("_jail")){ 
			this.setJail(); }else{ hasJail = null; 
		} 
		if(!connectionManagerIn.isNull("_isred")){ 
			this.setTeam(); }else{ isRed = null; 
		}
		if(!connectionManagerIn.isNull("_isready")){ 
			this.setReady(); }else{ isReady = null; 
		}
		if(!connectionManagerIn.isNull("_latitude") && 
			!connectionManagerIn.isNull("_longitude")){
			this.setGameLocation(); 
		}else{ 
			location[0] = -1.0; location[1] = -1.0; 
		}
	}

//Setters for input data: Using ConnectionManager to extract values from a json.
	void setProtocol(){	
		this.protocol = connectionManagerIn.getCommand();
	}
	void setId(){
		this.id = connectionManagerIn.parseJson("_playerid");
	}
	void setName(){
		this.username = connectionManagerIn.parseJson("_username");
	}
	void setGameId(){
		this.gameId = connectionManagerIn.parseJson("_gameid");
	}
	void setGameName(){ 
		this.gameName = connectionManagerIn.parseJson("_gamename");
	}
	void setGameLocation(){
		this.location[0] = Double.parseDouble(connectionManagerIn.parseJson("_latitude"));
		this.location[1] = Double.parseDouble(connectionManagerIn.parseJson("_longitude"));
	}
	void setNoOfPlayers(){
		this.noOfPlayers = Integer.parseInt(connectionManagerIn.parseJson("_player_no"));
	}
	void setNoOfFlags(){
		this.noOfFlags = Integer.parseInt(connectionManagerIn.parseJson("_flag_no"));
	}
	void setGameBoundry(){
		this.gameBoundary = Integer.parseInt(connectionManagerIn.parseJson("_boundary"));
	}
	void setJail(){
		String jail = connectionManagerIn.parseJson("_jail");
		if(jail.equals("true")){
			this.hasJail = true;
		}else if(jail.equals("false")){
			this.hasJail = false;
		}else{ 
			//**null
		}
	} 
	void setTeam(){
		String team = connectionManagerIn.parseJson("_isred");
		if(team.equals("true")){
			this.isRed = true;
		}else if(team.equals("false")){
			this.isRed = false;
		}else{
			System.out.println(
				"Error from data format >> the value of team should be boolean");
		}
	} 
	void setReady(){
		String ready = connectionManagerIn.parseJson("_isready");
		if(ready.equals("true")){
			this.isReady = true;
		}else if(ready.equals("false")){
			this.isReady = false;
		}else{ 
			System.out.println(
				"Error from data format >> the value of isReady should be boolean");
		}
	} 

//Putters for output data: Put data into DataMangager for a response.
	public void putId(String id){ this.id = id; }
	public void putName(String username){ this.username = username; }
	public void putGameId(String gameId){ this.gameId = gameId; }
	public void putGameName(String gameName){ this.gameName = gameName;}
	public void putGameLocation(double latitude, double longitude){	//**
		this.location[0] = latitude;
		this.location[1] = longitude;
	}
	public void putNoOfPlayers(int noOfPlayers){ this.noOfPlayers = noOfPlayers; }
	public void putNoOfFlags(int noOfFlags){ this.noOfFlags = noOfFlags; }
	public void putGameBoundry(int gameBoundary){ this.gameBoundary = gameBoundary; }
	public void putJoinedPlayers(int joinedPlayers){ this.joinedPlayers = joinedPlayers; }
	public void putNoOfPlayersInRed(int noOfPlayersInRed){ this.noOfPlayersInRed = noOfPlayersInRed; }
	public void putNoOfPlayersInBlue(int noOfPlayersInBlue){ this.noOfPlayersInBlue = noOfPlayersInBlue; }
	public void putJail(Boolean hasJail){ this.hasJail = hasJail; } 
	public void putTeam(Boolean isRed){ this.isRed = isRed; }
	public void putReady(Boolean isReady){ this.isReady = isReady; } 
	public void putError(String error){ this.error = error; }

	public void putDistance(double distance){ this.distance = distance; }
	public void putInfo(String information){ this.information = information; }
	public void putHoldingFlags(int holdingFlags){ this.holdingFlags = holdingFlags; }

//Parse values into String for output data
	public String toJson(){
		respondingJson = connectionManagerOut.getRespondingProtocolInString() + "{";
		if(this.id != null){ 			respondingJson += connectionManagerOut.addKeyValue("_playerid", this.id); }
		if(this.username != null){ 	 	respondingJson += connectionManagerOut.addKeyValue("_username", this.username); }
		if(this.gameId != null){ 		respondingJson += connectionManagerOut.addKeyValue("_gameid", this.gameId); }
		if(this.gameName != null){ 		respondingJson += connectionManagerOut.addKeyValue("_gamename", this.gameName); }
		if(this.location[0] != -1.0){ 	respondingJson += connectionManagerOut.addKeyValue("_latitude", Double.toString(this.location[0])); }
		if(this.location[1] != -1.0){ 	respondingJson += connectionManagerOut.addKeyValue("_longitude", Double.toString(this.location[1])); }
		if(this.noOfPlayers != -1){ 	respondingJson += connectionManagerOut.addKeyValue("_player_no",Integer.toString(this.noOfPlayers)); }
		if(this.noOfFlags != -1){ 		respondingJson += connectionManagerOut.addKeyValue("_flag_no", Integer.toString(this.noOfFlags)); }
		if(this.gameBoundary != -1){ 	respondingJson += connectionManagerOut.addKeyValue("_boundary", Integer.toString(this.gameBoundary)); }
		if(this.joinedPlayers != -1){ 	respondingJson += connectionManagerOut.addKeyValue("_joinedplayers", Integer.toString(this.joinedPlayers)); }
		if(this.noOfPlayersInRed != -1){respondingJson += connectionManagerOut.addKeyValue("_redplayers", Integer.toString(this.noOfPlayersInRed)); }
		if(this.noOfPlayersInBlue !=-1){respondingJson += connectionManagerOut.addKeyValue("_blueplayers", Integer.toString(this.noOfPlayersInBlue)); }
		//**For now >> NO Jail 			respondingJson += connectionManagerOut.addKeyValue("jail", Boolean.toString(this.hasJail));
		if(this.isRed != null){			respondingJson += connectionManagerOut.addKeyValue("_isred", Boolean.toString(this.isRed)); }
		if(this.isReady != null){		respondingJson += connectionManagerOut.addKeyValue("_isready", Boolean.toString(this.isReady)); }
		if(this.error != null){ 		respondingJson += connectionManagerOut.addKeyValue("_error_info", this.error); }

		if(this.distance != -1.0){		respondingJson += connectionManagerOut.addKeyValue("_distance", Double.toString(this.distance)); }
		if(this.information != null){	respondingJson += connectionManagerOut.addKeyValue("_information", this.information); }
		if(this.holdingFlags != -1){	respondingJson += connectionManagerOut.addKeyValue("_holdingflags", Integer.toString(this.holdingFlags)); }
		respondingJson += "}";
		return respondingJson;
	}	

//Getters: Return values.
	public Protocol getProtocol(){return protocol;}
	public RespondingProtocol getRespondingProtocol(){return respondingProtocol;}
	public String getId(){ return id; }
	public String getName(){ return username; }
	public String getGameId(){ return gameId; }
	public String getGameName(){ return gameName; }
	public double[] getLocation(){ return location; }
	public int getNoOfPlayers(){ return noOfPlayers; }
	public int getNoOfFlags(){ return noOfFlags; }
	public int getGameBoundary(){ return gameBoundary; }
	public int getJoinedPlayers(){ return joinedPlayers; }
	public int getNoOfPlayersInRed(){ return noOfPlayersInRed; }
	public int getNoOfPlayersInBlue(){ return noOfPlayersInBlue; }
	public Boolean hasJail(){ return hasJail; }
	public Boolean isRed(){ return isRed; }
	public Boolean isReady(){ return isReady; }
	public String getError(){ return error; }

	public double getDistance(){ return distance; }
    public String getInfo(){ return information; }
	public int getHoldingFlags(){ return holdingFlags; }

//Print out the information of a DataManager.
	public String toString(){ 
		String msg;
		if(isInput){
			msg = 	"Input Data>>===============================================================\n" +
												   "Protocol   | " + protocol + " |\n";
			if(this.id != null){			msg += "Player ID  | " + id + " |\n"; }
			if(this.username != null){		msg += "username   | " + username + " |\n"; }
			if(this.gameId != null){		msg += "Game ID    | " + gameId + " |\n"; }
			if(this.gameName != null){		msg += "Game Name  | " + gameName + " |\n"; }
			if(this.location[0] != -1.0){	msg += "Latitude   | " + location[0] + " |\n"; }
			if(this.location[1] != -1.0){	msg += "Longitude  | " + location[1] + " |\n"; } 
			if(this.noOfPlayers != -1){		msg += "Players no | " + noOfPlayers + " |\n"; }
			if(this.noOfFlags != -1){		msg += "Flags no   | " + noOfFlags + " |\n"; }
			if(this.gameBoundary != -1){	msg += "Boundary   | " + gameBoundary + " |\n"; }
			if(this.hasJail != null){		msg += "Has Jail   | " + hasJail + " |\n"; }
			if(this.isRed != null){			msg += "Is Red team| " + isRed + " |\n"; }
			if(this.isReady != null){		msg += "Is Ready   | " + isReady; }
		}else{
					
			msg = 	"Output Data>>==============================================================\n" +
					"R_Protocol	| " + respondingProtocol + " |\n";
			if(this.id != null){			msg += "Player ID  | " + id + " |\n"; }
			if(this.username != null){		msg += "username   | " + username + " |\n"; }
			if(this.gameId != null){		msg += "Game ID    | " + gameId + " |\n"; }
			if(this.gameName != null){		msg += "Game Name  | " + gameName + " |\n"; }
			if(this.location[0] != -1.0){	msg += "Latitude   | " + location[0] + " |\n"; }
			if(this.location[1] != -1.0){	msg += "Longitude  | " + location[1] + " |\n"; } 
			if(this.noOfPlayers != -1){		msg += "Players no | " + noOfPlayers + " |\n"; }
			if(this.noOfFlags != -1){		msg += "Flags no   | " + noOfFlags + " |\n"; }
			if(this.gameBoundary != -1){	msg += "Boundary   | " + gameBoundary + " |\n"; }
			if(this.hasJail != null){		msg += "Has Jail   | " + hasJail + " |\n"; }
			if(this.isRed != null){			msg += "Is Red team| " + isRed + " |\n"; }
			if(this.isReady != null){		msg += "Is Ready   | " + isReady + " |\n"; }
			if(this.joinedPlayers != -1){	msg += "Joined p   | " + joinedPlayers + " |\n"; }
			if(this.noOfPlayersInRed != -1){msg += "No. in Red | " + noOfPlayersInRed + " |\n"; }
			if(this.noOfPlayersInBlue !=-1){msg += "No. in Blue| " + noOfPlayersInBlue + " |\n"; }
			if(this.error != null){			msg += "Error      | " + error + " |\n"; }

			if(this.distance != -1.0){	    msg += "Distance   | " + distance; }
            if(this.information != null){	msg += "Information| " + information; }
			if(this.holdingFlags != -1){	msg += "HoldingFlag| " + holdingFlags;}
		}
		return msg;
	}
}