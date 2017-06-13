package com.games.numeral.pursuit.adfree;

import static com.games.common.Constants.SuccessCodes.SUCCESSCODE_SUCCESS;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.appspot.numeralpursuit.gameserver.Gameserver;
import com.appspot.numeralpursuit.gameserver.model.SuccessCode;
import com.games.numeral.pursuit.messages.ChildNewGameMessage;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;

public class PlayRNGAct extends Activity{
	NewGameReceiver receiver;

	public class NewGameReceiver extends BroadcastReceiver {

		public final IntentFilter intentFilter = new IntentFilter(ChildNewGameMessage.class.getName()+PlayRNGAct.RNG);

		@Override
		public void onReceive(final Context context, final Intent intent) {
			final Bundle b=intent.getExtras();
			Blog.v("GameIdBroadcastReceiver.onReceive bundle",b);
			final Long gameId=b.getLong(ChildNewGameMessage.GAME_ID_PARAM);
			final Long userId=b.getLong(ChildNewGameMessage.USER_ID_PARAM);
			PlayRNGAct.this.runOnUiThread(new Runnable(){
				@Override
				public void run() {
					PlayRNGAct.this.startGame(gameId,userId);					
				}
			});
		}
	}
	ProgressDialog regDlg;
	ProgressDialog waitDlg;
	public static final String STATE=PlayRNGAct.class.getName()+":"+"STATE";
	public static final String RNG = "RNG";

	/**
	 * Keeps a current state in a static variable. If the state is initialstate when the variable is checked, fetch the state from disk as this is either the first run
	 * or the process has been shut down so that the static variable has been reset.
	 * @author Ketil
	 *
	 */
	public static class States{
		public static final int INITIALSTATE=-1;
		public static final int NOT_REGISTERED=0;
		public static final int STARTING_GAME=1;
		public static final int REGISTERED=2;
		private static int currentState=INITIALSTATE;
	}
	public static void setState(final int state){
		States.currentState=state;
		SharedPreferences settings = MyApplication.getInstance().getSharedPreferences(PlayRNGAct.class.getName(), 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(STATE, state);
		editor.commit();
	}
	public void startGame(final Long gameId, final Long userId) {
		this.dismissDialogs();
		Intent i=new Intent(this,GameActivity.class);
		i.putExtra(GameActivity.GAME_ID_PARAM, gameId);
		i.putExtra(GameActivity.USER_ID_PARAM, userId);
		startActivity(i);
		setState(States.NOT_REGISTERED);
		this.finish();
	}
	public static int getState(){
		if(States.currentState==States.INITIALSTATE){
			SharedPreferences settings = MyApplication.getInstance().getSharedPreferences(PlayRNGAct.class.getName(), 0);
			return settings.getInt(STATE, States.INITIALSTATE);
		}
		else return States.currentState;
	}
	@Override
	public void onCreate(final Bundle saved){
		super.onCreate(saved);
		int state=getState();
		if(state==States.INITIALSTATE || state==States.NOT_REGISTERED){
			showRegisteringDialog();
			new RegisterTask().execute();
		}
		else if(state==States.STARTING_GAME){
			setState(States.NOT_REGISTERED);
			showRegisteringDialog();
			new RegisterTask().execute();
		}
		else if (state==States.REGISTERED){
			new RegisterTask().execute();
			showWaitingForOpponentDlg();
		}
	}
	@Override
	public void onResume(){
		super.onResume();
		this.receiver=new NewGameReceiver();
		LocalBroadcastManager.getInstance(this).registerReceiver(new NewGameReceiver(), receiver.intentFilter);
	}
	@Override
	public void onPause(){
		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
		dismissDialogs();
		super.onPause();
	}

	private void showRegisteringDialog(){
		regDlg=ProgressDialog.show(this, "Registering",
				"Registering for game", true);
		regDlg.setCanceledOnTouchOutside(false);
		regDlg.setCancelable(true);
		regDlg.show();
	}
	private void showWaitingForOpponentDlg() throws android.view.WindowManager.BadTokenException {
		waitDlg=ProgressDialog.show(this, "Registered",
				"Registered and waiting for opponent. You may leave the game in the background while you wait by pressing the home key. Press the backbutton to unregister", true);
		waitDlg.setCanceledOnTouchOutside(false);
		waitDlg.setCancelable(true);
		waitDlg.setOnCancelListener(new OnCancelListener(){

			@Override
			public void onCancel(final DialogInterface dialog) {
				PlayRNGAct.this.exitToMain();
			}
		});
		waitDlg.show();
	}

	public void exitToMain(){
		showBackDialog();

	}
	private void showBackDialog() {
		new AlertDialog.Builder(this)
		.setTitle("Stay registered?")
		.setMessage("You are about to leave for the main menu, do you want to stay registered?")
		.setNegativeButton("No(unregister)", new OnClickListener(){

			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				PlayRNGAct.this.unregister();
				PlayRNGAct.setState(States.NOT_REGISTERED);
				dialog.dismiss();
				PlayRNGAct.super.onBackPressed();
			}

		})
		.setPositiveButton("Yes(stay registered)", new OnClickListener() {
			@Override
			public void onClick(final DialogInterface arg0, final int arg1) {
				arg0.dismiss();
				PlayRNGAct.super.onBackPressed();
			}
		}).create().show();
	}
	protected void unregister() {
		new UnRegisterTask().execute();

	}
	private void dismissDialogs(){
		try{
			if(regDlg!=null){
				regDlg.dismiss();
				regDlg=null;
			}
			if(waitDlg!=null){
				waitDlg.dismiss();
				waitDlg=null;
			}
		}catch(Exception e){
			e.printStackTrace();
			regDlg=null;
			waitDlg=null;
		}

	}

	private class RegisterTask extends AsyncTask<Void, Void, String>{

		@Override
		protected String doInBackground(final Void... params) {
			Gameserver service;
			SuccessCode code = null;
			GoogleAccountCredential cred=MyApplication.getInstance().getCredential();
			ExponentialBackOff.Builder b=new ExponentialBackOff.Builder();
			cred.setBackOff(b.setInitialIntervalMillis(100).setMaxElapsedTimeMillis(3000).build());
			service=ServiceFactory.getService(cred);
			int tryNr=0,tryTimes=5;
			while(code==null&&tryNr<tryTimes){
				try {
					code=service.gameServerEndpoint().registerForRandomGame().execute();
					tryNr=tryTimes+1;
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					tryNr++;
				}
			}
			Blog.i(code.toString());
			return code.getResult();
		}
		@Override
		protected void onPostExecute(final String successCode){
			if(!successCode.equals(SUCCESSCODE_SUCCESS)){
				Toast.makeText(getApplicationContext(), "Could not register", Toast.LENGTH_SHORT).show();
				PlayRNGAct.this.finish();
			}
			else{
				Toast.makeText(PlayRNGAct.this, "Registered", Toast.LENGTH_SHORT).show();
				PlayRNGAct.this.dismissDialogs();
				//Resolves a race condition where this asynctask finishes after reception of a new game over gcm
				if(getState()!=States.STARTING_GAME){
					PlayRNGAct.setState(States.REGISTERED);
					Blog.i("setting alarm receiver");
					new AbstractAlarmReceiver.RepeatRegistration(getApplicationContext(), null, AbstractAlarmReceiver.RepeatRegistration.REP_IN_SECONDS);
					try{
						PlayRNGAct.this.showWaitingForOpponentDlg();
					}
					catch(android.view.WindowManager.BadTokenException e){

					}
				}
			}
		}
	}
	private class UnRegisterTask extends AsyncTask<Void, Void, Void>{
		@Override
		protected Void doInBackground(final Void... params) {
			Gameserver service;
			SuccessCode code = null;
			GoogleAccountCredential cred=MyApplication.getInstance().getCredential();
			ExponentialBackOff.Builder b=new ExponentialBackOff.Builder();
			cred.setBackOff(b.setInitialIntervalMillis(100).setMaxElapsedTimeMillis(3000).build());
			service=ServiceFactory.getService(cred);
			try {
				code=service.gameServerEndpoint().unregisterForRandomGame().execute();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			Blog.i(code);
			return null;
		}
	}
}
