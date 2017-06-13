package com.games.numeral.pursuit.adfree;

import static com.games.common.Constants.SuccessCodes.SUCCESSCODE_SUCCESS;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.appspot.numeralpursuit.gameserver.Gameserver;
import com.appspot.numeralpursuit.gameserver.model.GameInfo;
import com.appspot.numeralpursuit.gameserver.model.GameInfoCollection;
import com.appspot.numeralpursuit.gameserver.model.SuccessCode;
import com.games.common.gsonmessages.ParentGameOverMessage;
import com.games.common.gsonmessages.ParentNewGameMessage;
import com.games.numeral.pursuit.adfree.R;

public class FinishedGamesActivity extends Activity{

	private ProgressDialog genericDlg;
	private ActiveGamesAct.GamesAdapter adapter;
	private ListView listView;
	private boolean flipped=false;
	/**Contains the last clicked game list item*/
	private GameInfo info;
	ViewFlipper flipper;
	private static final String PREFS_FINISHED_GAMES="PREFS_FINISHED_GAMES";
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		setContentView(R.layout.activity_finished_games);
		flipper=(ViewFlipper)findViewById(R.id.viewflipper_finished_games);
		fetchFinishedGames();
		setRemoveAllListener();
		setRefreshListener();
		setSinglePlayerButtonListener();
	}


	private void setRefreshListener() {
		Button button=(Button)findViewById(R.id.button_refresh);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getInfosFromServer();
			}
		});
	}


	private void setSinglePlayerButtonListener() {
		Button button=(Button)findViewById(R.id.button_play_singleplayer);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				FinishedGamesActivity.this.onStartSinglePlayerActivity();
			}
		});
	}


	protected void onStartSinglePlayerActivity() {
		Long gid=info.getGameId();
		Long uid=MyApplication.getInstance().getCurrentUserId();
		Intent i=new Intent(this,GameActivitySinglePlayer.class);
		i.putExtra(GameActivity.GAME_ID_PARAM, gid);
		i.putExtra(GameActivity.USER_ID_PARAM, uid);
		startActivity(i);
	}


	private void setRemoveAllListener() {
		Button remove=(Button)findViewById(R.id.button_remove_all_finished);
		remove.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder=new AlertDialog.Builder(FinishedGamesActivity.this, AlertDialog.THEME_HOLO_DARK).setTitle("Remove old games").
						setMessage("Do you really want to remove all of your old games from your list").
						setNegativeButton("No", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						deleteLocallySavedList();
						new RemoveAllFinishedGamesTask().execute();
						dialog.dismiss();

					}
				});
				builder.create().show();
			}
		});
	}
	private void deleteLocallySavedList(){
		SharedPreferences prefs=getSharedPreferences(PREFS_FINISHED_GAMES, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(MyApplication.getInstance().getCurrentUserId().toString(), null);
		editor.commit();		
	}

	public void showGames(ArrayList<GameInfo> infos) {
		adapter=new ActiveGamesAct.GamesAdapter(this,infos,R.id.list_finished_games,R.layout.list_item_finished_games);
		listView=(ListView)findViewById(R.id.list_finished_games);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(getItemClickListener());
	} 

	private OnItemClickListener getItemClickListener() {
		return new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				FinishedGamesActivity.this.info=adapter.getItem(position);
				FinishedGamesActivity.this.flipToGameInfoScreen();
			}
			
		};
	}

	@Override
	public void onBackPressed(){
		if(flipped){
			flipper.showPrevious();
			flipped=false;
			info=null;
		}
		else{
			super.onBackPressed();
		}
	}
	protected void flipToGameInfoScreen() {
		flipper.showNext();
		flipped=true;
		setFlippedViews();
	}


	private void setFlippedViews() {
		Blog.i("FinishedGamesActivity.setFlippedViews", info);
		TextView v=(TextView)findViewById(R.id.textview_opponent_name);
		v.setText(info.getOpNames());
		v=(TextView)findViewById(R.id.textview_game_name);
		v.setText(info.getGameName());
		v=(TextView)findViewById(R.id.textview_result);
		v.setText(ParentGameOverMessage.interpretResult(info.getResult()));
		v=(TextView)findViewById(R.id.textview_end_due_to);
		v.setText(ParentGameOverMessage.interpretReason(info.getReasonEnded()));
		v=(TextView)findViewById(R.id.textview_gametype);
		v.setText(ParentNewGameMessage.interpretGametype(info.getGameType()));
	}


	private void fetchFinishedGames() {
		ArrayList<GameInfo> infos=getInfosFromLocalStorage();
		if(infos!=null){
			FinishedGamesActivity.this.showGames(infos);
			return;
		}
		getInfosFromServer();

	}
	private void getInfosFromServer() {
		showGenericWaitDlg("Loading games", "Loading your finished games from server, please wait",true, new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface arg0) {
				arg0.dismiss();
				FinishedGamesActivity.this.finish();
			}
		});
		new FetchFinishedGamesTask().execute();		
	}


	private ArrayList<GameInfo> getInfosFromLocalStorage() {
		final SharedPreferences prefs = getSharedPreferences(PREFS_FINISHED_GAMES, Context.MODE_PRIVATE);
		String gameString =prefs.getString(MyApplication.getInstance().getCurrentUserId().toString(), null);
		GameInfoCollection col=GsonConverter.jsonStringToGenericJson(gameString,GameInfoCollection.class);
		try{
			return new ArrayList<GameInfo>(col.getItems());
		}
		catch(Exception e){
			return null;
		}
		
	}
	public void saveFinishedGames(ArrayList<GameInfo> infos) {
		SharedPreferences prefs=getSharedPreferences(PREFS_FINISHED_GAMES, 0);
		SharedPreferences.Editor editor = prefs.edit();
		GameInfoCollection col=new GameInfoCollection().setItems(infos);
		editor.putString(MyApplication.getInstance().getCurrentUserId().toString(), GsonConverter.genericJsonToString(col));
		editor.commit();		
	}


	/**
	 * Creates a generic waiting dialog which is not cancellable by touch on outside
	 * @param title Title of the dialog
	 * @param text Text of the dialog
	 * @param cancelable Set whether or not the dialog is cancelable by backbutton
	 * @param ocl If this parameter is not null, it will be called when the dialog is cancelled by backbutton.
	 */
	private void showGenericWaitDlg(final CharSequence title, final CharSequence text, final boolean cancelable, final OnCancelListener ocl){
		genericDlg=ProgressDialog.show(this, title, text, true);
		genericDlg.setCanceledOnTouchOutside(false);
		genericDlg.setCancelable(cancelable);
		if(ocl!=null){
			genericDlg.setOnCancelListener(ocl);
		}
		genericDlg.show();
	}
	void dismissGenericDialog(){
		if(genericDlg!=null){
			this.genericDlg.dismiss();
		}
	}
	public class FetchFinishedGamesTask extends AsyncTask<Void,Void,ArrayList<GameInfo>>{

		@Override
		protected ArrayList<GameInfo> doInBackground(Void... params) {
			Gameserver service=ServiceFactory.getService(MyApplication.getInstance().getCredential());
			List<GameInfo> info=null;
			int tryTimes=5, tryNr=0;
			while(info==null && tryNr<tryTimes){
				try {
					info = service.gameServerEndpoint().fetchFinishedGames().execute().getItems();
					tryNr=tryTimes+1;
				} catch (Exception e) {
					e.printStackTrace();
					tryNr++;
				}
				Blog.i("FetchFinishedGamesTask.doInBackground", info);
			}
			return info!=null?new ArrayList<GameInfo>(info):null;
		}
		@Override
		protected void onPostExecute(ArrayList<GameInfo> info){
			dismissGenericDialog();
			if(info!=null){
				saveFinishedGames(info);
				FinishedGamesActivity.this.showGames(info);
			}
		}
	}
	public class RemoveAllFinishedGamesTask extends AsyncTask<Void,Void,SuccessCode>{

		@Override
		protected SuccessCode doInBackground(Void... params) {
			Gameserver service=ServiceFactory.getService(MyApplication.getInstance().getCredential());
			SuccessCode code=null;
			int tryTimes=5, tryNr=0;
			while(code==null && tryNr<tryTimes){
				try {
					code = service.gameServerEndpoint().removeAllFinishedGames().execute();
					tryNr=tryTimes+1;
				} catch (Exception e) {
					e.printStackTrace();
					tryNr++;
				}
				Blog.i("FetchFinishedGamesTask.doInBackground", code);
			}
			return code;
		}
		@Override
		protected void onPostExecute(SuccessCode code){
			dismissGenericDialog();
			if(code.getResult().equals(SUCCESSCODE_SUCCESS)){
				FinishedGamesActivity.this.showGames(new ArrayList<GameInfo>());
			}
		}
	}

}
