package com.games.numeral.pursuit.messages;



import android.content.Context;
import android.content.Intent;

import com.games.common.gsonmessages.ParentGameInviteMessage;
import com.games.numeral.pursuit.adfree.InvitedToGameActivity;
import com.games.numeral.pursuit.adfree.R;
import com.games.numeral.pursuit.adfree.Utils;

public final class ChildGameInviteMessage extends ParentGameInviteMessage{

	private ChildGameInviteMessage(){
	}
	@Override
	public void onMessageReceived(Object o){
		raiseNotificationWithTimeLimitForUserResponse((Context)o);
	}
	private void raiseNotificationWithTimeLimitForUserResponse(Context context) {
		Intent resultIntent = new Intent(context, InvitedToGameActivity.class);
		resultIntent.putExtra(InvitedToGameActivity.INTENT_PARAM_SENDER_NAME, this.senderName);
		resultIntent.putExtra(InvitedToGameActivity.INTENT_PARAM_SENDER_ID, this.senderId);
		resultIntent.putExtra(InvitedToGameActivity.INTENT_PARAM_MAX_RESPONSE_TIME, this.maxResponseTime);
		resultIntent.putExtra(InvitedToGameActivity.INTENT_PARAM_RECEIVER_ID, this.receiverId);
		resultIntent.putExtra(InvitedToGameActivity.INTENT_PARAM_RECEIVER_NAME, this.receiverName);
		Utils.raiseNotificationReturnToLastScreen(context, resultIntent, "Game invite", "Somebody wants to play with you", R.drawable.launcher_icon);
	}
}
