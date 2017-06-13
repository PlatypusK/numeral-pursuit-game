package com.games.numeral.pursuit.adfree;


import static com.games.common.Constants.AppengineAuthIds.CLIENT_ID_FOR_WEB_APPLICATION;
import static com.games.common.Constants.AppengineAuthIds.PROJECT_NUMBER;
import static com.google.android.gms.common.ConnectionResult.DATE_INVALID;
import static com.google.android.gms.common.ConnectionResult.SERVICE_DISABLED;
import static com.google.android.gms.common.ConnectionResult.SERVICE_INVALID;
import static com.google.android.gms.common.ConnectionResult.SERVICE_MISSING;
import static com.google.android.gms.common.ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED;
import static com.google.android.gms.common.ConnectionResult.SUCCESS;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.appspot.numeralpursuit.gameserver.Gameserver;
import com.appspot.numeralpursuit.gameserver.model.RegIdCGM;
import com.appspot.numeralpursuit.gameserver.model.Settings;
import com.games.numeral.pursuit.adfree.R;
import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

public class MainActivity extends Activity implements View.OnClickListener {


	long initTime;
	private static int CREDENTIALS_REQUEST_CODE=1;
	public final static String WEB_CLIENT_ID=CLIENT_ID_FOR_WEB_APPLICATION;
	private static final String IS_FIRST_RUN_MAIN_ACTIVITY = "IS_FIRST_RUN_MAIN_ACTIVITY";
	private ProgressDialog conDlg;
	protected GoogleAccountCredential credential;
	protected Gameserver service;
	private CountDownLatch latch;
	private final RegIdCGM regWrapper=new RegIdCGM();
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if(isFirstRun()){
			onFirstRun();
		}
		else{
			onAfterFirstRun();
		}

	}
	private void onAfterFirstRun() {
		int or=this.getResources().getConfiguration().orientation;
		this.getResources().getConfiguration();
		if(or==Configuration.ORIENTATION_PORTRAIT){
			setListeners();
			ifBadGooglePlayServicesExit();
		}
	}
	private void onFirstRun() {
		AlertDialog.Builder builder=new AlertDialog.Builder(this).setTitle("Hints").setMessage(getResources().getString(R.string.gameexplanation)).setPositiveButton("Ok", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				onAfterFirstRun();
			}
		});
		builder.create().show();
	
	}
	protected void onResume(){
		super.onResume();
		if(PlayRNGAct.getState()==PlayRNGAct.States.REGISTERED){
			new AbstractAlarmReceiver.RepeatRegistration(getApplicationContext(), null, AbstractAlarmReceiver.RepeatRegistration.REP_IN_SECONDS);
		}
	}
	protected void onPause(){
		super.onPause();
		if(conDlg!=null){
			conDlg.dismiss();
		}
	}
	private void ifBadGooglePlayServicesExit() {
		String error="";
		int availability=GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		switch(availability){
		case SUCCESS:
			latch=new CountDownLatch(1);
			new GCMRegistryTask().execute(regWrapper);
			registerWithAppengine();
			return;
		case SERVICE_MISSING:
			error="Google Play Services need to be installed on your device to use this application. It is not installed at present, exiting";
			break;
		case SERVICE_VERSION_UPDATE_REQUIRED:
			error="Google Play Services need to be installed and current on your device to use this application. The present installation is out of date, exiting";
			break;
		case SERVICE_DISABLED:
			error="Google Play Services need to be installed and enabled on your device to use this application. It is disabled at present, exiting";
			break;
		case SERVICE_INVALID:
			error="Google Play Services need to be installed and valid on your device to use this application. Your installation is not authentic, exiting";
			break;
		case DATE_INVALID:
			error="Google Play Services need to be installed on your device to use this application. It needs a valid date to function, there is a problem with your devices date settings, exiting";
			break;
		}
		exitDueToError(error, 5000);
	}
	private void registerWithAppengine() {
		credential = GoogleAccountCredential.usingAudience(MyApplication.getInstance(), "server:client_id:"+WEB_CLIENT_ID);
		Blog.i("scope: ",credential.getScope());
		startActivityForResult(credential.newChooseAccountIntent(), CREDENTIALS_REQUEST_CODE);
	}
	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

		if (requestCode == CREDENTIALS_REQUEST_CODE) {
			Blog.i("Credentials request code: ",resultCode);
			if(resultCode == RESULT_OK){
				showConnectingDialog();
				final Bundle b=data.getExtras();
				this.credential.setSelectedAccountName(b.getString("authAccount"));
				Blog.i("keyset: ",b.keySet(),"\n","accounttype: ", b.getString("accountType"), "\n", "authAccount: ",b.getString("authAccount"));
				MyApplication.getInstance().setCredential(this.credential);
				initTime=System.currentTimeMillis();
				new LoginTask().execute(regWrapper);

			}
			if (resultCode == RESULT_CANCELED) {
				Toast.makeText(getApplicationContext(), "Need to register with a google account to play, exiting", Toast.LENGTH_LONG).show();
				this.finish();
			}
		}
	}

	private void setListeners() {
		((Button)findViewById(R.id.button1)).setOnClickListener(this);
		((Button)findViewById(R.id.button2)).setOnClickListener(this);
		((Button)findViewById(R.id.button3)).setOnClickListener(this);
		((Button)findViewById(R.id.button4)).setOnClickListener(this);
		((Button)findViewById(R.id.button6)).setOnClickListener(this);		
	}
	private void showConnectingDialog(){
		conDlg=ProgressDialog.show(this, "Connecting",
				"Connecting to server, please wait", true);
		conDlg.setCanceledOnTouchOutside(false);
		conDlg.setCancelable(true);
		conDlg.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				MainActivity.this.conDlg.dismiss();
				MainActivity.this.finish();
			}
		});
		conDlg.show();
	}
	@Override
	public void onClick(final View v) {
		switch(v.getId()){
		case R.id.button1:
			startActivity(new Intent(this, FriendsActivity.class));
			break;
		case R.id.button2:
			startActivity(new Intent(this, PlayRNGAct.class));
			break;
		case R.id.button3:
			startActivity(new Intent(this, ActiveGamesAct.class));
			break;
		case R.id.button4:
			startActivity(new Intent(this, FinishedGamesActivity.class));
			break;
		case R.id.button6:
			startActivity(new Intent(this, SettingsActivity.class));
			break;
		}
	}
	void onRegistrationReturn(final boolean registered){
		if(!registered){
			MainActivity.this.exitDueToError("Error registering with GCM, exiting", 3000);
		}
	}
	private void exitDueToError(final String exitMessage, final long showForMillisec) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if(MainActivity.this.conDlg==null){
					showConnectingDialog();
				}
				MainActivity.this.conDlg.setMessage(exitMessage);
			}
		});
		new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					Thread.sleep(showForMillisec);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if(conDlg!=null){
							conDlg.dismiss();
						}
						MainActivity.this.finish();				            }
				});

			}
		}).start();		
	}

	private class GCMRegistryTask extends AsyncTask<RegIdCGM, Void, Void>{

		@Override
		protected Void doInBackground(RegIdCGM... wrapper) {
			final String[] SENDER_ID=new String[]{PROJECT_NUMBER};
			try{
			GCMRegistrar.checkDevice(MainActivity.this);
			}catch(Exception e){
				e.printStackTrace();
				return null;
			}
			try{
				GCMRegistrar.checkManifest(MainActivity.this);
			}catch(Exception e){
				return onError(e,wrapper);
			}
			GoogleCloudMessaging gcm=GoogleCloudMessaging.getInstance(MainActivity.this);
			String gcmId="";
			try {
				gcmId=gcm.register(SENDER_ID);
			} catch (IOException e) {
				return onError(e,wrapper);
			}
			Blog.v("registered with gcm address: "+gcmId);
			Blog.i(time());
			wrapper[0].setRegId(gcmId);
			latch.countDown();
			return null;
		}
		private Void onError(Exception e,RegIdCGM[] wrapper){
			e.printStackTrace();
			wrapper[0].setRegId(null);
			latch.countDown();
			return null;
		}
	}
	public class LoginTask extends AsyncTask<RegIdCGM,Void,Settings> {

		@Override
		protected Settings doInBackground(RegIdCGM... wrapper) {
			try {
				Blog.i(latch.getCount());
				latch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Blog.i(latch.getCount());

			if(wrapper[0].getRegId()==null){
				exitDueToError("Failed to register with GCM, exiting", 3000);
				return null;
			}
			Gameserver service=ServiceFactory.getService(MyApplication.getInstance().getCredential());
			Settings settings=null;
			int tryTimes=5, tryNr=0;
			while(settings==null && tryNr<tryTimes){
				try {
					settings = service.gameServerEndpoint().login(wrapper[0]).execute();
					Blog.i("Settings in loop: ",settings);
					tryNr=tryTimes+1;
				} catch (Exception e) {
					e.printStackTrace();
					tryNr++;
				}
				Blog.i(settings);
			}
			if(settings==null){
				exitDueToError("Networking error, exiting", 3000);
				return null;
			}
			MyApplication.getInstance().updateCurrentUserId(settings.getUserId());
			MyApplication.getInstance().setSettings(settings);
			return settings;
		}
		@Override
		protected void onPostExecute(Settings set){
			if(set==null){
				return;
			}
			onSettingsFetched();
			Blog.i(time());
		}

	}
	/**Elapsed times since login started*/
	private long time(){
		return System.currentTimeMillis()-this.initTime;
	}
	public void onSettingsFetched() {
		if(conDlg!=null){
			conDlg.dismiss();
		}
	}
	private boolean isFirstRun() {
		SharedPreferences prefs=getSharedPreferences(MyApplication.APPLICATION_DATA, 0);
		boolean isFirst=prefs.getBoolean(IS_FIRST_RUN_MAIN_ACTIVITY, true);
		if(isFirst){
			SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean(IS_FIRST_RUN_MAIN_ACTIVITY, false);
			editor.commit();		
		}
		return isFirst;
	}
}
