package com.games.numeral.pursuit.adfree;

import android.app.Activity;
import android.os.Bundle;

import com.games.numeral.pursuit.adfree.R;

public class ChatActivity extends Activity {

	public static final String CHAT_LOG_PARAM = "CHAT_LOG_PARAM";
	public static final int CHAT_ACTIVITY_RETURN_CODE=1;


	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
	}


}
