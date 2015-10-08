package uk.ac.qmul.flagpredatorsserver;

import java.util.ArrayList;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.EOFException;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import uk.ac.qmul.flagpredatorsserver.model.*;

/**
 * Created by Ming-Jiun Huang on 15/5/29.
 * Contact me at m.huang@hss13.qmul.ac.uk
 * The class to launch the Game Server listening to multiple clients via socket connections.
 * Each socket connection is given a separate thread to respond requests from multiple 
 * clients at the same time.
 */

public class CTFServer{
	//A specified port number, 13333, is to be bound with the game server.
	private static final int SERVER_PORT = 13333;	
	private ServerSocket serverSocket;
	private Socket clientSocket;
	private Thread connectionThread;
	private IdGenerator idGenerator;
	private ArrayList<Player> allPlayers;
	private ArrayList<CTFGame> allGames;
	private ArrayList<CTFGame> liveGames;

	CTFServer(){
		idGenerator = new IdGenerator();
		allPlayers = new ArrayList<Player>();
		allGames = new ArrayList<CTFGame>();
		liveGames = new ArrayList<CTFGame>();
	}

	public void listenToClient(){
		try{
			//Print out the IP address of this server.
			System.out.println(InetAddress.getLocalHost()); 
			serverSocket = new ServerSocket(SERVER_PORT);
			System.out.println("Waiting for a Client~~~~~~~~~~");
			while(true){ //For multiple clients.
				//Connecting with client.
				clientSocket = serverSocket.accept();
				System.out.println("Connecting With A Client From IP Address: " + 
									clientSocket.getInetAddress() + " is Connected.");
				connectionThread = new Thread(new ConnectionThread(clientSocket));
				connectionThread.start();
				System.out.println("Create a thread for the connection of this client.");
			}
		}catch(IOException e){
			System.out.println("Could not listen on port 13333.");
		}
	}

//Launch the game server.
	public static void main(String[] args){
		CTFServer server = new CTFServer();
		server.listenToClient();
	}

/** An inner class provides a thread for each client socket.
 * This thread deals with the command requested by clients.
 */
	public class ConnectionThread implements Runnable{
		protected Socket clientSocket; 
		private String clientJson;
		private String result;
		private String respondJson;
		private DataInputStream input = null; 
		private DataOutputStream output = null;
		private boolean isBroadcast;

		private DataManager inputData;
		private DataManager outputData;
		private ArrayList<DataManager> dataForMultiResponding;
		private ArrayList<String> respondJsons;
		//**For updating all other players waiting in the same game/room.
		private ArrayList<Socket> broadcastSockets;	
		private ArrayList<double[]> locations;
		//private Boolean multipleResponding = false;
		private BoundingBox thisFlagBox;
		private BoundingBox thisBaseBox;
		private BoundingBox thisGroundBox;
		private double playerLat;
		private double playerLng;

		public ConnectionThread(Socket clientSocket){
			this.clientSocket = clientSocket;
			respondJsons = new ArrayList<String>();
			dataForMultiResponding = new ArrayList<DataManager>();
			broadcastSockets = new ArrayList<Socket>();
			locations = new ArrayList<double[]>();
			isBroadcast = true;	//** For waiting for other players' updating 
		}

		void getStream(Socket clientSocket) throws IOException{
			input = new DataInputStream(clientSocket.getInputStream());
			output = new DataOutputStream(clientSocket.getOutputStream());
		}

		public void run(){
			try{
				this.getStream(clientSocket);
				while(isBroadcast){ 
					//Read the String data sent by the client.
					clientJson = input.readUTF();	
					//Print out the information of this connection.
					System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
					//Print out the String received.
					System.out.println("Received JSON: " + clientJson + "\n
										>>>>From Client Socket: " + clientSocket.toString());
					//Extract data from a request and create a data for responses.
					inputData = new DataManager(clientJson);
					outputData = this.executeCommand(inputData);

					//** The logic must be double checked!!!!!!!!
					/** Check if a response is including more than one String data or not.
					 * If the return value is not null, the server makes a single response to
					 * the client. 
					 * If the return value is null, the server is making a multiple responses
					 * to the client, which means the server sends more than one String data 
					 * to the client.
					 * Sending String data via DataOutputStream.
					 */
					if(outputData != null){ //**OR use boolean value, multipleResponding.
						System.out.println(outputData.toString()); //Show ouputData
						respondJson = outputData.toJson();
						System.out.println("<><><><><><>Responding JSON" + respondJson);
						output.writeUTF(respondJson);
					}else{
						System.out.println("<><><><><><>Multiple Responding JSON");
						//Sending all the String in the list.
						for(String json : respondJsons){
							output.writeUTF(json);
							System.out.println(json);
						}
						System.out.println("MULTIPLE RESPONDING IS DONE");
						//multipleResponding = false;
					}
					System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
					
					if(!isBroadcast){
						output.writeUTF("end");	//Notify clients to close connection.
						output.flush(); //
					}

					try{
						Thread.sleep(200); //Sleep 0.2 second
					}catch(InterruptedException e){
						e.printStackTrace();
					}
				}
				input.close();
				output.close();
				clientSocket.close();
			}catch(IOException e){
				e.printStackTrace();
			}
		}

//Execute a responding command and creates a responding data(JSON).
		DataManager executeCommand(DataManager inputData){
			DataManager data;
			CTFGame thisGame = null;
			Player thisPlayer = null;
			Team thisTeam = null;
			double aDistance = -1.0;
			respondJson = "";
			//Remove all previous data.
			dataForMultiResponding.clear(); 
			respondJsons.clear();
			
			//Execute different actions in different protocols.
			switch(inputData.getProtocol()){

//**Haven't implemented yet------------------------------------------------------------------
				case REGISTER_BROADCAST:
					thisPlayer = this.getPlayerById(inputData.getId());
					thisPlayer.setSocket(clientSocket);
					data = new DataManager(RespondingProtocol.UPDATE_GAME_INFO);
					data.putInfo("Registered Broadcast");
					return data;
				case BROADCAST:

					return null;
//**Haven't implemented yet------------------------------------------------------------------

				case REGISTER_PLAYER:
					isBroadcast = false;
					//Capitalise the username.
					String username = Character.toUpperCase(inputData.getName().charAt(0)) + 
									  inputData.getName().substring(1);
					//Register a player for this client.
					thisPlayer = new Player(idGenerator.generateId(), 
											username, 
											inputData.getLocation());
					//Add this player into the list storing all the players.
					allPlayers.add(thisPlayer);
					/** RespondingProtocol <respond_id>
					 * Data:
					 * > playerId
					 * > username
					 */
					data = new DataManager(RespondingProtocol.RESPOND_ID);
					data.putId(thisPlayer.getId());
					data.putName(thisPlayer.getName());
					System.out.println(data.toString());
					return data;

				case CREATE_GAME:
					isBroadcast = false;
					//Create a Game.
					//The game name(gameName) is builder's username.
					thisGame = new CTFGame(idGenerator.generateGameId(), 
											inputData.getId(), 
											this.getPlayerById(inputData.getId()).getName(), 
											inputData.getLocation(), 
											inputData.getNoOfPlayers(), 
											inputData.getNoOfFlags(), 
											inputData.getGameBoundary(), 
											inputData.hasJail());
					allGames.add(thisGame);
					/** RespondingProtocol <game_initiated>
					 * Data:
					 * > gameId
					 * > gameName
					 * > noOfPlayers
					 */
					data = new DataManager(RespondingProtocol.GAME_INITIATED);
					data.putGameId(thisGame.getGameId());
					data.putGameName(thisGame.getGameName());
					data.putNoOfPlayers(thisGame.getNoOfPlayers());
					System.out.println(data.toString());
					return data;

				case REQUEST_GAMES:
					isBroadcast = false;
					/** RespondingProtocol <show_games>
					 * Multiple Data:
					 * > gameId
					 * > gameName
					 * > noOfPlayers
					 * > joinedPlayers
					 */
					this.showGames();

					//For reconnection: Check if this player is still in a game or not.
					thisPlayer = this.getPlayerById(inputData.getId());
					for(CTFGame game : this.getLiveGames()){
						if(this.isPlayerInGame(thisPlayer, game)){
							DataManager restartData = 
								new DataManager(RespondingProtocol.STILL_IN_GAME);
							restartData.putGameId(game.getGameId());
							respondJsons.add(restartData.toJson());
						}
					}				
					//multipleResponding = true;
					return null;

				/**
				 * Only provide the info of a game room(including team info) and check
				 * availablity of the game, but this player has not join it yet. The server
				 * only sends the information of the game requested.
				 */
				case JOIN_GAME:	
					isBroadcast = false;
					thisGame = this.getGameById(inputData.getGameId());
					//**CHECK need it?
					//Check if there is a game that its ID is the given gameId.
					if(thisGame != null){ 
						//Check whether this game is full or not.
						if(thisGame.checkAvailability()){
							/** RespondingProtocol <update_game_room>
							 * Multiple Data:
					 		 * > gameId
							 * > gameName
							 * > noOfPlayers
							 * > joinedPlayers
							 * > noOfPlayersInRed
							 * > noOfPlayersInBlue
							 */
							data = new DataManager(RespondingProtocol.SHOW_GAMEROOM_INFO);
							data.putGameId(thisGame.getGameId());
							data.putGameName(thisGame.getGameName());
							data.putNoOfPlayers(thisGame.getNoOfPlayers());
							data.putJoinedPlayers(thisGame.getJoinedPlayers());
							data.putNoOfPlayersInRed(thisGame.getNoOfPlayersInRed());
							data.putNoOfPlayersInBlue(thisGame.getNoOfPlayersInBlue());	
							System.out.println(data.toString());
							return data;
						}else{
							/**This game is full or not available anymore, so the request of 
							 * joining this game is denied.
							 */
							data = new DataManager(RespondingProtocol.JOINING_DENIED);
							return data;
						}
					}else{

						data = new DataManager(RespondingProtocol.ERROR);
						return data;
					}

				case JOIN_TEAM:
					isBroadcast = false;
					thisGame = this.getGameById(inputData.getGameId());
					thisPlayer = this.getPlayerById(inputData.getId());

					//Check if this player is already in this game.
					if(this.isPlayerInGame(thisPlayer, thisGame)){
						System.out.println("This player is already in this game.");
						/** RespondingProtocol <error>
						 * Data:
			 			 * > error
						 */
						data = new DataManager(RespondingProtocol.ERROR);
						data.putError("already_in");
						return data;
					}
					//Add this player into the team requested.
					//Avoid a game they want to join becomes inactive.
					if(thisGame != null && thisPlayer != null){	
						//**Need to check no of players in this team?? 
						if(inputData.isRed() 
							&& thisGame.getNoOfPlayersInRed() < (thisGame.getNoOfPlayers()/2)){
							thisGame.getTeamRed().addPlayer(thisPlayer);
							thisPlayer.setTeam(true);
						}else if(!inputData.isRed() 
							&& thisGame.getNoOfPlayersInBlue() < (thisGame.getNoOfPlayers()/2)){
							thisGame.getTeamBlue().addPlayer(thisPlayer);
							thisPlayer.setTeam(false);
						}else{ 
							System.out.println("The team you choose is full.");
							/** RespondingProtocol <error>
							 * Data:
			 				 * > error
							 */
							data = new DataManager(RespondingProtocol.ERROR);
							data.putError("team_full");
							respondJsons.add(data.toJson());
						}
					} //** Handle other null pointer exception
					
					/** RespondingProtocol <update_game_room>
					 * Multiple Data:
			 		 * > gameId
					 * > gameName
					 * > noOfPlayers
					 * > joinedPlayers
					 * > noOfPlayersInRed
					 * > noOfPlayersInBlue
					 */
					/** RespondingProtocol <show_gameroom>
					 * Data:
					 * > gameName
					 * > noOfPlayers
					 * > noOfPlayersInRed
					 * > noOfPlayersInBlue
					 */
					this.updateGameRoom(thisGame);

					//** CALL UPDATE_GAME_ROOM again to notify other players??
					//this.broadcast(thisGame, RespondingProtocol.UPDATE_GAME_ROOM);
					return null;

//**Haven't implemented yet------------------------------------------------------------------
				case CHANGE_TEAM:
					isBroadcast = false; 
					//**player TeamStatus should be changed
					//** RespondingProtocol <update_game_room>  >> players: username, isReady, team | gameId, gameName, noOfPlayers
					data = new DataManager(RespondingProtocol.UPDATE_GAME_ROOM);
					return data;
				case CANCEL_GAME:
					isBroadcast = false;
					//**Initiator leave and cancel the game
					//thisGame.setAlive(false);
					//Broadcast >> all players are put out
					return null;	
//**Haven't implemented yet------------------------------------------------------------------
				
				case LEAVE_ROOM:
					isBroadcast = false;
					thisGame = this.getGameById(inputData.getGameId());
					thisPlayer = this.getPlayerById(inputData.getId());

					//Remove this player from the game.
					if(thisPlayer != null){
						if(thisPlayer.isRed()){
							thisGame.getTeamRed().removePlayerById(thisPlayer.getId());
						}else if (thisPlayer.isRed() == false){
							thisGame.getTeamBlue().removePlayerById(thisPlayer.getId());
						}
					//**null >> haven't added into a team, but clientSocket is registered.
					}else{	
						//Release the information of this player.
						thisPlayer.releaseGameInfo();
					}
					//** Broadcast
					//** RespondingProtocol <update_game_room>  >> players: username, isReady, team | gameId, gameName, noOfPlayers
					return null;

				case READY_TO_GO:
					isBroadcast = false;
					thisGame = this.getGameById(inputData.getGameId());
					thisPlayer = this.getPlayerById(inputData.getId());

					//Check if this player is ready or not.
					if(!thisPlayer.getReady()){
						thisPlayer.readyToGo();
					}
					//Update the data of players in this game.
					/** RespondingProtocol <update_game_room>
					 * Multiple Data:
			 		 * > playerId
					 * > username
					 * > isRed
					 * > isReady
					 */
					this.updateGameRoom(thisGame);

					//** Broadcast
					//**Refresh every clients' view
					//**Wait for other players to get ready, and then keep updating 
					// >> send 'end' communication
					return null;

				case START_GAME:
					isBroadcast = false;
					thisGame = this.getGameById(inputData.getGameId());
					thisPlayer = this.getPlayerById(inputData.getId());

					//Check if this player is ready or not. (For game initiator)
					if(!thisPlayer.getReady()){
						thisPlayer.readyToGo();
					}
					//Start the game if all the players in this game are ready.
					if(thisGame.isAllReady()){	
						thisGame.startGame();
						data = new DataManager(RespondingProtocol.UPDATE_GAME_INFO);
						return data;
					//If not, just update the room info.
					}else{	
						this.updateGameRoom(thisGame);
						return null;
					}

				case LEAVE_GAME:
					isBroadcast = false;
					thisGame = this.getGameById(inputData.getGameId());
					thisPlayer = this.getPlayerById(inputData.getId());
					//Remove this player from this game.
					if(thisPlayer.isRed()){
						thisGame.getTeamRed().removePlayerById(thisPlayer.getId());
					}else{
						thisGame.getTeamBlue().removePlayerById(thisPlayer.getId());
					}
					return null;

				case CHECK_LOCATION_WITH_FLAG:
					boolean getFlag = false;
					thisGame = this.getGameById(inputData.getGameId());
					thisPlayer = this.getPlayerById(inputData.getId());
					playerLat = inputData.getLocation()[0];
					playerLng = inputData.getLocation()[1];
					//Check if it is game over. If yes, return the result.
					if(thisGame == null){
						CTFGame overGame = this.getGameFromAllGames(inputData.getGameId());
						if(overGame.isGameOver()){
							this.gameOver(overGame);
							return null;
						}	
					}
					//Check if this player is in this game
					if(thisPlayer.isRed() && this.isPlayerInGame(thisPlayer, thisGame)){
						//Check if this player gets a flag.
						getFlag = thisGame.checkWithFlag(true, playerLat, playerLng);
						//Get flag distance
						aDistance = 
							thisGame.getBlueBox().getAccurateDistance(playerLat, playerLng);	
					}else if (thisPlayer.isRed() == false 
							&& this.isPlayerInGame(thisPlayer, thisGame)){
						getFlag = thisGame.checkWithFlag(false, playerLat, playerLng);
						aDistance = 
							thisGame.getRedBox().getAccurateDistance(playerLat, playerLng);
					}
					//Check if this player is out of bounds or not.
					if(thisGame.checkOutOfBoundary(playerLat, playerLng)){
						DataManager outData = new DataManager(RespondingProtocol.OUT_OF_BOUNDS);
						outData.putError("You are out of bounds");
						//**OUT_OF_BOUNDS is disabled.
						//**respondJsons.add(outData.toJson());	
					}
					//If a flag is capture, responding the result.
					if(getFlag){
//WORKING LINE>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
						/**Later: TAGGING enemy
						 * Need to tag this player having a flag[id] 
						 *		>> record current location
						 * Need a for loop to getFlagHolder() in CTFGame 
						 *		>> compare with other players' locations
						 * Need to a Protocol for catch enemies 
						 *		>> CHECK_LOCATION_WITH_FLAG_HOLDER?? 
						 *			or in CHECK_LOCATION_WITH_FLAG
						 */
						thisPlayer.captureFlag();
						//Record the current location of the flag holder.
						thisPlayer.setCurrentLocation(playerLat, playerLng);
						/** RespondingProtocol <get_flag>
						 * Data:
						 * distance
						 * information
						 */
						data = new DataManager(RespondingProtocol.GET_FLAG);
						data.putDistance(aDistance);
						data.putInfo("You get a FLAG!");
						respondJsons.add(data.toJson());
					//If not, update the distance between flags and players.
					}else{
						/** RespondingProtocol <update_game_info>
						 * Data:
						 * distance
						 */
						data = new DataManager(RespondingProtocol.UPDATE_GAME_INFO);
						data.putDistance(aDistance);
						respondJsons.add(data.toJson());
					}
					System.out.println(
						"LOCATION INFO=================================================================\n" +
						"Player: " + thisPlayer.getId() + 
						"[ " + thisPlayer.getName() + " ]\n" + 
						"LAT: " + playerLat + " LNG: " + playerLng + 
						"This Distance: " + aDistance);
					return null;

				case CHECK_LOCATION_WITH_BASE:
					boolean getBase = false;
					thisGame = this.getGameById(inputData.getGameId());
					thisPlayer = this.getPlayerById(inputData.getId());
					playerLat = inputData.getLocation()[0];
					playerLng = inputData.getLocation()[1];
					//Check if it is game over. If yes, return the result.
					if(thisGame == null){
						CTFGame overGame = this.getGameFromAllGames(inputData.getGameId());
						if(overGame.isGameOver()){
							this.gameOver(overGame);
							return null;
						}	
					}
					thisTeam = this.getTeamByPlayerGameId(thisPlayer, thisGame);
					//Check if reaching their base and this player is in this game.
					if(thisPlayer.isRed() && this.isPlayerInGame(thisPlayer, thisGame)){
						getBase = 
							thisGame.checkWithBase(true, playerLat, playerLng);
						aDistance = thisGame.getRedBox().getAccurateDistance(playerLat, playerLng);
					}else if (thisPlayer.isRed() == false 
							&& this.isPlayerInGame(thisPlayer, thisGame)){
						getBase = thisGame.checkWithBase(false, playerLat, playerLng);
						aDistance = 
							thisGame.getBlueBox().getAccurateDistance(playerLat, playerLng);
					}
					//Check if this player is out of bounds or not.
					if(thisGame.checkOutOfBoundary(playerLat, playerLng)){
						DataManager outData = new DataManager(RespondingProtocol.OUT_OF_BOUNDS);
						outData.putError("You are out of bounds");
						//**OUT_OF_BOUNDS is disabled.
						//**respondJsons.add(outData.toJson());
					}
					//Responding
					if(getBase && thisPlayer.isHoldingFlag()){
						//Check whether the game is still alvie to avoid that another team is won, but game is still going
						if(thisGame.isAlive()){	
							thisPlayer.releaseFlag();
							thisTeam.obtainFlag();
						}
						/** RespondingProtocol <get_base>
						 * Data:
						 * distance
						 * information
						 * holdingFlags
						 */
						data = new DataManager(RespondingProtocol.GET_BASE);
						data.putInfo("Your team gains a flag!");
						data.putDistance(aDistance);
						data.putHoldingFlags(thisTeam.getHoldingFlags());
						respondJsons.add(data.toJson());
						//Check whether one of teams wins the game.
						if(thisGame.isGameOver()){
							this.gameOver(thisGame);
							//Release all game information from each player
							thisGame.releaseAllPlayersInfo(); 
							//** BROACAST >> GAME_OVER
							System.out.println("[GAMEOVER]\n" + thisGame.toString());
							thisGame.setAlive(false);
						}
					//If not, update the distance between bases and players.
					}else{
						/** RespondingProtocol <update_game_info>
						 * Data:
						 * distance
						 */
						data = new DataManager(RespondingProtocol.UPDATE_GAME_INFO);
						data.putDistance(aDistance);
						respondJsons.add(data.toJson());
					}
					System.out.println("LOCATION INFO=================================================================\n" +
										"Player: " + thisPlayer.getId() + 
										"[ " + thisPlayer.getName() + " ]\n" + 
										"LAT: " + playerLat + " LNG: " + playerLng + 
										"This Distance: " + aDistance);
					return null;

				default:
					System.out.println("<< Protocol received does not exist>> ");
					/** RespondingProtocol <update_game_info>
					 * Data:
					 * error
					 */
					data = new DataManager(RespondingProtocol.ERROR);
					return data;
			}
		}

//FUNCTIONS -----------------------------------------------------------------------------------------------------------------------------
	//Get the game by gameId in liveGames.
		CTFGame getGameById(String gameId){
			for(CTFGame game : this.getLiveGames()){
				if(game.getGameId().equals(gameId)){
					System.out.println("Get Game By ID>>\n"+ game.toString());
					return game; 
				} 
			}
			return null;
		}

	//Get the player by ID in allPlayers.
		Player getPlayerById(String id){
			for(Player player : allPlayers){
				if(player.getId().equals(id)){
					return player;
				}
			}
			return null;
		}

	//Check whether a given player is in the specific game or not.
		boolean isPlayerInGame(Player thisPlayer, CTFGame thisGame){
			for(Player player : thisGame.getAllPlayers()){
				if(thisPlayer.getId().equals(player.getId())){
					return true;
				}
			}
			return false;
		}

	//Return the team in which the specific player in the given game is. 
		Team getTeamByPlayerGameId(Player thisPlayer, CTFGame thisGame){
			if(thisPlayer.isRed() && isPlayerInGame(thisPlayer, thisGame)){
				return thisGame.getTeamRed();
			}else if(thisPlayer.isRed() == false && isPlayerInGame(thisPlayer, thisGame)){
				return thisGame.getTeamBlue();
			}
			return null;
		}

	//Get the specific game from the all game list including both active and inactive games.
		CTFGame getGameFromAllGames(String gameId){
			for(CTFGame game : allGames){
				if(game.getGameId().equals(gameId)){
					return game; 
				} 
			}
			return null;
		}

//SHOW GAMES-----------------------------------------------------------------------------------------------------------------------------
	/**Update the list of live games to get all the games that are not over / not played yet.  
	 * Playing / waiting for players to join >> is alive
	 */
		ArrayList<CTFGame> getLiveGames(){
			liveGames.clear(); //Remove all previous data to update.
			for(CTFGame game : allGames){
				if(game.isAlive()){
					liveGames.add(game);
				}
			}
			return liveGames;
		}

	//Get all games are available to be joined.
		ArrayList<CTFGame> getJoinableGames(){
			ArrayList<CTFGame> joinableGames = new ArrayList<CTFGame>();
			for(CTFGame game : this.getLiveGames()){
				if(game.checkAvailability()){
					joinableGames.add(game);
				}
			}
			return joinableGames;
		}

	//Set the data of all the joinable games in order to send to clients.
		void setJoinableGameData(){
			for(CTFGame game : this.getJoinableGames()){
				/** RespondingProtocol <show_games>
				 * Multiple Data:
		 		 * > gameId
				 * > gameName
				 * > noOfPlayers
				 * > joinedPlayers
				 */
				DataManager dataManager = new DataManager(RespondingProtocol.SHOW_GAMES);
				dataManager.putGameId(game.getGameId());
				dataManager.putGameName(game.getGameName());
				dataManager.putNoOfPlayers(game.getNoOfPlayers());
				dataManager.putJoinedPlayers(game.getJoinedPlayers());
				dataForMultiResponding.add(dataManager);
				System.out.println(dataManager.toString());
			}
		}

	//Parse the data of all the joinable games into String data for communication.
		void showGames(){
			this.setJoinableGameData();
			for (DataManager outputData : dataForMultiResponding) {
				respondJsons.add(outputData.toJson());
			}
		}

//JOIN TEAM-----------------------------------------------------------------------------------------------------------------------------
	//Update all the players in this game room.
		void updateGameRoom(CTFGame thisGame){
			this.showGameRoomInfo(thisGame);
			/** RespondingProtocol <update_game_room>
			 * Multiple Data:
	 		 * > playerId
			 * > username
			 * > isRed
			 * > isReady
			 */
			for(Player player : thisGame.getTeamRed().getPlayersInThisTeam()){ //** need to get player list
				DataManager dataManager = new DataManager(RespondingProtocol.UPDATE_GAME_ROOM);
				dataManager.putId(player.getId());
				dataManager.putName(player.getName());
				dataManager.putTeam(true);
				dataManager.putReady(player.getReady());
				dataForMultiResponding.add(dataManager);
				System.out.println(dataManager.toString());
			}
			for(Player player : thisGame.getTeamBlue().getPlayersInThisTeam()){ //** need to get player list
				DataManager dataManager = new DataManager(RespondingProtocol.UPDATE_GAME_ROOM);
				dataManager.putId(player.getId());
				dataManager.putName(player.getName());
				dataManager.putTeam(false);
				dataManager.putReady(player.getReady());
				dataForMultiResponding.add(dataManager);
				System.out.println(dataManager.toString());
			}
			for(DataManager dataManager : dataForMultiResponding){
				respondJsons.add(dataManager.toJson()); 
			}
		}

	//
		void showGameRoomInfo(CTFGame thisGame){
			/** RespondingProtocol <show_gameroom>
			 * Data:
			 * > gameName
			 * > noOfPlayers
			 * > noOfPlayersInRed
			 * > noOfPlayersInBlue
			 */
			DataManager dataManager = new DataManager(RespondingProtocol.SHOW_GAMEROOM_INFO); 
			dataManager.putGameName(thisGame.getGameName());
			dataManager.putNoOfPlayers(thisGame.getNoOfPlayers());
			dataManager.putNoOfPlayersInRed(thisGame.getNoOfPlayersInRed());
			dataManager.putNoOfPlayersInBlue(thisGame.getNoOfPlayersInBlue());	
			respondJsons.add(dataManager.toJson());
		}

//BROADCAST-----------------------------------------------------------------------------------------------------------------------------
//Doesnt work~
	//Set the list of clientSockets in this game.
		void setBroadcastList(CTFGame thisGame){
			broadcastSockets.clear();
			String msg = "Socket List:\n";
			for(Player player : thisGame.getAllPlayers()){
				broadcastSockets.add(player.getSocket());
				msg += (player.getSocket().toString() + "\n");
			}
			System.out.println(msg);
		}

	//Broadcast the information to all clients.
		void broadcast(CTFGame thisGame, RespondingProtocol respondingProtocol){
			//Remove all previous data to update.
			dataForMultiResponding.clear(); 
			respondJsons.clear(); 

			this.setBroadcastList(thisGame);
			try{
				for(Socket clientSocket : broadcastSockets){	
					//**adding the socket is not configured yet.
					//DataInputStream inputBroadcast = 
						//new DataInputStream(clientSocket.getInputStream());
					DataOutputStream outputBroadcast = 
						new DataOutputStream(clientSocket.getOutputStream());
					switch(respondingProtocol){
						case UPDATE_GAME_ROOM:
							this.updateGameRoom(thisGame);
							break;
						case UPDATE_GAME_INFO:
//WORKING LINE>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

							//**
							DataManager getFlagData = new DataManager(respondingProtocol);
							outputBroadcast.writeUTF(getFlagData.toJson());
							break;
						case GAME_OVER:
							break;
						//**RoomList broadcast LATER
					}
					for (String json : respondJsons) {
						System.out.println("<><><><><><>Broadcast");
						outputBroadcast.writeUTF(json);
						System.out.println(json);
					}
					outputBroadcast.writeUTF("end");
					outputBroadcast.flush();	//**
					//inputBroadcast.close();
					outputBroadcast.close();
				}
			}catch(IOException e){
				e.printStackTrace();
			}
		}
//BROADCAST-----------------------------------------------------------------------------------------------------------------------------
	/*
		void updateGameInfo(CTFGame thisGame){
			DataManager dataManager = new DataManager(RespondingProtocol.UPDATE_GAME_INFO);
			dataManager.putGameId(thisGame.getGameName());	//?
		}

		void gameOver(CTFGame thisGame){
			Team winner = thisGame.getWinnerTeam();
			System.out.println(winner.toString()); 	//**
			DataManager gameOverData = new DataManager(RespondingProtocol.GAME_OVER);
			gameOverData.putInfo(winner.getTeamName() + " wins");
			gameOverData.putTeam(winner.isRedTeam());
			respondJsons.add(gameOverData.toJson());
		}
	*/
	}
}