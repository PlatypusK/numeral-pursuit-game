package com.games.numeral.pursuit.adfree;


import static com.games.common.Constants.SuccessCodes.SUCCESSCODE_SUCCESS;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.appspot.numeralpursuit.gameserver.Gameserver;
import com.appspot.numeralpursuit.gameserver.model.Settings;
import com.appspot.numeralpursuit.gameserver.model.SuccessCode;
import com.games.numeral.pursuit.adfree.R;


public class SettingsActivity extends Activity {


	protected static final int RESULT_LOAD_IMAGE = 1;
	private ProgressDialog genericDlg;
	private Settings lastFetchedSettings;
	private String backgroundPath=MyApplication.getInstance().getBackgroundImagePath();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		restoreInstance(savedInstanceState);
		addPicToGallery();
		setContentView(R.layout.activity_settings);

		onSettingsFetched(MyApplication.getInstance().getSettings());
		setClickListeners();
	}
	private void restoreInstance(Bundle savedInstanceState) {
		if(savedInstanceState!=null){
			this.backgroundPath=savedInstanceState.getString(MyApplication.KEY_BACKGROUND_PATH);
		}
	}
	private void setClickListeners() {
		setSaveListener();
		setCancelListener();
		setPickBackgroundListener();
	}
	private void addPicToGallery() {
		Intent media = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);


		//mCurrentPhotoPath is the path to image.
		File f = new File(MyApplication.DEFAULT_BACKGROUND);
		Uri contentUri = Uri.fromFile(f);
		Blog.i("SettingsActivity.addPicToGallery", contentUri);
		media.setData(contentUri);
		this.sendBroadcast(media);
	}
	private void setPickBackgroundListener() {

		ImageButton picker=(ImageButton)findViewById(R.id.button_background_picker);
		picker.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				startActivityForResult(Intent.createChooser(intent,
						"Select Picture"), RESULT_LOAD_IMAGE);
			}
		});
	}
	private void setCancelListener() {
		Button cancel=(Button)findViewById(R.id.cancel);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	private void setSaveListener() {
		Button save=(Button)findViewById(R.id.save);
		save.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Settings settings=new Settings();
				settings.setNotifyGameUpdate((((CheckBox)findViewById(R.id.setting_notify_game_updates)).isChecked()));
				settings.setName(((EditText)findViewById(R.id.setting_name)).getText().toString());
				settings.setOpacity(SettingsActivity.this.getOpacityFromUI());
//				settings.setNotifyFriendChatUpdates((((CheckBox)findViewById(R.id.setting_notify_chat_from_friend)).isChecked()));
				settings.setNotifyGameRequest((((CheckBox)findViewById(R.id.setting_notify_game_requests)).isChecked()));
				settings.setNotifyGameChatUpdates((((CheckBox)findViewById(R.id.setting_notify_in_game_chat)).isChecked()));
				new SettingsActivity.UpdateSettingsTaks().execute(settings);
				MyApplication.getInstance().setBackGroundImagePath(SettingsActivity.this.backgroundPath);
				showGenericWaitDlg("Saving", "Saving settings to server, please wait" ,false, null);
			}
		});		
	}
	@Override
	protected void onSaveInstanceState(final Bundle icicle) {
		icicle.putString(MyApplication.KEY_BACKGROUND_PATH, this.backgroundPath);	
		super.onSaveInstanceState(icicle);

	}
	protected Float getOpacityFromUI() {
		EditText opacityView=(EditText)findViewById(R.id.setting_opacity);
		String opacityString=opacityView.getText().toString();
		Float opacityValue;
		try{
			opacityValue=Float.parseFloat(opacityString)/100;
		}catch(NumberFormatException e){
			opacityValue=this.lastFetchedSettings.getOpacity();
			opacityView.setText(Float.valueOf((lastFetchedSettings.getOpacity()*100)).toString());
			Toast.makeText(this, "Error in opacity value, needs to be a number from 0 to 100, resetting value to stored value", Toast.LENGTH_LONG).show();;
		}
		if(opacityValue>1){
			opacityValue=1f;
			opacityView.setText(Float.valueOf(100f).toString());
		}
		if(opacityValue<0){
			opacityValue=this.lastFetchedSettings.getOpacity();
			opacityView.setText(Float.valueOf(opacityValue*100).toString());
		}
		return opacityValue;
	}
	public void onSettingsFetched(Settings settings) {
		this.lastFetchedSettings=settings;
		CheckBox box=(CheckBox)findViewById(R.id.setting_notify_game_updates);
		box.setChecked(settings.getNotifyGameUpdate());
//		box=(CheckBox)findViewById(R.id.setting_notify_chat_from_friend);
//		box.setChecked(settings.getNotifyFriendChatUpdates());
		box=(CheckBox)findViewById(R.id.setting_notify_game_requests);
		box.setChecked(settings.getNotifyGameRequest());
		box=(CheckBox)findViewById(R.id.setting_notify_in_game_chat);
		box.setChecked(settings.getNotifyGameChatUpdates());
		EditText name=(EditText)findViewById(R.id.setting_name);
		name.setText(settings.getName());
		EditText opacity=(EditText)findViewById(R.id.setting_opacity);
		opacity.setText(Float.valueOf((settings.getOpacity()*100)).toString());
		//		TextView path=(TextView)findViewById(R.id.setting_backpath);
		//		path.setText(this.backgroundPath);
		ImageButton background=(ImageButton)findViewById(R.id.button_background_picker);
		File imgFile = new  File(MyApplication.getInstance().getBackgroundImagePath());
		Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());		
		if(bitmap!=null){
			background.setImageBitmap(bitmap);
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
			Uri selectedImageUri = data.getData();
			Blog.i(selectedImageUri);
            String selectedImagePath = getPath(selectedImageUri);
            Blog.i(selectedImageUri," : ",selectedImagePath);
            this.backgroundPath=selectedImagePath==null?selectedImageUri.getPath():selectedImagePath;
			ImageButton background=(ImageButton)findViewById(R.id.button_background_picker);
			try{
				File imgFile = new  File(this.backgroundPath);
				Blog.i(imgFile.getAbsolutePath());
				Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
				if(bitmap!=null){
					background.setImageBitmap(bitmap);
				}
			}catch(Exception e){
				e.printStackTrace();
				Toast.makeText(this, "Error fetching file", Toast.LENGTH_LONG).show();
			}

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
	private class UpdateSettingsTaks extends AsyncTask<Settings,Void,SuccessCode>{
		Settings newSettings;
		@Override
		protected SuccessCode doInBackground(Settings... params) {
			this.newSettings=params[0];
			Gameserver service=ServiceFactory.getService(MyApplication.getInstance().getCredential());

			SuccessCode code=null;
			int tryTimes=5, tryNr=0;
			while(code==null && tryNr<tryTimes){
				try {
					code = service.gameServerEndpoint().updateSettings(params[0]).execute();
					tryNr=tryTimes+1;
				} catch (IOException e) {
					e.printStackTrace();
					tryNr++;
				}
				Blog.i("UpdateSettingsTask.doInBackground", code);
			}
			return code;
		}
		@Override
		protected void onPostExecute(SuccessCode code){
			dismissGenericDialog();
			if(code!=null){
				if(code.getResult().equals(SUCCESSCODE_SUCCESS)){
					Toast.makeText(SettingsActivity.this, "Updated settings", Toast.LENGTH_LONG).show();
					MyApplication.getInstance().setSettings(newSettings);
					SettingsActivity.this.finish();
				}
			}
		}
	}

		public String getPath(Uri uri) {
			String[] projection = { MediaStore.Images.Media.DATA };

			Cursor cursor=getContentResolver().query(uri,projection, null, null, null);
			if(cursor!=null)
			{
				//HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
				//THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
				int column_index = cursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				return cursor.getString(column_index);
			}
			else return null;
		}
}
