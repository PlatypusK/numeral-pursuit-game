package com.games.numeral.pursuit.adfree;

import java.io.File;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.appspot.numeralpursuit.gameserver.Gameserver;
import com.appspot.numeralpursuit.gameserver.model.Settings;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

public class MyApplication extends Application {
	private static final String EMAIL_PREF_KEY="email";
	private static MyApplication instance;
	private GoogleAccountCredential credential;
	private static Long currentUserId=null;
	static final String APPLICATION_DATA="APPLICATION_DATA";
	private static final String UID_KEY="USER_ID_KEY";
	private Gameserver service;
	private Settings settings=null;
	private String backgroundImagePath;
	private static int uniqueId=1;
	//	private ActivityRecvGCM recvGCM;
	//	private GameActivity gameActivity;
	public static final String APPLICATION_NAME="com.games.numeral.pursuit";
	//	private SparseArray<ComHandler> comHandlers=new SparseArray<ComHandler>();
	public static final String INTENT_PARAM_NOTIFICATION = "INTENT_PARAM_NOTIFICATION";
	private static final String SETTINGS_KEY = "SETTINGS_KEY";
	static final String DEFAULT_BACKGROUND=new File(FileHandler.BACKGROUNDS_DIRECTORY,FileHandler.DEFAULT_FILE).getAbsolutePath();
	public static final String KEY_BACKGROUND_PATH = "KEY_BACKGROUND_PATH";
	private static final String GCM_TOKEN_KEY = "GCM_TOKEN_KEY";


	@Override
	public void onCreate() {
		super.onCreate();
		FileHandler.copyAssets(this);
		instance=this;
		Log.i("MyApplication.onCreate","Creating application");
	}

	public static MyApplication getInstance(){	
		return instance;
	}
	public GoogleAccountCredential getCredential() {
		if(credential==null){
			Blog.i("MyApplication.getCredential","fetching stored credential");
			getSavedCredentials();
		}
		return credential;
	}
	private void getSavedCredentials() {
		credential = GoogleAccountCredential.usingAudience(MyApplication.getInstance(), "server:client_id:"+MainActivity.WEB_CLIENT_ID);
		final SharedPreferences prefs = this.getSharedPreferences(APPLICATION_DATA, Context.MODE_PRIVATE);
		if(prefs==null) throw new IllegalArgumentException("Missing files, restart application");
		final String email= prefs.getString(EMAIL_PREF_KEY, null);
		if(email==null) throw new IllegalArgumentException("Corrupt file, restart application");
		credential.setSelectedAccountName(email);
	}
	public void setCredential(final GoogleAccountCredential credential) {
		this.credential = credential;
		storeCurrentCredential();
	}
	private void storeCurrentCredential() {
		final SharedPreferences prefs = this.getSharedPreferences(
				APPLICATION_DATA, Context.MODE_PRIVATE);
		final String email= credential.getSelectedAccount().name;
		System.out.println(email);
		prefs.edit().putString(EMAIL_PREF_KEY, email).commit();
	}
	public Gameserver getService() {
		return service;
	}
	public void setService(final Gameserver service) {
		this.service = service;
	}
	public synchronized void updateCurrentUserId(final Long newUserId){
		if(newUserId==null) throw new NullPointerException("newUserId argument can not be null");
		if(newUserId==getCurrentUserId()){
			return;
		}
		else{
			storeNewUserId(newUserId);
			MyApplication.currentUserId=newUserId;
			return;
		}
	}
	private synchronized void storeNewUserId(final Long newUserId) {
		SharedPreferences prefs=getSharedPreferences(MyApplication.APPLICATION_DATA, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(UID_KEY+this.getCredential(), Long.toString(newUserId));
		editor.commit();
	}
	public synchronized Long getCurrentUserId() {
		if(currentUserId!=null){
			return currentUserId;
		}
		else{
			currentUserId=getStoredUserId();
			return currentUserId;
		}
	}
	Long getStoredUserId() {
		final SharedPreferences prefs = getSharedPreferences(APPLICATION_DATA, Context.MODE_PRIVATE);
		final String currentString= prefs.getString(UID_KEY+this.getCredential(), null);
		if(currentString==null) return null;
		return Long.parseLong(currentString);
	}
	public static synchronized int getUniqueId(){
		if(uniqueId==0) return ++uniqueId;
		return uniqueId++;
	}
	public void setSettings(Settings settings) {
		Blog.i("MyApplication.setSettings", settings);
		this.settings=settings;
		saveSettings();
	}
	private void saveSettings() {
		SharedPreferences prefs=getSharedPreferences(MyApplication.APPLICATION_DATA, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(SETTINGS_KEY+getCurrentUserId(), GsonConverter.genericJsonToString(settings));
		editor.commit();		
	}
	public Settings getSettings(){
		if(settings==null){
			loadSettings();
		}
		return settings;
	}
	private void loadSettings() {
		final SharedPreferences prefs = getSharedPreferences(APPLICATION_DATA, Context.MODE_PRIVATE);
		String settingString =prefs.getString(SETTINGS_KEY+getCurrentUserId(), null);
		this.settings=GsonConverter.jsonStringToGenericJson(settingString, Settings.class);
		}
	public String getBackgroundImagePath() {
		if(this.backgroundImagePath==null){
			String backFromFile=getSharedPreferences(MyApplication.APPLICATION_DATA, 0).getString(KEY_BACKGROUND_PATH+getCurrentUserId(), null);
			return backFromFile!=null?backFromFile:DEFAULT_BACKGROUND;
		}
		else{
			return this.backgroundImagePath;
		}
	}
	public void setBackGroundImagePath(String path){
		this.backgroundImagePath=path;
		SharedPreferences prefs=getSharedPreferences(MyApplication.APPLICATION_DATA, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(SETTINGS_KEY+getCurrentUserId(), path);
		editor.commit();
	}

	public void saveGCMToken(String token) {
		SharedPreferences prefs=getSharedPreferences(MyApplication.APPLICATION_DATA, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(GCM_TOKEN_KEY, token);
		editor.commit();				
	}

	public String getGCMToken() {
		final SharedPreferences prefs = getSharedPreferences(APPLICATION_DATA, Context.MODE_PRIVATE);
		return prefs.getString(GCM_TOKEN_KEY, null);
	}
}
