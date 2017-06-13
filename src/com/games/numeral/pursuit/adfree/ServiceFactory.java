package com.games.numeral.pursuit.adfree;

import java.io.IOException;

import com.appspot.numeralpursuit.gameserver.Gameserver;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ExponentialBackOff;

public class ServiceFactory {
	public static final Gameserver getService(final GoogleAccountCredential credentials){
		if(credentials==null) throw new IllegalArgumentException("Credentials can not be null");
		getFreshTokenOnCredentials(credentials);
		final Gameserver.Builder builder = new Gameserver.Builder(
				AndroidHttp.newCompatibleTransport(), new GsonFactory(), credentials);
		builder.setApplicationName( MyApplication.APPLICATION_NAME);
		return builder.build();
	}

	private static void getFreshTokenOnCredentials(GoogleAccountCredential credentials) {
		boolean credentialsUpdatedWithToken=false;
		credentials.setBackOff(getExponentialBackoff());
		try {
			credentials.getBackOff().reset();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			String token=credentials.getToken();
			GoogleAuthUtil.invalidateToken(MyApplication.getInstance(), token);
			credentialsUpdatedWithToken=true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GoogleAuthException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(!credentialsUpdatedWithToken){
			Blog.e("Could not get a valid token with credentials");
		}
	}
	public static ExponentialBackOff getExponentialBackoff(){
		return new ExponentialBackOff.Builder().setInitialIntervalMillis(100).setMaxIntervalMillis(1600).setMultiplier(2).setMaxElapsedTimeMillis(5000).build();
	}
}
