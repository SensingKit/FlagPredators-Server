package uk.ac.qmul.flagpredatorsserver.model;

/**
 * Created by Ming-Jiun Huang on 15/6/4.
 * Contact me at m.huang@hss13.qmul.ac.uk
 * A class of helper functions is generate id and gameId for object Player and CTFGame.
 * ONCE the server is shut down, it resets the value cause there is no database for the
 * server right now.
 */

import java.util.ArrayList;

public class IdGenerator{
	private int countId = 1;
	private int countGameId = 1;
	private ArrayList<String> generatedId;	//Store the Ids created
	private ArrayList<String> generatedGameId; 

	public IdGenerator(){
		generatedId = new ArrayList<String>();
		generatedGameId = new ArrayList<String>();
	}

	public String generateId(){
		int digit = 10000000;	//Format >> pn00000001
		String id = "pn";
		while((digit - countId) > 0){
			id += "0";
			digit /= 10;
		}
		id += countId;
		countId += 1;
		generatedId.add(id);
		System.out.println("<The ID: " + id + " is generated>");
		return  id;
	}

	public String generateGameId(){
		int digit = 10000000;
		String id = "gn";
		while((digit - countId) > 0){
			id += "0";
			digit /= 10;
		}
		id += countGameId;
		countGameId += 1;
		generatedGameId.add(id);
		System.out.println("<The ID: " + id + " is generated>");
		return  id;
	}

	public int getCountId(){return countId;}
	public int getCountGameId(){return countGameId;}
	public ArrayList<String> getGeneratedId(){return generatedId;}
	public ArrayList<String> getGeneratedGameId(){return generatedGameId;}
	
}