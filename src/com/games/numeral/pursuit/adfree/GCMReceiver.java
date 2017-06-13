package com.games.numeral.pursuit.adfree;

import android.content.Context;

import com.google.android.gcm.GCMBroadcastReceiver;

public class GCMReceiver extends GCMBroadcastReceiver { 
    @Override
	protected String getGCMIntentServiceClassName(final Context context) { 
		return GCMIntentService.class.getName(); 
	}
}