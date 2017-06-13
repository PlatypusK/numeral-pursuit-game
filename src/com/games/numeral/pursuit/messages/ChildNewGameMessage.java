package com.games.numeral.pursuit.messages;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.games.common.gsonmessages.ParentNewGameMessage;
import com.games.numeral.pursuit.adfree.Blog;
import com.games.numeral.pursuit.adfree.GameActivity;
import com.games.numeral.pursuit.adfree.MyApplication;
import com.games.numeral.pursuit.adfree.PlayRNGAct;
import com.games.numeral.pursuit.adfree.R;
import com.games.numeral.pursuit.adfree.Utils;

public final class ChildNewGameMessage extends ParentNewGameMessage {

	public static final String GAME_ID_PARAM="GAME_ID_PARAM";
	public static final String USER_ID_PARAM = "USER_ID_PARAM";

	private ChildNewGameMessage() {
	}

	@Override
	public void onMessageReceived(final Object o){
		Context ctx=(Context) o;
		Blog.i("GameUpdateMessage update", gameId);
		Context context=(Context)o;
		final Intent i =new Intent(context,GameActivity.class);
		i.putExtra(GameActivity.GAME_ID_PARAM, this.gameId);
		i.putExtra(GameActivity.USER_ID_PARAM, this.userId);
		boolean received=false;
		Blog.i("ChildNewGameMessage.onMessageReceived", gameId.toString()+" "+userId.toString()+" "+this.gameType.toString());
		if(this.gameType.equals(ParentNewGameMessage.GAMETYPE_RNG)){
			PlayRNGAct.setState(PlayRNGAct.States.NOT_REGISTERED);
			i.setAction(ChildNewGameMessage.class.getName()+PlayRNGAct.RNG);
			received=LocalBroadcastManager.getInstance(ctx).sendBroadcast(i);
		}
		Blog.i("ChildNewGameMessage.onMessageReceived", this.toString());
		if(!received){
			raiseNotification(ctx,i);
		}
	}

	/*
	 * Stores a ComHandlerControlGame.GameMove with moveNr=0; to start the game.
	 */
	//	private void storeInitMessage() {
	//		GameMove move=new GameMove(clientUserId, opponentUserId, gameId, gridString, playerOne);
	//		move.saveAsLastMove();
	//	}



	private void raiseNotification(final Context context, Intent i){
		int notificationId=MyApplication.getUniqueId();
		i.putExtra(MyApplication.INTENT_PARAM_NOTIFICATION, notificationId);
		Utils.raiseNotificationReturnToLastScreen(context, i, "New Game Started", "Press to play", R.drawable.launcher_icon);
	}
}
