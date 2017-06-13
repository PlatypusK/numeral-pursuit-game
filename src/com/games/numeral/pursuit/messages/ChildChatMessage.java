package com.games.numeral.pursuit.messages;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.games.common.gsonmessages.ParentChatMessage;
import com.games.numeral.pursuit.adfree.Blog;
import com.games.numeral.pursuit.adfree.GameActivity;
import com.games.numeral.pursuit.adfree.MyApplication;
import com.games.numeral.pursuit.adfree.R;
import com.games.numeral.pursuit.adfree.Utils;

public class ChildChatMessage extends ParentChatMessage {
	public static final String CHAT_MESSAGE_PARAM = "CHAT_MESSAGE_PARAM";
	public static final String ACTION_CHAT=ChildChatMessage.class.getName();
	private ChildChatMessage(){
	}

	@Override
	public void onMessageReceived(final Object o) {
		Blog.i("GameUpdateMessage update", gameId);
		Context context=(Context)o;
		final Intent i =new Intent(context,GameActivity.class);
		i.putExtra(GameActivity.GAME_ID_PARAM, gameId);
		i.putExtra(GameActivity.USER_ID_PARAM, this.receiverId);
		i.putExtra(CHAT_MESSAGE_PARAM, this.message);
		i.setAction(ChildChatMessage.class.getName()+gameId.toString()+receiverId.toString());

		String chatLogsKey=this.receiverId.toString()+this.gameId.toString();
		if(GameActivity.chatLogs.get(chatLogsKey)!=null){
			GameActivity.chatLogs.get(chatLogsKey).add(opName+": "+this.message);
		}
		else{
			GameActivity.chatLogs.put(chatLogsKey, new ArrayList<String>());
			GameActivity.chatLogs.get(chatLogsKey).add(opName+": "+this.message);
		}
		boolean received=LocalBroadcastManager.getInstance((Context)o).sendBroadcast(i);
		if(!received){
			raiseNotification(i, context);
		}


	}
	private void raiseNotification(Intent i, Context context) {
		if(MyApplication.getInstance().getSettings().getNotifyGameChatUpdates()){
			i.setAction(ACTION_CHAT);
			Utils.raiseNotificationReturnToLastScreen(context, i, "New message",this.opName +" said something",R.drawable.launcher_icon);
		}
		Blog.i("GameUpdateMessage.raiseNotification", "activity not in foreground, raise notification");
	}
}
