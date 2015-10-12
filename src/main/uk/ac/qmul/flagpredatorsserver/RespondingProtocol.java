package uk.ac.qmul.flagpredatorsserver;

/**
 * Created by Ming-Jiun Huang on 15/7/7.
 * Contact me at m.huang@hss13.qmul.ac.uk
 * The protocol for the game server to respond a request from a client.
 */

public enum RespondingProtocol{
	RESPOND_ID,
	GAME_INITIATED,
	SHOW_GAMES,
	SHOW_GAMEROOM_INFO,
	JOINING_DENIED,
	STILL_IN_GAME,
	UPDATE_GAME_ROOM,
	UPDATE_GAME_INFO,
	GET_FLAG,
	GET_BASE,
	OUT_OF_BOUNDS,
	GAME_OVER,
	ERROR
}