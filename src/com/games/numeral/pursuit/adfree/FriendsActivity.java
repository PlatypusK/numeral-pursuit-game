package com.games.numeral.pursuit.adfree;

import static com.games.common.Constants.SuccessCodes.SUCCESSCODE_SUCCESS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.appspot.numeralpursuit.gameserver.Gameserver;
import com.appspot.numeralpursuit.gameserver.model.FriendData;
import com.appspot.numeralpursuit.gameserver.model.FriendDataCollection;
import com.appspot.numeralpursuit.gameserver.model.GameInviteData;
import com.appspot.numeralpursuit.gameserver.model.SuccessCode;
import com.games.numeral.pursuit.adfree.R;

public class FriendsActivity extends Activity {


	private static final String PREFS_FRIENDS = "PREFS_FRIENDS";
	ListView listView;
	FriendsAdapter adapter;
	List<FriendData> friends;
	boolean isFlipped=false;
	private Long currentFriend;
	private String currentFriendEmail;
	private ProgressDialog genericDlg=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friends);
		createListView();
		fetchFriends();
		setClickListener();
	}
	private void setClickListener() {
		ImageButton addFriend=(ImageButton)findViewById(R.id.button_friend_request);
		ImageButton refresh=(ImageButton) findViewById(R.id.button_refresh);
		addFriend.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				FriendsActivity.this.onAddFriendClicked();
			}

		});
		refresh.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				onRefresh();
			}

		});
		Button inviteGameButton=(Button)findViewById(R.id.button_flipped_friend_invite_game);
		Button removeFriendButton=(Button)findViewById(R.id.button_flipped_remove_friend);
		inviteGameButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FriendsActivity.this.onInviteToGame();
			}
		});
		removeFriendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FriendsActivity.this.onRemoveFriend();
			}
		});
		
	}
	protected void onRefresh() {
		new FriendFetcherTask().execute();
		showGenericWaitDlg("Updating", "Updating list,please wait", true, null);
	}
	protected void onRemoveFriend() {
		FriendData badFriend=new FriendData();
		Long badFriendId=this.currentFriend;
		badFriend.setFriendId(badFriendId);
		showGenericWaitDlg("Remove friend", "Removing friend from list, please wait",true, null);
		new FriendRemoverTask().execute(badFriend);
	}
	protected void onInviteToGame() {
		GameInviteData data=new GameInviteData();
		data.setMaxTimeToAnswer(1000l*60*30).setReceiverId(this.currentFriend);
		new GameInviterTask().execute(data);
	}
	protected void onAddFriendClicked() {
		EditText emailField=(EditText)findViewById(R.id.edittext_new_friend_email);
		String emailString=emailField.getText().toString();
		FriendData friend=new FriendData().setFriendEmail(emailString);
		emailField.setText("");
		showGenericWaitDlg("Adding friend", "Adding friend on server, please wait",true, null);
		new FriendAdderTask().execute(friend);
	}
	public void updateList(List<FriendData> friends) {
		this.adapter.clear();
		this.adapter.addAll(friends);
		this.adapter.notifyDataSetChanged();
		Blog.i(friends);
	}
	private void fetchFriends() {
		ArrayList<FriendData> friendsL=this.getFriendsFromLocalStorage();
		if(friendsL!=null){
			this.onFriendsFetched(friendsL);
		}
		else{
			onRefresh();
		}

	}
	private void createListView() {
		friends=new ArrayList<FriendData>();
		//		friends.add(new Friend(23452345l,"llama","that guy"));
		//		friends.add(new Friend(34563456l,"alpacca","that other guy"));
		adapter=new FriendsAdapter(this,R.id.friend_name,friends);
		listView=(ListView) this.findViewById(R.id.list_view_friends);
		listView.setAdapter(adapter);
		setOnItemClickListener();
	}
	private void setOnItemClickListener() {
		listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,long id) {
				FriendsActivity.this.onFriendClicked(position);
			}

		});
	}
	protected void onFriendClicked(int position) {
		ViewFlipper vf=(ViewFlipper)this.findViewById(R.id.viewflipper_friends_activity);
		Long friendId=friends.get(position).getFriendId();
		this.currentFriend=friendId;
		this.currentFriendEmail=friends.get(position).getFriendEmail();
		String playerName=friends.get(position).getFriendNick();
		TextView idView=(TextView)findViewById(R.id.textview_flipped_friend_id);
		TextView nameView=(TextView)findViewById(R.id.textview_flipped_friend_name);
		nameView.setText(playerName);
		idView.setText(currentFriendEmail);
		vf.showNext();
		this.isFlipped=true;
	}
	@Override
	public void onBackPressed(){
		if(this.isFlipped){
			ViewFlipper vf=(ViewFlipper)this.findViewById(R.id.viewflipper_friends_activity);
			vf.showPrevious();
			this.isFlipped=false;
		}
		else{
			super.onBackPressed();
			finish();
		}
	}
	public void onFriendsFetched(List<FriendData> friends2) {
		dismissGenericDialog();
		if(friends2==null){
			friends2=new ArrayList<FriendData>();
		}
		FriendsActivity.this.updateList(friends2);
	}
	public static class FriendsAdapter extends ArrayAdapter<FriendData>{

		private Context context;
		public FriendsAdapter(Context context, int textViewResourceId, List<FriendData> friends) {
			super(context, textViewResourceId, friends);
			this.context=context;
			// TODO Auto-generated constructor stub
		}
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.friends_list_item, null);
			}
			FriendData friend = getItem(position);
			TextView itemView = (TextView) view.findViewById(R.id.friend_name);
			itemView.setText(friend.getFriendEmail());
			TextView noteView=(TextView) view.findViewById(R.id.friend_note);
			noteView.setText(friend.getFriendNick());
			return view;
		}

	}
	private ArrayList<FriendData> getFriendsFromLocalStorage() {
		final SharedPreferences prefs = getSharedPreferences(PREFS_FRIENDS, Context.MODE_PRIVATE);
		String gameString =prefs.getString(MyApplication.getInstance().getCurrentUserId().toString(), null);
		FriendDataCollection col=GsonConverter.jsonStringToGenericJson(gameString,FriendDataCollection.class);
		try{
			return new ArrayList<FriendData>(col.getItems());
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
		
	}
	private void saveFriendsToLocalStorage(List<FriendData> friends2) {
		SharedPreferences prefs=getSharedPreferences(PREFS_FRIENDS, 0);
		SharedPreferences.Editor editor = prefs.edit();
		FriendDataCollection col=new FriendDataCollection().setItems(friends2);
		editor.putString(MyApplication.getInstance().getCurrentUserId().toString(), GsonConverter.genericJsonToString(col));
		editor.commit();		
	}
	public void removeLocalFriends() {
		SharedPreferences prefs=getSharedPreferences(PREFS_FRIENDS, 0);
		SharedPreferences.Editor editor = prefs.edit();	
		editor.putString(MyApplication.getInstance().getCurrentUserId().toString(), null);
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
	public class GameInviterTask extends AsyncTask<GameInviteData,Void,SuccessCode>{

		@Override
		protected SuccessCode doInBackground(GameInviteData... params) {
			Gameserver service=ServiceFactory.getService(MyApplication.getInstance().getCredential());
			SuccessCode code=null;
			int tryTimes=5, tryNr=0;
			while(code==null && tryNr<tryTimes){
				try {
					code = service.gameServerEndpoint().inviteFriendlyGame(params[0]).execute();
				} catch (IOException e) {
					e.printStackTrace();
					tryNr++;
				}
				Blog.i("GameInviterTask.doInBackground", code);
			}
			return code;
		}
		@Override
		protected void onPostExecute(SuccessCode code){
			if(code==null){
				onFailedToInviteFriend();
			}
			dismissGenericDialog();
			if(code.getResult().equals(SUCCESSCODE_SUCCESS)){
				Toast.makeText(FriendsActivity.this, "Invitation sent", Toast.LENGTH_LONG).show();
			}

		}

	}
	public class FriendAdderTask extends AsyncTask<FriendData,Void, SuccessCode> {

		@Override
		protected SuccessCode doInBackground(FriendData... params) {
			Gameserver service=ServiceFactory.getService(MyApplication.getInstance().getCredential());
			SuccessCode code=null;
			int tryTimes=5, tryNr=0;
			while(code==null && tryNr<tryTimes){
				try {
					code = service.gameServerEndpoint().addFriend(params[0]).execute();
				} catch (IOException e) {
					e.printStackTrace();
					tryNr++;
				}
				Blog.i("FriendAdderTask.doInBackground", code);
			}
			return code;
		}
		@Override
		protected void onPostExecute(SuccessCode code){
			dismissGenericDialog();
			if(code==null){
				onCouldNotAddFriend();
				return;
			}
			if(code.getResult().equals(SUCCESSCODE_SUCCESS)){
				removeLocalFriends();
				FriendsActivity.this.fetchFriends();
			}

		}

	}
	public class FriendRemoverTask extends AsyncTask<FriendData, Void, SuccessCode>{

		@Override
		protected SuccessCode doInBackground(FriendData... params) {
			Gameserver service=ServiceFactory.getService(MyApplication.getInstance().getCredential());
			SuccessCode code=null;
			int tryTimes=5, tryNr=0;
			while(code==null && tryNr<tryTimes){
				try {
					code = service.gameServerEndpoint().removeFriend(params[0]).execute();
				} catch (IOException e) {
					e.printStackTrace();
					tryNr++;
				}
				Blog.i("FriendRemoverTask.doInBackground", code);
			}
			return code;
		}
		@Override
		protected void onPostExecute(SuccessCode code){
			dismissGenericDialog();
			if(code==null){
				onCouldNotRemoveFriend();
				return;
			}
			if(code.getResult().equals(SUCCESSCODE_SUCCESS)){
				removeLocalFriends();
				FriendsActivity.this.fetchFriends();
			}

		}

	}
	
	public class FriendFetcherTask extends AsyncTask<Void,Void,List<FriendData>>{

		@Override
		protected List<FriendData> doInBackground(Void... params) {
			Gameserver service=ServiceFactory.getService(MyApplication.getInstance().getCredential());
			FriendDataCollection friends=null;
			int tryTimes=5, tryNr=0;
			while(friends==null && tryNr<tryTimes){
				try {
					friends = service.gameServerEndpoint().getFriends().execute();
					Blog.i(friends);
				} catch (IOException e) {
					e.printStackTrace();
					tryNr++;
				}
			}
			Blog.i(friends);
			if(friends!=null){
				return friends.getItems()==null?new ArrayList<FriendData>():friends.getItems();
			}
			else{
				return null;
			}
		}
		@Override
		protected void onPostExecute(List<FriendData> friends){
			if(friends==null){
				onFailedToUpdateFriends();
				return;
			}
			saveFriendsToLocalStorage(friends);
			Blog.i(friends);
			onFriendsFetched(friends);
		}

	}
	public void onFailedToUpdateFriends() {
		Toast.makeText(this, "Could not update friends list, there was a network error.", Toast.LENGTH_LONG).show();
	}
	public void onFailedToInviteFriend() {
		Toast.makeText(this, "Could not invite to a game, most likely due to a network error.", Toast.LENGTH_LONG).show();
	}
	public void onCouldNotAddFriend() {
		Toast.makeText(this, "Could not add friend. Either your friend does not have an account or there was a network error.", Toast.LENGTH_LONG).show();
		
	}
	public void onCouldNotRemoveFriend() {
		Toast.makeText(this, "Could not remove friend, there was a network error.", Toast.LENGTH_LONG).show();
		
	}



}
