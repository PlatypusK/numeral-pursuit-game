package com.games.numeral.pursuit.messages;

import com.appspot.numeralpursuit.gameserver.model.GameUpdate;
import com.games.common.gsonmessages.GsonMessage;

public class GameUpdateWrapper extends GsonMessage{

	private GameUpdate update;
	public GameUpdateWrapper(GameUpdate update){
		this.update=update;
	}
	
	
	public GameUpdate getUpdate() {
		return update;
	}


	@Override
	public void onMessageReceived(Object o) {
		
	}

}
