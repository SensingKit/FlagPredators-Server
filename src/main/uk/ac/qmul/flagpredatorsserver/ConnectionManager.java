
package uk.ac.qmul.flagpredatorsserver;

/**
 * Created by Ming-Jiun Huang on 15/6/9
 * Contact me at m.huang@hss13.qmul.ac.uk
 * Collect string Data and Parse it into a real command.
 * Hold all the protocol information to execute commands.
 * Data Format: 
 *		<register_player>{username="Ming",latitude="2.2",longitude="1.1"}
 * 		<create_game>{playerid="pn00000001",
 *					  latitude="2.2",
 *					  longitude="1.1",
 *					  players="10",
 *					  flags="3",
 *					  boundary="50",
 *					  jail="false"}
 * 		<join_game>{playerid="pn00000002",gameid="gn00000001",latitude="2.2",longitude="1.1"}
 *		...
 */


public class ConnectionManager{
	private Protocol command;
	private RespondingProtocol respondingProtocol;
	private String commandValue;
	private String json;
	private String jsonValues;
	private boolean firstKey = true;
	private int keyStartIndex;

//For Input Data: Get the command of this connection.
	ConnectionManager(String json){
		this.json = json;
		int startIndex = 0;
		keyStartIndex = json.indexOf(">",startIndex);
		commandValue =  json.substring(startIndex+1, keyStartIndex);
		jsonValues = json.substring(keyStartIndex + 1, json.length());
		System.out.println("commandValue: " + commandValue); 
	}

//For Output Data: Set up the command of this connection.
	ConnectionManager(RespondingProtocol respondingProtocol){
		this.respondingProtocol = respondingProtocol;
	}

//Getters for testing
	String getCommandString(){ return commandValue; }
	String getKeyValues(){ return jsonValues; }

//Collecting---------------------------------------------------------------------------------
	//Search the value from the given key, returning the value in String type.
	String parseJson(String key){
		if(jsonValues.contains(key)){
			int keyIndex = jsonValues.indexOf(key,0);
			int startIndex = jsonValues.indexOf("\"",keyIndex);
			int endIndex = jsonValues.indexOf("\"",startIndex+1);
			String value = jsonValues.substring(startIndex+1, endIndex);
			System.out.println("Key: " + key + " Value: " + value);
			return value;
		}else{
			return null;
		}
	}

	//Check if there is a string key in this JSON.
	boolean isNull(String key){
		if(jsonValues.contains(key)){
			return false;
		}else{
			return true;
		}
	}

	//Get the command from JSON String and Return a Protocol value
	Protocol getCommand(){
		switch(commandValue){
			case "register_broadcast":
				command = Protocol.REGISTER_BROADCAST;
				return command;
			case "broadcast":
				command = Protocol.BROADCAST;
				return command;
			case "register_player": 
				command = Protocol.REGISTER_PLAYER;
				return command;
			case "create_game": 
				command = Protocol.CREATE_GAME;
				return command;
			case "request_games": 
				command = Protocol.REQUEST_GAMES;
				return command;
			case "join_game": 
				command = Protocol.JOIN_GAME;
				return command;
			case "join_team": 
				command = Protocol.JOIN_TEAM;
				return command;
			case "change_team": 
				command = Protocol.CHANGE_TEAM;
				return command;
			case "cancel_game":
				command = Protocol.CANCEL_GAME;
				return command;
			case "leave_room": 
				command = Protocol.LEAVE_ROOM;
				return command;
			case "ready_to_go":
				command = Protocol.READY_TO_GO;
				return command;
			case "start_game": 
				command = Protocol.START_GAME;
				return command;
			case "leave_game": 
				command = Protocol.LEAVE_GAME;
				return command;
			case "check_location_with_flag": 
				command = Protocol.CHECK_LOCATION_WITH_FLAG;
				return command;
			case "check_location_with_base": 
				command = Protocol.CHECK_LOCATION_WITH_BASE;
				return command;
			default:
				command = null;
				return command;
		}
	}

//Responding-------------------------------------------------------------------------------
	//Respond the information to clients
	String getRespondingProtocolInString(){
		switch(respondingProtocol){
			case RESPOND_ID:
				return "<respond_id>";
			case GAME_INITIATED:
				return "<game_initiated>";
			case SHOW_GAMES:
				return "<show_games>";
			case SHOW_GAMEROOM_INFO:
				return "<show_gameroom>";
			case JOINING_DENIED:
				return "<joining_denied>";
			case STILL_IN_GAME:
				return "<still_in_game>";
			case UPDATE_GAME_ROOM:
				return "<update_game_room>";
			case UPDATE_GAME_INFO:
				return "<update_game_info>";
			case GET_FLAG:
				return "<get_flag>";
			case GET_BASE:
				return "<get_base>";
			case OUT_OF_BOUNDS:
				return "<out_of_bounds>";
			case GAME_OVER:
                return "<game_over>";
            case ERROR:
            	return "<error>";
			default:
				return null;
		}
	}

	//Add Key and Value for String data
	String addKeyValue(String key, String value){ 
		String info = "";
		if(firstKey){
			firstKey = false;
		}else{
			info += ",";
		}
		info += (key + "=\"" + value + "\"");
		return info;
	}

	//Return the protocol command in String
	String getProtocolInString(){
		this.getCommand();
		switch(command){
			case REGISTER_BROADCAST:
				return "Register broadcast";
			case BROADCAST:
				return "Broadcast";
			case REGISTER_PLAYER:
				return "Register a player";
			case CREATE_GAME:
				return "Create a game";
			case REQUEST_GAMES:
				return "Request games";
			case JOIN_GAME:
				return "Join a game";
			case JOIN_TEAM:
				return "Join a team";
			case CHANGE_TEAM:
				return "Change to another team";
			case CANCEL_GAME:
				return "Cancel the game";
			case LEAVE_ROOM:
				return "Leave the game room";
			case READY_TO_GO:
				return "Ready to go";
			case START_GAME:
				return "Start the game";
			case LEAVE_GAME:
				return "Leave the game";
			case CHECK_LOCATION_WITH_FLAG:
				return "Check flag";
			case CHECK_LOCATION_WITH_BASE:
				return "Check base";
			default: return "null";
		}
	}

//Testing------------------------------------------------------------------------------------
	public static void main(String[] args){
        String js = "<create_game>{playerid=\"pn00000001\",latitude=\"2.2\",longitude=\"1.1\",players=\"10\",flags=\"3\",boundary=\"50\",jail=\"false\"}";
        //String out = "<out_of_boundary>{error_info=\"You are out of boundary\"}";
        String out = "<check_location_with_flag>{_playerid=\"pn00000001\",_gameid=\"gn00000001\",_latitude=\"51.5238433\",_longitude=\"-0.0398783\"}";
        int mstartIndex = 0;
        int mkeyStartIndex = out.indexOf(">",mstartIndex);
        String mcommandValue =  out.substring(mstartIndex+1, mkeyStartIndex);
        System.out.println("keyStartIndex:"+mkeyStartIndex);
        System.out.println("commandValue: " + mcommandValue); //Get the command of this connection.
        out = out.substring(mkeyStartIndex + 1, out.length());
        System.out.println(out);
        String key = "_gameid";
        if(out.contains(key)){
            int keyIndex = out.indexOf(key,0);
            System.out.println("keyIndex:"+keyIndex);
            int startIndex = out.indexOf("\"",keyIndex);
            System.out.println("startIndex:"+startIndex);
            int endIndex = out.indexOf("\"",startIndex+1);
            System.out.println("endIndex:"+endIndex);
            String value = out.substring(startIndex+1, endIndex);
            System.out.println("Key: " + key + " Value: " + value);
        }
	}
}