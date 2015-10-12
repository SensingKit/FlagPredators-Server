package uk.ac.qmul.flagpredatorsserver.model;

import java.net.Socket;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Ming-Jiun Huang on 15/6/1.
 * A class for testing models
 */

public class TestGame{
	public static void main(String[] args){
		IdGenerator idGenerator = new IdGenerator();

	//Testing Team~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		System.out.println("TEST TEAM~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		//Socket clientSocket = new Socket("161.23.77.45",13333);
		
		double[] fakeLocation = {1.1, 2.2};
		int noOfPlayersInThisTeam = 5;
		Team teamRed = new Team(true, noOfPlayersInThisTeam, 3);
		Team teamBlue = new Team(true, noOfPlayersInThisTeam, 3);
		//Add players into two teams
		for(int i = 0; i < noOfPlayersInThisTeam; i++){
			teamRed.addPlayer(new Player(("R" + (i+1)), "Red Player" + (i+1), fakeLocation));
			teamBlue.addPlayer(new Player(("B" + (i+1)), "Blue Player" + (i+1), fakeLocation));
		}
		/*
		for(int i = 0; i < noOfPlayersInThisTeam; i++){
			teamRed.addPlayer(new Player(clientSocket, ("R" + (i+1)), "Red Player" + (i+1), fakeLocation));
			teamBlue.addPlayer(new Player(clientSocket, ("B" + (i+1)), "Blue Player" + (i+1), fakeLocation));
		}
		*/

		teamRed.removePlayerById("R3");
		teamRed.removePlayerById("R1");
		teamBlue.removePlayerById("B2");
		teamBlue.removePlayerById("B4");

		System.out.println(teamRed.toString());
		System.out.println(teamBlue.toString());
	//Testing Game~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		System.out.println("TEST GAME~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		int noOfPlayers = 10;
		int noOfFlags = 3;
		int gameBoundary = 50;
		boolean hasJail = false;
		/*
		Initiator builder = new Initiator(
		idGenerator.generateId(), "Ming", fakeLocation); //Builder
		//Initiator builder = new Initiator(clientSocket, idGenerator.generateId(), "Ming", fakeLocation); //Builder 
		System.out.println(builder.toString());
		CTFGame game = new CTFGame(idGenerator.generateGameId(), builder.getId(), builder.getName(), fakeLocation, noOfPlayers, noOfFlags, gameBoundary, hasJail);
		for(int i = 0; i < game.getTeamRed().getNoOfPlayersInThisTeam(); i++){
			game.getTeamRed().addPlayer(new Player(idGenerator.generateId(), "Red Player" + (i+1), fakeLocation));
			game.getTeamBlue().addPlayer(new Player(idGenerator.generateId(), "Blue Player" + (i+1), fakeLocation));
		}
		*/
		/*
		for(int i = 0; i < game.getTeamRed().getNoOfPlayersInThisTeam(); i++){
			game.getTeamRed().addPlayer(new Player(clientSocket, idGenerator.generateId(), "Red Player" + (i+1), fakeLocation));
			game.getTeamBlue().addPlayer(new Player(clientSocket, idGenerator.generateId(), "Blue Player" + (i+1), fakeLocation));
		}
		*/
		//System.out.println(game.toString());

		//The size of initiated ArrayList
		ArrayList<Team> teama = new ArrayList<Team>();
		System.out.println(teama.size()); 

		//Double/Integer.toString
		TestGame t = new TestGame();
		double d = 2.0;
		String s = "======";
		t.a(s, Double.toString(d));
		System.out.println(Boolean.toString(hasJail));
	}

	void a(String s, String d){
		String r = s + d;
		System.out.println(r);
	}
}