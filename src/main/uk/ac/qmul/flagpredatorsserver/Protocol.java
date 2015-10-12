package uk.ac.qmul.flagpredatorsserver;

/**
 * Created by Ming-Jiun Huang on 15/7/5.
 * Contact me at m.huang@hss13.qmul.ac.uk
 * The protocol for client to make a request.
 */

public enum Protocol{
	REGISTER_BROADCAST,
	BROADCAST,
	REGISTER_PLAYER,
	CREATE_GAME,
	REQUEST_GAMES,
	JOIN_GAME,
	JOIN_TEAM,
	CHANGE_TEAM,
	CANCEL_GAME,
	LEAVE_ROOM,
	READY_TO_GO,
	START_GAME,
	LEAVE_GAME,
	CHECK_LOCATION_WITH_FLAG,
	CHECK_LOCATION_WITH_BASE
}