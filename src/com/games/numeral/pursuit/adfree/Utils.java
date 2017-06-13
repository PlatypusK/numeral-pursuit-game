package com.games.numeral.pursuit.adfree;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class Utils {

	public static void raiseNotificationReturnToLastScreen(Context context, Intent i, String title, String text, int drawableId) {
		i.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		PendingIntent pIntent = PendingIntent.getActivity(context, 0, i, 0);
		Notification n  = new NotificationCompat.Builder(context)
		.setSmallIcon(drawableId)
		.setContentTitle(title)
		.setContentText(text)
		.setContentIntent(pIntent)
		.setAutoCancel(true).build();
		NotificationManager notificationManager =(NotificationManager) MyApplication.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(0, n);
	}

}
