package com.games.numeral.pursuit.adfree;


import static com.games.common.Constants.SuccessCodes.SUCCESSCODE_SUCCESS;

import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.numeralpursuit.gameserver.Gameserver;
import com.appspot.numeralpursuit.gameserver.model.FriendlyGameData;
import com.appspot.numeralpursuit.gameserver.model.SuccessCode;
import com.games.numeral.pursuit.adfree.R;

public class InvitedToGameActivity extends Activity {

	public class GameCreatorTask extends AsyncTask<FriendlyGameData,Void,SuccessCode> {

		@Override
		protected SuccessCode doInBackground(FriendlyGameData... params) {
			Gameserver service=ServiceFactory.getService(MyApplication.getInstance().getCredential());
			SuccessCode code=null;
			int tryTimes=5, tryNr=0;
			while(code==null && tryNr<tryTimes){
				try {
					code = service.gameServerEndpoint().createFriendlyGame(params[0]).execute();
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
			dismissGenericDialog();
			if(code.getResult().equals(SUCCESSCODE_SUCCESS)){
				Toast.makeText(MyApplication.getInstance(), "Game created", Toast.LENGTH_LONG).show();
			}

		}

	}

	public static final String INTENT_PARAM_SENDER_NAME="INTENT_PARAM_SENDER_NAME";
	public static final String INTENT_PARAM_SENDER_ID = "INTENT_PARAM_SENDER_ID";
	public static final String INTENT_PARAM_MAX_RESPONSE_TIME = "INTENT_PARAM_MAX_RESPONSE_TIME";
	public static final String INTENT_PARAM_RECEIVER_ID="INTENT_PARAM_RECEIVER_ID";
	public static final String INTENT_PARAM_RECEIVER_NAME="INTENT_PARAM_RECEIVER_NAME";
	private ProgressDialog genericDlg;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_invited_to_game);
		setQuestionText();
		setButtonClickListeners();
	}
	@Override
	protected void onPause(){
		dismissGenericDialog();
		super.onPause();
	}

	
	
	private void setQuestionText() {
		TextView requestView=(TextView)findViewById(R.id.textview_who_invited);
		String requestString =requestView.getText().toString();
		String inviter=getUserNameFromIntent();
		String inviterString=requestString.replace("<UserName>", inviter);
		requestView.setText(inviterString);		
	}

	private void setButtonClickListeners() {
		setAcceptButtonListener();
		setRejectButtonListener();
	}
	private void setAcceptButtonListener() {
		Button accept=(Button)findViewById(R.id.button_accept);
		accept.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {
				acceptGameInvite();
			}
		});
	}
	protected void acceptGameInvite() {
		Long opponentId=this.getIntent().getLongExtra(INTENT_PARAM_SENDER_ID,0);
		if(opponentId.equals(0)){
			throw new IllegalArgumentException("parameter INTENT_PARAM_SENDER_ID in intent to this activity should never be 0 or blank");
		}
		Long clientId=this.getIntent().getLongExtra(INTENT_PARAM_RECEIVER_ID,0);
		if(clientId.equals(0)){
			throw new IllegalArgumentException("parameter INTENT_PARAM_RECEIVER_ID in intent to this activity should never be 0 or blank");
		}
		createInvitedGame(clientId,opponentId);
	}



	private void createInvitedGame(Long clientId, Long opponentId) {
		FriendlyGameData data=new FriendlyGameData().setId1(clientId).setId2(opponentId);
		showGenericWaitDlg("Creating game", "Creating game, please wait", false, null);
		new GameCreatorTask().execute(data);
		finish();
	}



	private void setRejectButtonListener() {
		Button reject=(Button)findViewById(R.id.button_reject);
		reject.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {
				finish();
			}
		});		
	}

	private String getUserNameFromIntent() {
		String name=this.getIntent().getStringExtra(INTENT_PARAM_SENDER_NAME);
		return name;
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
}
