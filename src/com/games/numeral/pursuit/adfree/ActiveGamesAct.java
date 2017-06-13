package com.games.numeral.pursuit.adfree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.ActivityInfo;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.appspot.numeralpursuit.gameserver.Gameserver;
import com.appspot.numeralpursuit.gameserver.model.GameInfo;
import com.games.common.gsonmessages.ParentGameOverMessage;
import com.games.numeral.pursuit.adfree.R;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

/**
 * This activity shows a list of all the games you currently have active and lets you choose one to play.
 * @author Ketil
 *
 */
public class ActiveGamesAct extends Activity {


	ProgressDialog genericDlg;
	final int[] to=new int[]{R.id.game_name,R.id.opponent_nick};


	Long waitGameUpdate;
	public static final String GID_MAPPED="GID_MAPPED";
	public static final String OPID_MAPPED="OPID_MAPPED";
	public static final String GAMETYPE_MAPPED = "GAMETYPE_MAPPED";
	public static final String GAME_NAME_MAPPED="GAME_NAME_MAPPED";
	public static final String OP_NAME_MAPPED="OP_NAME_MAPPED";
	public static final String RESULT_MAPPED="RESULT_MAPPED";

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		showGenericWaitDlg( "Fetching list","Connecting to server and fetching games list", true,new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				ActiveGamesAct.this.finish();
			}
		} );
		new FetchGamesTask().execute();
	}
	public void onFetchedGames(final ArrayList<GameInfo> listMap){
		if(genericDlg!=null){
			genericDlg.dismiss();
		}
		setContentView(R.layout.activity_active_games);
		ListView v=(ListView) findViewById(R.id.list_active_games);
		GamesAdapter dap=new GamesAdapter(this,listMap,R.id.list_active_games,R.layout.list_item_active_games);
		//		SimpleAdapter dap=new SimpleAdapter(this, listMap, R.layout.active_games_item, from, to);
		v.setAdapter(dap);
		v.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(final AdapterView<?> arg0, final View arg1, final int position,
					final long id) {
				startGame(listMap,position);

			}
		});
	}

	protected void startGame(final ArrayList<GameInfo> listMap, int position) {
		this.dismissGenericDialog();
		final Intent i =new Intent(this,GameActivity.class);
		i.putExtra(GameActivity.GAME_ID_PARAM,listMap.get(position).getGameId());
		i.putExtra(GameActivity.USER_ID_PARAM, MyApplication.getInstance().getCurrentUserId());
		startActivity(i);
	}
	private void dismissGenericDialog(){
		if(genericDlg!=null){
			this.genericDlg.dismiss();
		}
	}
	/**
	 * Creates a generic waiting dialog which is not cancellable by touch on outside
	 * @param title Title of the dialog
	 * @param text Text of the dialog
	 * @param cancelable Set whether or not the dialog is cancelable by backbutton
	 * @param ocl If this parameter is not null, it will be called when the dialog is cancelled by backbutton.
	 */
	private void showGenericWaitDlg(final CharSequence title, final CharSequence text, final boolean cancelable, final OnCancelListener ocl){
		dismissGenericDialog();
		genericDlg=ProgressDialog.show(this, title, text, true);
		genericDlg.setCanceledOnTouchOutside(false);
		genericDlg.setCancelable(cancelable);
		if(ocl!=null){
			genericDlg.setOnCancelListener(ocl);
		}
		genericDlg.show();
	}
	public class FetchGamesTask extends AsyncTask<Void, Void, ArrayList<GameInfo>>{

		@Override
		protected ArrayList<GameInfo> doInBackground(final Void... params) {
			ArrayList<GameInfo> cgi = null;
			GoogleAccountCredential cred=MyApplication.getInstance().getCredential();
			Gameserver service=ServiceFactory.getService(cred);
			List<GameInfo> list = null;
			boolean tryAgain=true;
			int backOffTime=100;
			int maxBackOffTime=2000;
			while(tryAgain)
				try {
					list=service.gameServerEndpoint().getRunningGamesList().execute().getItems();
					tryAgain=false;
					Blog.i("FetchGamesTask.doInBackground", cgi);
				} catch (IOException e1) {
					if(backOffTime*2<maxBackOffTime){
						tryAgain=true;
						backOffTime=2*backOffTime;
						try {
							Thread.sleep(backOffTime);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else{
						notifyErrorConnecting();
						break;
					}
					e1.printStackTrace();
				}
			if(list!=null){
				return new ArrayList<GameInfo>(list);
			}
			else{
				return null;
			}
		}
		private void notifyErrorConnecting() {
			ActiveGamesAct.this.runOnUiThread(new Runnable(){

				@Override
				public void run() {
					ActiveGamesAct.this.genericDlg.setMessage("Problem connecting to server, leaving activity");
				}

			});
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ActiveGamesAct.this.runOnUiThread(new Runnable(){

				@Override
				public void run() {
					ActiveGamesAct.this.dismissGenericDialog();
				}
			});
		}
		@Override
		protected void onPostExecute(final ArrayList<GameInfo> listMap){
			if(listMap!=null)
				ActiveGamesAct.this.onFetchedGames(listMap);
			else{
				ActiveGamesAct.this.genericDlg.setMessage("No active games at the moment, challenge a friend or register for a random game if you want to play");
				new Thread(new Runnable(){

					@Override
					public void run() {
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						ActiveGamesAct.this.runOnUiThread(new Runnable(){

							@Override
							public void run() {
								ActiveGamesAct.this.dismissGenericDialog();
								ActiveGamesAct.this.finish();
							}
						});
					}
				}).start();
				
			}
		}


	}
	public static class GamesAdapter extends ArrayAdapter<GameInfo>{

		private Context context;
		private int listItemId;

		public GamesAdapter(Context context, ArrayList<GameInfo> objects, int listId,int listItemId ) {
			super(context, listId, objects);
			this.context=context;
			this.listItemId=listItemId;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(listItemId, null);
			}
			GameInfo info = getItem(position);

			String gameName=info.getGameName();
			String playerName=info.getOpNames();
			int gameType=info.getGameType();
			TextView opNameView = (TextView) view.findViewById(R.id.opponent_nick);
			TextView gameNameView = (TextView) view.findViewById(R.id.game_name);
			View iconView=view.findViewById(R.id.icon);
			if(listItemId==R.layout.list_item_finished_games){
				int resultIndex=info.getResult();
				String result=getFromIndex(resultIndex);
				TextView resultView=(TextView) view.findViewById(R.id.result);
				resultView.setText(result);
			}
			opNameView.setText(playerName);
			gameNameView.setText(gameName);
			if(gameType==1){
				iconView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.random));
			}
			else{
				iconView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.friendly));
			}

			return view;
		}

		private String getFromIndex(int resultIndex) {
			switch(resultIndex){
			case ParentGameOverMessage.WinnerValues.NOT_FINISHED:
				return "not finished";
			case ParentGameOverMessage.WinnerValues.TIE:
				return "tie";
			case ParentGameOverMessage.WinnerValues.THIS_PLAYER_WON:
				return "won";
			case ParentGameOverMessage.WinnerValues.OPPONENT_WON:
				return "lost";
			}
			throw new IllegalArgumentException("resultIndex is wrong, this is an impossible result. Index is: "+Integer.toString(resultIndex));
		}

	}
}
