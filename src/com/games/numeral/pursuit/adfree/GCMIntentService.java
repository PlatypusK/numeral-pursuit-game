package com.games.numeral.pursuit.adfree;


import static com.games.common.Constants.GCM.MESSAGE;
import android.content.Context;
import android.content.Intent;

import com.games.common.gsonmessages.GsonMessage;
import com.games.numeral.pursuit.messages.GsonToLocalConverter;
import com.google.android.gcm.GCMBaseIntentService;
public class GCMIntentService extends GCMBaseIntentService{

	public GCMIntentService(){
		super(com.games.common.Constants.AppengineAuthIds.PROJECT_NUMBER);
		Blog.v("Service started");
	}
	@Override
	protected void onError(final Context arg0, final String arg1) {
		Blog.v(arg1);
		// TODO Auto-generated method stub
	}

	@Override
	protected void onMessage(final Context arg0, final Intent arg1) {
		final String message=arg1.getStringExtra(MESSAGE);
		Blog.v("message: ",message);

		try{
			GsonMessage gsonMes=GsonMessage.getAsGsonMessage(message,GsonToLocalConverter.getConversionMap());
			if(gsonMes==null){
				return;
			}
			gsonMes.onMessageReceived(this);
			return;
		}
		catch(IllegalArgumentException e){
			e.printStackTrace();
		}
	}

	@Override
	protected void onRegistered(final Context arg0, final String regId) {
		Blog.d("regID: ",regId);
	}

	@Override
	protected void onUnregistered(final Context arg0, final String arg1) {
		Blog.d("regID: ",arg1);
	}

}