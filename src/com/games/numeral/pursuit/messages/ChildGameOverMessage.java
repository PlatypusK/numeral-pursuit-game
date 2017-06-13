package com.games.numeral.pursuit.messages;

import android.content.Context;
import android.widget.Toast;

import com.games.common.gsonmessages.ParentGameOverMessage;

public final class ChildGameOverMessage extends ParentGameOverMessage {
	private Context context;

	private ChildGameOverMessage(){
		super();
	}

	@Override
	public void onMessageReceived(Object o){
		context=(Context)o;
		Toast.makeText(context, "ChildGameOverMessage received", Toast.LENGTH_LONG).show();
	}
}
