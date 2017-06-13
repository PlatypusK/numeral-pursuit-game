package com.games.numeral.pursuit.adfree;





import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.appspot.numeralpursuit.gameserver.Gameserver;
import com.appspot.numeralpursuit.gameserver.model.SuccessCode;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

abstract public class AbstractAlarmReceiver extends BroadcastReceiver {
	private final String REMINDER_BUNDLE = "MyReminderBundle";

	// this constructor is called by the alarm manager.
	public AbstractAlarmReceiver(){ }

	/**Create and set an alarm*/
	public AbstractAlarmReceiver(Context context, Bundle extras, int timeoutInSeconds, Class<?> clazz){
		AlarmManager alarmMgr = 
				(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, AbstractAlarmReceiver.class);
		intent.putExtra(REMINDER_BUNDLE, extras);
		intent.setClass(context, clazz);
		PendingIntent pendingIntent =
				PendingIntent.getBroadcast(context, 0, intent, 
						PendingIntent.FLAG_UPDATE_CURRENT);
		Calendar time = Calendar.getInstance();
		time.setTimeInMillis(System.currentTimeMillis());
		time.add(Calendar.SECOND, timeoutInSeconds);
		alarmMgr.set(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(),
				pendingIntent);
	}


	@Override
	abstract public void onReceive(Context context, Intent intent);
	
	public static class RepeatRegistration extends AbstractAlarmReceiver{
		public RepeatRegistration(){
			//this is here for the alarm to create an object
		}
		public RepeatRegistration(Context context, Bundle extras, int timeoutInSeconds){
			super(context,extras,timeoutInSeconds,RepeatRegistration.class);
		}
		public static final int REP_IN_SECONDS=60*15;
		@Override
		public void onReceive(Context context, Intent intent) {
			Blog.i("alarm received");
			if(PlayRNGAct.getState()==PlayRNGAct.States.REGISTERED){
				regKeepAlive();
				new RepeatRegistration(context,intent.getExtras(),REP_IN_SECONDS);
			}
		}

		private void regKeepAlive() {
			new Thread(new Runnable(){
				@Override
				public void run() {
					Blog.i("reregistering");
					Gameserver service;
					SuccessCode code = null;
					GoogleAccountCredential cred=MyApplication.getInstance().getCredential();
					service=ServiceFactory.getService(cred);
					int tryNr=0,tryTimes=5;
					while(code==null&&tryNr<tryTimes){
						try {
							code=service.gameServerEndpoint().registerForRandomGame().execute();
							tryNr=tryTimes+1;
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							tryNr++;
						}
					}
					Blog.i("SuccessCode from reregistration: ",code);					
				}
			}).start();

		}
	}
}
