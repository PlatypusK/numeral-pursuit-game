package com.games.numeral.pursuit.messages;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.games.common.gsonmessages.ParentGameUpdateMessage;
import com.games.numeral.pursuit.adfree.Blog;
import com.games.numeral.pursuit.adfree.GameActivity;
import com.games.numeral.pursuit.adfree.MyApplication;
import com.games.numeral.pursuit.adfree.R;
import com.games.numeral.pursuit.adfree.Utils;

public final class ChildGameUpdateMessage extends ParentGameUpdateMessage {





	private ChildGameUpdateMessage(Long gameId, String gameName, Long senderId) {
		super(gameId, gameName, senderId);
		// TODO Auto-generated constructor stub
	}
	@Override
	public void onMessageReceived(final Object o) {
		Blog.i("GameUpdateMessage update", gameId);
		Context context=(Context)o;
		final Intent i =new Intent(context,GameActivity.class);
		i.putExtra(GameActivity.GAME_ID_PARAM, gameId);
		i.putExtra(GameActivity.USER_ID_PARAM, getSenderId());
		i.setAction(ChildGameUpdateMessage.class.getName()+gameId.toString());

		boolean received=LocalBroadcastManager.getInstance((Context)o).sendBroadcast(i);
		Blog.i(MyApplication.getInstance().getSettings());
		if(!received && MyApplication.getInstance().getSettings().getNotifyGameUpdate()){
			Utils.raiseNotificationReturnToLastScreen(context, i, "Opponent Moved","New move in "+this.getGameName(),R.drawable.launcher_icon);
		}


	}
}
