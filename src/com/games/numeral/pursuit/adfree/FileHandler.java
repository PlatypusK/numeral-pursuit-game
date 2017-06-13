package com.games.numeral.pursuit.adfree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Environment;

public class FileHandler {
	public static final String BACKGROUNDS_DIRECTORY=new File(Environment.getExternalStorageDirectory().getAbsolutePath(),"com.games.numeral.pursuit/backgrounds").getAbsolutePath();
	public static final String SCREENSHOTS_DIRECTORY=new File(Environment.getExternalStorageDirectory().getAbsolutePath(),"com.games.numeral.pursuit/screenshots").getAbsolutePath();
	public static final String DEFAULT_FILE="game_background.png";
	
	
	/**
	 * Copies certain files from assets directory to external storage. Path of files will be: Environment.getExternalStorageDirectory().getAbsolutePath()+"com.games.numeral.pursuit/backgrounds"
	 * @param context
	 * @return false if external storage is not writeable, true otherwise.
	 */
	public static boolean copyAssets(Context context) {
		if(!isExternalStorageWritable()){
			return false;
		}
		AssetManager am = context.getAssets();
		try {
			// Create new file to copy into.
			Blog.i("FileHandler.copyAssets", isExternalStorageWritable());

			File dir=new File(BACKGROUNDS_DIRECTORY);
			dir.mkdirs();
			File file = new File(dir,DEFAULT_FILE);
			Blog.i("FileHandler.copyAssets",file.getAbsolutePath());
			if(!file.exists()){
				file.createNewFile();
				copyFile(am.open("backgrounds/game_background.png"), new FileOutputStream(file));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	public static String saveBitmapToApplicationDirectory(Bitmap bit){
		String absPath=null;
		OutputStream fout = null;
		File dir=new File(SCREENSHOTS_DIRECTORY);
		dir.mkdirs();
		File imageFile = new File(dir,"screenshot"+Integer.toString(MyApplication.getUniqueId())+".png");
		Blog.i(imageFile.getAbsolutePath());
		try {
			if(!imageFile.exists()){
				imageFile.createNewFile();
			}
		    fout = new FileOutputStream(imageFile);
		    bit.compress(Bitmap.CompressFormat.JPEG, 90, fout);
		    fout.flush();
		    fout.close();
		    absPath=imageFile.getAbsolutePath();

		} catch (FileNotFoundException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		} catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		return absPath;
	}
	/** Checks if external storage is available for read and write */
	public static boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}
	/** Checks if external storage is available to at least read */
	public static boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state) ||
				Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}
	private static void copyFile(InputStream in, OutputStream out) throws IOException {
	    byte[] buffer = new byte[1024];
	    int read;
	    while((read = in.read(buffer)) != -1){
	      out.write(buffer, 0, read);
	    }
	}
}
