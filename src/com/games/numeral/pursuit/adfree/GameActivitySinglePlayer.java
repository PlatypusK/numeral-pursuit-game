package com.games.numeral.pursuit.adfree;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.appspot.numeralpursuit.gameserver.Gameserver;
import com.appspot.numeralpursuit.gameserver.model.GameUpdate;
import com.appspot.numeralpursuit.gameserver.model.GameUpdateRequest;
import com.games.common.CommonFunctions;
import com.games.numeral.pursuit.adfree.R;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;


public class GameActivitySinglePlayer extends Activity implements SolverHandler.CallerHandle{





	public static final String GAME_ID_PARAM = "GAME_ID_PARAM";
	public static final String SINGLE_PLAYER_PARAM = "SINGLE_PLAYER_PARAM";

	private static final int NOT_STARTED = -1;
	private static final int WAITING_FOR_UPDATE=2;
	private static final int WAITING_FOR_PLAYER_MOVE=1;
	private static final int ANIMATING=3;
	public static final String USER_ID_PARAM = "USER_ID_PARAM";
	public static final String CHAT_MESSAGE_PARAM = "CHAT_MESSAGE_PARAM";
	private static final String SAVED_CHATLOG="SAVED_CHATLOG";
	/** The OpenGL view */
	private MySurfaceView glSurfaceView;
	private final ItemSelector tileSelector=new ItemSelector();
	private boolean isPaused=true;
	private Long gameId;
	protected GameUpdate lastUpdate;
	private int state=NOT_STARTED;
	private ProgressDialog genericDlg;
	private final ArrayList<String> chatLog=new ArrayList<String>();
	private boolean flipped=false;
	private Long userId;
	private String gameFinishedMessage=null;
	public static final Map<String,ArrayList<String>> chatLogs=new HashMap<String,ArrayList<String>>();
	private static final String SINGLE_PLAYER_GAMES = "SINGLE_PLAYER_GAMES";
	CountDownLatch latch;
	protected CountDownLatch renderLatch;
	static String testString="before test";


	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Blog.i("onCreate", MySurfaceView.class.getName());
		this.isPaused=false;
		this.state=WAITING_FOR_UPDATE;
		getStuffFromBundle();
		GameSquare.setAlphaLevel(MyApplication.getInstance().getSettings().getOpacity());
		setContentView(R.layout.activity_game_activity_single_player);
		setTrashClickListener();
		initSurfaceView();
	}






	private void setTrashClickListener() {
		ImageButton button=(ImageButton)findViewById(R.id.button_trash);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				GameActivitySinglePlayer.this.onTrashClicked();
			}
		});
	}

	protected void onTrashClicked() {
		AlertDialog.Builder builder=new AlertDialog.Builder(this).setTitle("Delete local game").setMessage("This will delete the changes you have made to this game after it finished. Continue?");
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				GameActivitySinglePlayer.this.deleteLocalGame();
				glSurfaceView.queueEvent(new Runnable(){

					@Override
					public void run() {
						glSurfaceView.renderer.returnAllToInit();
					}
					
				});
				GameActivitySinglePlayer.this.getUpdateFromServer();
			}
		});
		builder.create().show();
	}



	protected void deleteLocalGame() {
		final SharedPreferences prefs = this.getSharedPreferences(SINGLE_PLAYER_GAMES, Context.MODE_PRIVATE);
		prefs.edit().remove(this.gameId.toString()).commit();
	}






	/**
	 * Remember to resume the glSurface
	 */
	@Override
	protected void onResume() {
		super.onResume();
		renderLatch=new CountDownLatch(1);
		Blog.i("GameActivitySinglePlayer.onResume testString", testString);
		this.isPaused=false;
		glSurfaceView.onResume();
		
		this.getUpdateFromServer();
	}

	/**
	 * Also pause the glSurface
	 */
	@Override
	protected void onPause() {
		this.isPaused=true;
		glSurfaceView.onPause();
		super.onPause();
		finish();
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	@Override
	protected void onSaveInstanceState(final Bundle icicle) {
		super.onSaveInstanceState(icicle);
		icicle.putStringArrayList(SAVED_CHATLOG, chatLog);
	}

	@Override
	public void onBackPressed() {
		if(flipped){
			ViewFlipper vf = (ViewFlipper) findViewById( R.id.viewFlipper );
			vf.showPrevious();
			showLastMoveAnimation(lastUpdate);
			this.flipped=false;
		}
		else{
			GameActivitySinglePlayer.super.onBackPressed();
			GameActivitySinglePlayer.this.finish();
		}
	}











	private void initSurfaceView() {
		glSurfaceView=(MySurfaceView)findViewById(R.id.game_surface_view);
		glSurfaceView.setParameters(this, this.tileSelector);
		glSurfaceView.setRenderer(new GlRenderer(this,tileSelector,glSurfaceView));
		glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}






	private void getStuffFromBundle() {
		final Bundle b=this.getIntent().getExtras();
		gameId=b.getLong(GAME_ID_PARAM,0);
		userId=b.getLong(USER_ID_PARAM, 0);
		if(gameId==0){
			throw new IllegalArgumentException("parameter GAME_ID_PARAM can not be 0 for this activity intent");
		}
		if(userId==0){
			throw new IllegalArgumentException("parameter USER_ID_PARAM can not be 0 for this activity intent");
		}
	}
	private void getUpdateFromServer() {
		this.showGenericWaitDlg("Checking in with server", "Fetching gamedata from server, please wait",true,null);
		GameUpdateRequest req=new GameUpdateRequest().setGameId(gameId).setUserId(this.userId);
		new RequestUpdateTask().execute(req);
	}

	public void updateActivity(final GameUpdate update) {
		saveGameLocal(update);
		this.lastUpdate=update;
		this.glSurfaceView.queueEvent(new Runnable(){
			@Override
			public void run() {
				GameActivitySinglePlayer.this.glSurfaceView.renderer.setTilesOnBoard(CommonFunctions.integerListToAr(update.getNewGrid()));
				GameActivitySinglePlayer.this.glSurfaceView.requestRender();
				if(update.getMoveNr()>0){
					showLastMoveAnimation(update);
				}
			}
			
		});
		this.state=WAITING_FOR_PLAYER_MOVE;
		this.dismissGenericDialog();
		Blog.v(this.getClass().getName()+".updateActivity", "update");
	}

	public void onRendererReady() {
		this.runOnUiThread(new Runnable(){

			@Override
			public void run() {
				if(GameActivitySinglePlayer.this.lastUpdate!=null){
					GameActivitySinglePlayer.this.updateActivity(lastUpdate);
				}
				renderLatch.countDown();
			}
		});
	}

	private void saveGameLocal(GameUpdate update) {
		this.lastUpdate=update;
		final SharedPreferences prefs = this.getSharedPreferences(SINGLE_PLAYER_GAMES, Context.MODE_PRIVATE);
		prefs.edit().putString(this.gameId.toString(), GsonConverter.genericJsonToString(update)).commit();
	}
	private GameUpdate getStoredLocalUpdate(){
		final SharedPreferences prefs = getSharedPreferences(SINGLE_PLAYER_GAMES, Context.MODE_PRIVATE);
		String gString =prefs.getString(this.gameId.toString(), null);
		return (gString==null?null:GsonConverter.jsonStringToGenericJson(gString, GameUpdate.class));
	}

	private void showLastMoveAnimation(final GameUpdate update) {
		int oldState=this.state;
		this.state=ANIMATING;
		this.glSurfaceView.renderer.animateLastMove(update, oldState);
		this.state=oldState;
	}



	public void updateActivityWhenPaused(final GameUpdate update) {
		this.lastUpdate=update;
		Blog.v(this.getClass().getName()+".updateActivityWhenPaused", "update");
	}
	private void removeMostRecentTile() {
		// Remove most recent tile in case of network error or wrongly placed tile

	}



	private class RequestUpdateTask extends AsyncTask<GameUpdateRequest,Void,GameUpdate>{

		@Override
		protected GameUpdate doInBackground(final GameUpdateRequest... params) {
			GameUpdate saved=GameActivitySinglePlayer.this.getStoredLocalUpdate();
			if(saved!=null){
				return saved;
			}
			Blog.v("RequestUpdateTask.doInBackground playerId",params[0]);
			GameUpdate update=null;
			GoogleAccountCredential cred=MyApplication.getInstance().getCredential();
			Gameserver serv=ServiceFactory.getService(cred);
			int tryTimes=5, tryNr=0;
			while(update==null && tryNr<tryTimes){
				try {
					update=serv.gameServerEndpoint().requestGameUpdate(params[0]).execute();
					tryNr=tryTimes+1;

				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					tryNr++;
				}
			}
			try {
				GameActivitySinglePlayer.this.renderLatch.await(3,TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return update;
		}
		@Override
		protected void onPostExecute(final GameUpdate update){
			if(update!=null){
				if(!GameActivitySinglePlayer.this.isPaused){
					GameActivitySinglePlayer.this.updateActivity(update);

				}
				else{
					GameActivitySinglePlayer.this.updateActivityWhenPaused(update);
					Blog.v("RequestUpdateTask.onPostExecute", "activity paused");
				}
			}
			else{
				GameActivitySinglePlayer.this.handleSyncError("Error fetching game data from server");
			}
			
		}
	}
	public static class MySurfaceView extends GLSurfaceView{

		private GlRenderer renderer;
		private ItemSelector ts;
		private GameSquare fingeredSquare;
		private Tile fingeredTile;
		private GameActivitySinglePlayer activity;
		public MySurfaceView(final Context con) {
			super(con);
		}
		public MySurfaceView(final Context con,final AttributeSet attrs) {
			super(con, attrs);
		}
		public void setParameters(final GameActivitySinglePlayer activity, final ItemSelector ts){
			this.activity=activity;
			this.ts=ts;
		}
		@Override
		public boolean onTouchEvent(final MotionEvent e){
			if(activity.state==GameActivitySinglePlayer.WAITING_FOR_UPDATE || activity.state==GameActivitySinglePlayer.ANIMATING){
				return true;
			}
			int actionType=e.getAction();
			float xNorm=e.getAxisValue(MotionEvent.AXIS_X)/this.getWidth();
			float yNorm=e.getAxisValue(MotionEvent.AXIS_Y)*-1.0f/getWidth()+1.0f*getHeight()/getWidth();
			switch(actionType){
			case MotionEvent.ACTION_DOWN:
				onTouchDown(xNorm,yNorm);
				break;
			case MotionEvent.ACTION_UP:
				onTouchUp(xNorm,yNorm);
				break;
			case MotionEvent.ACTION_MOVE:
				onTouchMove(xNorm,yNorm);
				break;
			}

			return true;
		}
		private void onTouchMove(final float x,final float y) {
			if(fingeredTile!=null){
				GameSquare square=(GameSquare)this.ts.getFingeredGameSquare(x, y);
				tileMovement(x,y,square);

			}
		}
		private void tileMovement(final float x, final float y, final GameSquare square) {
			this.queueEvent(new Runnable(){
				@Override
				public void run() {
					fingeredTile.setPosition(x, y);
					if(square!=null && !square.equals(MySurfaceView.this.fingeredSquare)){
						square.setColor(GameSquare.TILE_OVER_SQUARE_COLOR);
						resetColorOldSquare(square);
						if(MySurfaceView.this.fingeredSquare==null) MySurfaceView.this.fingeredSquare=square;
					}
					else if(square==null && MySurfaceView.this.fingeredSquare!=null){
						resetColorOldSquare(square);
					}
					MySurfaceView.this.requestRender();
				}
				public void resetColorOldSquare(final GameSquare square){
					if(MySurfaceView.this.fingeredSquare!=null){
						MySurfaceView.this.fingeredSquare.setColor(GameSquare.INIT_COLOR);
						MySurfaceView.this.fingeredSquare=square;
					}

				}
			});
		}
		private void onTouchUp(final float x, final float y) {
			final GameSquare square=(GameSquare)this.ts.getFingeredGameSquare(x, y);
			if(square!=null && !square.hasTile()){
				if(fingeredTile!=null){
					activity.state=GameActivitySinglePlayer.WAITING_FOR_UPDATE;
					this.queueEvent(new Runnable(){
						@Override
						public void run() {
							MySurfaceView.this.activity.verifyPlacement(fingeredTile.setEqual(square).setFingerable(false).setOwnedBy(square));
							MySurfaceView.this.requestRender();
						}

					});
				}
			}
			else if(fingeredTile!=null){
				this.queueEvent(new Runnable(){
					@Override
					public void run() {
						fingeredTile.animatePathToInit(MySurfaceView.this);
						fingeredTile=null;
					}

				});

			}
		}

		private void onTouchDown(final float x, final float y) {
			this.fingeredTile=null;
			final Fingerable fing=this.ts.getFingeredTile(x, y);
			if(fing!=null){
				this.queueEvent(new Runnable(){
					@Override
					public void run() {
						fing.onFingered(MySurfaceView.this,x,y,Fingerable.TOUCH_DOWN);
						MySurfaceView.this.requestRender();
					}

				});

				Log.i("MySurfaceView","tile fingered");
			}
			Log.i("OpenGlTestActivity",Float.toString(x)+" "+Float.toString(y));
			requestRender();

		}
		public void onTileFingered(final Tile tile, final float x, final float y) {
			this.fingeredTile=tile;
			tile.setPosition(x,y);
		}
		public void onTileMoved(final Square tile, final float x, final float y){
			tile.setPosition(x,y);
		}
		public void setRenderer (final GlRenderer renderer){
			super.setRenderer(renderer);
			this.renderer=renderer;
		}
		public Tile getFingeredTile() {
			return this.fingeredTile;
		}

	}
	public static class BackgroundImage extends Square{
		protected static final float[] INIT_COLOR=new float[]{1f,1f,1f,1f,1f,1f,1f,1f,1f,1f,1f,1f,1f,1f,1f,1f};



		public BackgroundImage(float xPos, float yPos, float xSize, float ySize, ItemSelector ts) {
			super(xPos, yPos, xSize, ySize, ts);
			super.setColor(INIT_COLOR);
			this.isBlended=false;
			this.isTextured=true;
		}

		@Override
		public void onFingered(MySurfaceView v, float x, float y, int touchState) {
			// TODO Auto-generated method stub

		}
		public void loadGLTexture(final GL10 gl, final Context context) throws IOException {
			// loading texture
			this.isTextured=true;
			File imgFile = new  File(MyApplication.getInstance().getBackgroundImagePath());
		    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
		    if(bitmap==null){
		    	bitmap = BitmapFactory.decodeStream(context.getAssets().open("backgrounds/game_background.png"));
		    }
			// generate one texture pointer
			gl.glGenTextures(1, textures, 0);
			// ...and bind it to our array
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

			// create nearest filtered texture
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

			// Use Android GLUtils to specify a two-dimensional texture image from our bitmap 
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
			bitmap.recycle();
			// Clean up
		}

		@Override
		protected boolean isDirty() {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		protected void setClean() {
			// TODO Auto-generated method stub
		}


	}
	public static class GameSquare extends Square{
		private static float ALPHA_LEVEL;
		private Tile ownsTile;
		protected static float[] INIT_COLOR = new float[]{1f,1f,1f,ALPHA_LEVEL,1f,1f,1f,ALPHA_LEVEL,1f,1f,1f,ALPHA_LEVEL,1f,1f,1f,ALPHA_LEVEL};
		protected static final float[] TILE_OVER_SQUARE_COLOR=ColorConverter.rgbToFloatArray(65,105,225,255,65,105,225,255,65,105,225,255,65,105,225,255);
		private int place=-2;
		public GameSquare(final float xPos, final float yPos, final float xSize, final float ySize,
				final ItemSelector ts) {
			super(xPos, yPos, xSize, ySize, ts);
			setUseAlpha();
			setColor(INIT_COLOR);
			this.setTexture();
		}
		private void setUseAlpha() {
			if(ALPHA_LEVEL<1.0f){
				this.useAlpha=true;
				this.isBlended=true;
			}
			else{
				this.isBlended=false;
				this.useAlpha=false;
			}
		}
		public boolean hasTile() {
			return ownsTile!=null;
		}
		private void setTexture() {
			this.textureFile="data/"+Integer.toString(1)+".png";
			this.isTextured=false;
		}
		@Override
		public void onFingered(final MySurfaceView v, final float x, final float y,final int touchState){
			Log.i("GameSquare.onFingered", "fingered gameSquare");
			//			if(touchState==TOUCH_UP){
			//				v.onSquareFingered(this, x, y);
			//			}
		}
		public void setFingerable(){
			this.itemSelector.setFingerable(this);
		}
		public int getPlace() {
			return this.place;
		}
		public GameSquare setPlace(final int x) {
			this.place=x;
			return this;
		}
		public boolean doesOwnTile(){
			if(this.ownsTile!=null) return true;
			return false;
		}
		public void setOwnsTile(final Tile tile) {
			this.ownsTile=tile;
		}
		public boolean releaseTile(final Tile tile){
			if(tile==ownsTile){
				this.ownsTile=null;
				return true;
			}
			return false;
		}
		@Override
		protected boolean isDirty() {
			// TODO Auto-generated method stub
			return true;
		}
		@Override
		protected void setClean() {
			// TODO Auto-generated method stub

		}
		public static void setAlphaLevel(float level){
			ALPHA_LEVEL=level;
			INIT_COLOR = new float[]{1f,1f,1f,ALPHA_LEVEL,1f,1f,1f,ALPHA_LEVEL,1f,1f,1f,ALPHA_LEVEL,1f,1f,1f,ALPHA_LEVEL};
		}

	}
	public static class Tile extends Square{
		private static int lastValue=-1;
		protected static final float[] INIT_COLOR=ColorConverter.rgbToFloatArray(176,196,222,255,255,248,220,255f,255,248,220,255f,176,196,222,255f);
		public final int value;
		private int place=-1;
		private GameSquare ownedBySquare;
		private boolean isFingerable=true;
		public Tile(final float xPos, final float yPos, final float xSize, final float ySize,
				final int value, final ItemSelector ts) {
			super(xPos, yPos, xSize, ySize, ts);
			isBlended=true;
			this.value=value;
			this.setColor(INIT_COLOR);
			// TODO Auto-generated constructor stub
		}


		public Tile setOwnedBy(final GameSquare square) {
			this.ownedBySquare=square;
			square.setOwnsTile(this);
			return this;
		}
		public boolean releaseSelf(){
			if(ownedBySquare==null){
				return false;
			}
			return ownedBySquare.releaseTile(this);
		}


		public int getPlace() {
			return this.place;
		}

		public GameSquare getOwner(){
			return this.ownedBySquare;
		}

		public void animatePathToInit(final MySurfaceView mySurfaceView) {

			new Thread(new Runnable(){

				@Override
				public void run() {
					Tile.this.size=initSize;
					Tile.this.scale=new Vector3(1,1,1);
					final List<Vector3> path=Tile.this.getPath();
					for(int x=path.size()-1;x>-1;x--){
						final int y=x;
						mySurfaceView.queueEvent(new Runnable(){
							@Override
							public void run() {
								Tile.this.setPosition(path.get(y).a,path.get(y).b);
								mySurfaceView.requestRender();
							}		
						});
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				}

			}).start();
		}


		protected List<Vector3> getPath() {
			float xStep=(this.pos.a-initPos.a)/30;
			float yStep=(this.pos.b-initPos.b)/30;
			float total=Math.abs(30f*xStep);
			float x=initPos.a+0.5f*size.a,y=initPos.b+0.5f*size.b;
			float xInit=x;
			List<Vector3> path=new ArrayList<Vector3>();
			while(Math.abs(x-xInit)<total){
				path.add(new Vector3(x,y,0));
				x+=xStep;
				y+=yStep;
			}

			return path;

		}
		public void loadGLTexture(final GL10 gl, final Context context, final Bitmap[] bitMaps) throws IOException {
			// loading texture
			this.isTextured=true;
			Bitmap bitmap=bitMaps[value-1];
			//			bitmap=BitmapFactory.decodeResource(context.getResources(),R.drawable.tile1_small);

			// generate one texture pointer
			gl.glGenTextures(1, textures, 0);
			// ...and bind it to our array
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

			// create nearest filtered texture
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

			// Use Android GLUtils to specify a two-dimensional texture image from our bitmap 
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

			// Clean up
		}


		@Override
		public void onFingered(final MySurfaceView v,final float x, final float y, final int touchType) {
			if(touchType==TOUCH_DOWN){
				v.onTileFingered(this,x,y);

			}
			if(touchType==TOUCH_MOVED){
				v.onTileMoved(this, x, touchType);
			}
		}

		public void setFingerable(){
			this.isFingerable=true;
			this.itemSelector.setFingerable(this);
		}


		public static Bitmap[] getBitmaps(final Context context) {
			Bitmap[] maps=new Bitmap[9];
			for(int x=0;x<9;x++){
				try {
					maps[x]=BitmapFactory.decodeStream(context.getAssets().open("data/tile"+Integer.toString(x+1)+".png"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return maps;
		}
		public static void recycleBitmaps(final Bitmap[] maps){
			for(Bitmap map:maps){
				map.recycle();
			}
		}
		public int getValue(){
			return this.value;
		}


		public boolean isOnBoard() {
			if(this.ownedBySquare!=null) return true;
			return false;
		}
		/**Sets the position and dimensions of the Tile equal to the square given as an argument*/
		public Tile setEqual(final GameSquare square) {
			this.pos=square.getPos();
			this.scale=this.scaleAtoB(this, square);
			this.size=square.getSize();
			this.place=square.getPlace();
			return this;
		}


		public Tile setFingerable(final boolean b) {
			this.isFingerable=b;
			if(!b) this.itemSelector.setNotFingerable(this);
			else this.itemSelector.setFingerable(this);
			return this;
		}


		public boolean isFingerable() {
			return isFingerable;
		}


		@Override
		protected boolean isDirty() {
			if(lastValue!=this.value){
				return true;
			}
			else{
				return false;
			}
		}


		@Override
		protected void setClean() {
			lastValue=this.value;
		}
		public static void resetDirtyStatus(){
			lastValue=-1;
		}

	}


	public static abstract class Square implements Fingerable{
		public static final float[] DEFAULT_COLOR=ColorConverter.rgbToFloatArray(255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255);

		/** Contains the maximum and minimum coordinates for the vertices a=xmin, b=xmax, c=ymin, d=ymax*/
		protected Vector4 vertLims;
		protected Vector3 pos=null;
		protected Vector3 centPos=null;
		protected Vector3 initCenter=null;
		protected final Vector3 initPos;
		protected Vector3 scale=new Vector3(1,1,1);
		protected final Vector2 initSize;
		protected Vector2 size=null;
		protected FloatBuffer vertexBuffer, colorBuff;	// buffer holding the vertices
		protected String textureFile="";
		protected float vertices[] = {
				0.0f, 0.0f,  0.0f,		// V1 - bottom left
				0.0f,  1.0f,  0.0f,		// V2 - top left
				1.0f, 0.0f,  0.0f,		// V3 - bottom right
				1.0f,  1.0f,  0.0f			// V4 - top right
		};
		protected ByteBuffer byteBuffer;
		protected float[] vertColor=new float[vertices.length/3*4];
		/** The texture pointer */
		protected int[] textures = new int[1];
		protected FloatBuffer textureBuffer;	// buffer holding the texture coordinates
		protected float texture[] = {    		
				// Mapping coordinates for the vertices
				0.0f, 1.0f,		// top left		(V2)
				0.0f, 0.0f,		// bottom left	(V1)f
				1.0f, 1.0f,		// top right	(V4)
				1.0f, 0.0f		// bottom right	(V3)
		};
		protected ItemSelector itemSelector;
		protected boolean isTextured=false,isBlended=false;

		protected boolean useAlpha=false;



		public Square(final float xPos, final float yPos, final float xSize, final float ySize, final ItemSelector ts){
			initPos=new Vector3(xPos,yPos,0);
			this.pos=new Vector3(xPos,yPos,0);
			initSize=new Vector2(xSize,ySize);
			size=new Vector2(xSize,ySize);
			initCenter=this.getCentrePos();
			this.itemSelector=ts;

			setVertices(xPos,yPos,xSize,ySize);
			byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4); 
			byteBuffer.order(ByteOrder.nativeOrder());
			vertexBuffer = byteBuffer.asFloatBuffer();
			vertexBuffer.put(vertices);
			vertexBuffer.position(0);
			//			List<Vector4> list=new ArrayList<Vector4>();
			//			list.add(ColorConverter.rgbToFloatVector(240f,230f,140f,255f));
			setColor(Square.DEFAULT_COLOR);
			vertLims=new Vector4(xPos, xPos+xSize, yPos,yPos+ySize);
		}


		private void setVertices(final float xPos, final float yPos, final float xSize, final float ySize) {
			this.vertices=new float[]{
					xPos, yPos, 0,
					xPos, yPos+ySize,0,
					xPos+xSize,yPos,0,
					xPos+xSize, yPos+ySize,0
			};
			byteBuffer = ByteBuffer.allocateDirect(texture.length * 4);
			byteBuffer.order(ByteOrder.nativeOrder());
			textureBuffer = byteBuffer.asFloatBuffer();
			textureBuffer.put(texture);
			textureBuffer.position(0);
		}
		public void setColor(final float...colorValues){
			if(colorValues.length!=4 && colorValues.length!=vertColor.length){
				Log.w("Tile.setColor", "wrong number of colorvalues");
				return;
			}
			int x=0,y=0;
			while(x<vertColor.length){				
				vertColor[x]=colorValues[y];
				if(y==colorValues.length-1){
					y=0;
				}
				y++;
				x++;
			}
			byteBuffer = ByteBuffer.allocateDirect(vertColor.length * 4);
			byteBuffer.order(ByteOrder.nativeOrder());
			colorBuff = byteBuffer.asFloatBuffer();
			colorBuff.put(vertColor);
			colorBuff.position(0);
		}
		public void setColor(final int...colors){
			if(colors.length!=4 && colors.length!=vertColor.length){
				Log.w("Tile.setColor", "wrong number of colorvalues");
				return;
			}
			float[] floatColors=new float[colors.length];
			for(int i=0;i<colors.length;i++){
				floatColors[i]=colors[i]/255f;
			}
			int x=0,y=0;
			while(x<vertColor.length){				
				vertColor[x]=floatColors[y];
				if(y==floatColors.length-1){
					y=0;
				}
				y++;
				x++;
			}
			byteBuffer = ByteBuffer.allocateDirect(vertColor.length * 4);
			byteBuffer.order(ByteOrder.nativeOrder());
			colorBuff = byteBuffer.asFloatBuffer();
			colorBuff.put(vertColor);
			colorBuff.position(0);
		}
		/** The draw method for the square with the GL context */
		public void draw(final GL10 gl) {
			// bind the previously generated texture
			//			Blog.v("Square.draw | position: ", pos);
			gl.glPushMatrix();
			if(isTextured){
				gl.glEnable(GL10.GL_TEXTURE_2D);
				if(isDirty()){
					gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
					setClean();
				}
			}
			if(isBlended){
				gl.glEnable(GL10.GL_BLEND);
			}
			if(useAlpha){
				gl.glEnable(GL10.GL_BLEND);    
				gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

				gl.glEnable(GL10.GL_ALPHA_BITS);
			}

			// Point to our buffers
			gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
			if(isTextured)gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
			//			if(isBlended)gl.glEnableClientState(GL10.GL_BLEND);
			// Set the face rotation
			//			gl.glFrontFace(GL10.GL_CW);

			Vector3 translateTo=this.getCentrePos();
			gl.glTranslatef(translateTo.a,translateTo.b,translateTo.c);	
			gl.glScalef(scale.a,scale.b,scale.c);
			gl.glTranslatef(initCenter.a*-1f, initCenter.b*-1f, 0);	

			//			gl.glTranslatef(pos.a-initPos.a, pos.b-initPos.b, pos.c-initPos.c);

			// Point to our vertex buffer
			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
			if(isTextured)gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
			gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuff);

			// Draw the vertices as triangle strip
			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);

			//Disable the client state before leaving
			gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
			if(isTextured)gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
			//			if(isBlended)gl.glDisableClientState(GL10.GL_BLEND);
			if(isTextured){
				gl.glDisable(GL10.GL_TEXTURE_2D);
			}
			if(isBlended){
				gl.glDisable(GL10.GL_BLEND);
			}
			if(useAlpha){
				gl.glDisable(GL10.GL_ALPHA_BITS);
			}
			gl.glPopMatrix();

		}
		protected abstract boolean isDirty();
		protected abstract void setClean();


		public void setPosition(final float x, final float y) {
			this.pos=new Vector3(x-0.5f*size.a,y-0.5f*size.b,0.0f);
			vertLims=new Vector4(pos.a,pos.a+size.a,pos.b,pos.b+size.b);
		}
		private Vector3 getCentrePos(){
			return new Vector3(pos.a+0.5f*size.a,pos.b+0.5f*size.b,0);
		}
		protected Vector3 scaleAtoB(final Square a, final Square b){
			return new Vector3(b.getInitSize().a/a.getInitSize().a,b.getInitSize().b/a.getInitSize().b,1);
		}
		@Override
		public boolean isFingered(final float x, final float y) {
			if(vertLims.a<x && x<vertLims.b && vertLims.c<y && y<vertLims.d){
				return true;
			}
			return false;
		}

		public Vector3 getPos(){
			return this.pos;
		}
		public Vector2 getInitSize(){
			return this.initSize;
		}
		public Vector2 getSize(){
			return this.size;
		}
	}

	public static class ItemSelector{
		private final Set<Fingerable> gameSquareFingerables=new HashSet<Fingerable>();;
		private final Set<Fingerable> tileFingerables=new HashSet<Fingerable>();;
		public Fingerable getFingeredTile(final float x,final float y){
			for(Fingerable fingerable:tileFingerables){
				if(fingerable.isFingered(x,y)) return fingerable;
			}
			return null;
		}
		public Fingerable getFingeredGameSquare(final float x,final float y){
			for(Fingerable fingerable:gameSquareFingerables){
				if(fingerable.isFingered(x,y)) return fingerable;
			}
			return null;
		}
		public void setFingerable(final GameSquare fingerable){
			gameSquareFingerables.add(fingerable);
		}
		public void setFingerable(final Tile fingerable){
			tileFingerables.add(fingerable);
		}
		public void setNotFingerable(final Fingerable fingerable){
			this.gameSquareFingerables.remove(fingerable);
			this.tileFingerables.remove(fingerable);
		}

	}

	public static class GlRenderer implements Renderer {

		private List<Tile> tiles;		// the square
		private final Context context;
		private List<GameSquare> squares;
		private final MySurfaceView msv;
		private final ItemSelector is;
		private int height;
		private int width;
		private static final float TILESIZE=0.1100f;
		private int[] grid=new int[]{
				0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,0,0,
		};
		private BackgroundImage background;


		/** Constructor to set the handed over context 
		 * @param glSurfaceView 
		 * @param view */
		public GlRenderer(final Context context,final ItemSelector ts, final MySurfaceView glSurfaceView) {
			this.context=context;
			this.msv=glSurfaceView;
			this.is=ts;
			//		this.square= new Square();
		}

		public void returnAllToInit() {
			for(Tile tile:tiles){
				tile.releaseSelf();
				tile.animatePathToInit(this.msv);
			}
		}

		public void animateLastMove(final GameUpdate update, final int oldState) {
			new Thread(new Runnable(){

				@Override
				public void run() {
					if(update.getMoveNr()==0){
						return;
					}
					Tile failed=getTileValued(update.getValue(),update.getPlace());
					GameSquare failedPlace=GlRenderer.this.squares.get(update.getPlace());
					failed.setEqual(failedPlace);
					blinkSquare(failed, failedPlace, 3,300);
					if(!update.getMoveCorrect()){
						failed.animatePathToInit(msv);
						failed.setFingerable();
						failed.releaseSelf();
					}
					failedPlace.setColor(GameSquare.INIT_COLOR);
					msv.activity.onAnimationDone(oldState);

				}

			}).start();


		}

		private void blinkSquare(final Tile failed, final GameSquare place, final int times, final long interval) {
			for(int i=0;i<times;i++){
				failed.setColor(203,77,77,255);
				msv.requestRender();
				sleep(interval);
				failed.setColor(Tile.INIT_COLOR);
				msv.requestRender();
				sleep(interval);
			}
		}

		private void sleep(final long ms){
			try {
				Thread.sleep(ms);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private Tile getTileValued(final Integer value,final Integer place) {
			if(squares.get(place).doesOwnTile()){
				return squares.get(place).ownsTile;
			}
			for(Tile tile:tiles){
				if(tile.getValue()==value.intValue()&& tile.isFingerable()){
					return tile;
				}
			}
			return null;
		}

		public int[] getGrid() {
			return this.grid;
		}

		private void createGameSquares(final ItemSelector ts, final int w, final int h) {
			this.squares=new ArrayList<GameSquare>();
			for(int x=0;x<9*9;x++){
				GameSquare gs=new GameSquare(getXpos(x,w,h),getYpos(x,w,h),0.1f,0.1f,ts);
				gs.setPlace(x);
				squares.add(gs);
				gs.setFingerable();
			}
			Blog.v("GlRenderer.createGameSquares",squares.size());
		}
		private float getXpos(final int x,final int w, final int h){
			float pos=0.005f+0.109f*(x%9);
			int a=x-9*(x/9);//get only number of tiles in this row
			float add=0f,addOne=.01f,addTwo=0.02f;
			if(a>1){
				for(int b=3;b<9;b++){
					if(a%b==0 && b<7) add=addOne;
					if(a%b==0 && b>5) add=addTwo;
				}
			}
			return pos+add;
		}
		private float getYpos(final int x, final int w, final int h){
			float pos=1f*h/w-0.109f*(x/9+1);
			int a=x/9;//get only number of tiles in this column
			float sub=0f,subOne=.01f,subTwo=0.02f;
			if(a>1){
				for(int b=3;b<9;b++){
					if(a%b==0 && b<7) sub=subOne;
					if(a%b==0 && b>5) sub=subTwo;
				}
			}
			return pos-sub;
		}
		public void removeTile(final Tile tile){
			this.tiles.remove(tile);
		}
		public void addTile(final Tile tile){
			this.tiles.add(tile);
		}

		private void createTiles(final ItemSelector ts, final int width, final int height) {
			this.tiles=new ArrayList<Tile>();
			float bottomGrid=1f*height/width-1f;
			for(int value=1;value<10;value++){
				for(int y=0;y<9;y++){
					Tile t=new Tile((value-1)*(TILESIZE+TILESIZE*0.01f)+0.0025f,(bottomGrid-0.119f*1f),TILESIZE,TILESIZE,value,ts);
					tiles.add(t);
					t.setFingerable();
				}
			}
		}
		public Tile setTilesOnBoard(final int[] newGrid){
			int i=0;
			this.grid=newGrid;
			for(int value:newGrid){
				if(value!=0 && !squares.get(i).doesOwnTile()){
					Blog.v("value", value);
					try{
						removeFingerableTiles(value).setEqual(squares.get(i)).setOwnedBy(squares.get(i)).setFingerable(false);
					}
					catch(NullPointerException e){
						e.printStackTrace();
					}
				}
				i++;
			}
			return null;
		}
		public Tile removeFingerableTiles(final int value){
			for(Tile tile:tiles){
				if(tile.getValue()==value && !tile.isOnBoard()) return tile;
			}
			return null;
		}


		private void loadBackGround(GL10 gl, int width, int height) {
			background=new BackgroundImage(0f,0f,1.0f,1.0f*height/width,is);
			try {
				background.loadGLTexture(gl, context);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		private void loadTiles(final GL10 gl, final Bitmap[] textureBitmaps){
			createTiles(is,width,height);

			for(Tile tile:tiles){
				try {
					tile.loadGLTexture(gl, this.context, textureBitmaps);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		private void loadGameSquares(final GL10 gl){
			createGameSquares(is,width,height);
		}
		@Override
		public void onDrawFrame(final GL10 gl) {
			// clear Screen and Depth Buffer
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

			// Reset the Modelview Matrix
			gl.glLoadIdentity();

			// Drawing
			gl.glTranslatef(0.0f, 0.0f, 0.0f);		// move 5 units INTO the screen
			// is the same as moving the camera 5 units away
			// Draw the triangle
			this.background.draw(gl);
			for(Square square:squares){
				square.draw(gl);
			}
			for(Square tile:tiles){
				tile.draw(gl);
			}
			Tile fingered=msv.getFingeredTile();
			if(fingered!=null){
				fingered.draw(gl);
			}
			Tile.resetDirtyStatus();


		}
		@Override
		public void onSurfaceChanged(final GL10 gl, final int width, int height) {
			if(height == 0) { 						//Prevent A Divide By Zero By
				height = 1; 						//Making Height Equal One
			}
			this.width=width; this.height=height;
			loadBackGround(gl,width,height);
			loadGameSquares(gl);
			Bitmap[] tileBitmaps=Tile.getBitmaps(context);
			loadTiles(gl, tileBitmaps);
			Tile.recycleBitmaps(tileBitmaps);
			gl.glViewport(0, 0, width, height); 	//Reset The Current Viewport
			gl.glMatrixMode(GL10.GL_PROJECTION); 	//Select The Projection Matrix
			gl.glLoadIdentity(); 					//Reset The Projection Matrix
			//Calculate The Aspect Ratio Of The Window
			//			GLU.gluPerspective(gl, 45.0f, (float)width / (float)height, 0.1f, 100.0f);
			gl.glOrthof(0, 1.0f, 0, 1.0f*height/width, -1f, 100.0f);

			gl.glMatrixMode(GL10.GL_MODELVIEW); 	//Select The Modelview Matrix
			gl.glLoadIdentity();

			//Reset The Modelview Matrix
		}
		@Override
		public void onSurfaceCreated(final GL10 gl, final EGLConfig config) {
			// Load the texture for the square 


			gl.glEnable(GL10.GL_TEXTURE_2D);			//Enable Texture Mapping ( NEW )
			gl.glShadeModel(GL10.GL_SMOOTH); 			//Enable Smooth Shading
			gl.glClearColor(28/255f,16/255f,33/255f, 255/255f); 	//Black Background
			gl.glClearDepthf(1.0f); 					//Depth Buffer Setup
			//			gl.glEnable(GL10.GL_DEPTH_TEST); 			//Enables Depth Testing
			//			gl.glDepthFunc(GL10.GL_LEQUAL); 			//The Type Of Depth Testing To Do

			//Really Nice Perspective Calculations
			gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST); 
		}
	}
	public static final class Vector4{
		final public float a,b,c,d;
		public Vector4(final float a, final float b, final float c, final float d) {
			this.a = a;
			this.b = b;
			this.c = c;
			this.d = d;
		}
		@Override
		public String toString(){
			return "("+Float.toString(a)+","+Float.toString(b)+","+Float.toString(c)+","+Float.toString(d)+")";
		}
	}
	public static final class Vector3{
		final public float a,b,c;
		public Vector3(final float a, final float b, final float c){
			this.a=a;
			this.b=b;
			this.c=c;
		}
		@Override
		public String toString(){
			return "("+Float.toString(a)+","+Float.toString(b)+","+Float.toString(c)+")";
		}
		public Vector3 subtract(final Vector3 sub ){
			return new Vector3(a-sub.a,b-sub.b,c-sub.c);
		}
	}
	public static final class Vector2{
		final public float a,b;
		public Vector2(final float a, final float b){
			this.a=a;
			this.b=b;
		}
		@Override
		public String toString(){
			return "("+Float.toString(a)+","+Float.toString(b)+")";
		}
	}
	public static class ColorConverter{
		public static float[] rgbToFloatArray(final float...value){
			float [] result=new float[value.length];
			for(int a=0;a<value.length;a++){
				result[a]=value[a]/255;
			}
			return result;
		}
	}
	public static interface Fingerable {
		int TOUCH_DOWN = 0;
		int TOUCH_MOVED = 1;
		int TOUCH_UP = 2;
		public boolean isFingered(float x, float y);
		public void onFingered(MySurfaceView v, float x, float y, int touchState);
	}


	protected void verifyPlacement(final Tile placedTile) {
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				
				final SolverHandler h=new SolverHandler(CommonFunctions.integerListToAr(lastUpdate.getNewGrid()),placedTile.getPlace(),placedTile.getValue(),GameActivitySinglePlayer.this);
				h.start();
				showGenericWaitDlg("Test and Update", "Testing if placement is correct", true, new OnCancelListener(){
					@Override
					public void onCancel(DialogInterface dialog) {
						dialog.dismiss();
						h.cancelOperation();
						onCancelTest();
					}
				});			
			}
		});

	}
	protected void onAnimationDone(final int oldState) {
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				GameActivitySinglePlayer.this.state=oldState;
			}
			
		});
	}






	public void onCancelTest() {
		this.dismissGenericDialog();
		final Tile t=this.glSurfaceView.getFingeredTile();
		if(t!=null){
			new Thread(new Runnable(){

				@Override
				public void run() {
					t.animatePathToInit(glSurfaceView);
					t.setFingerable(true);
					t.animatePathToInit(GameActivitySinglePlayer.this.glSurfaceView);
					t.getOwner().setColor(GameSquare.INIT_COLOR);
					t.releaseSelf();
				}

			}).start();

		}
		this.state=WAITING_FOR_PLAYER_MOVE;
	}

	public void showGameFinishedMessage() {
		AlertDialog.Builder builder=new AlertDialog.Builder(this).setTitle("Game Finished").setMessage("Game Finished").setPositiveButton("Ok", null);
		builder.create().show();
	}

	public boolean isFinished() {
		return this.gameFinishedMessage!=null;
	}

	public void handleSyncError(final String error) {
		this.dismissGenericDialog();
		Toast.makeText(GameActivitySinglePlayer.this, error, Toast.LENGTH_LONG).show();		
	}
	public void handleUpdateError(final String error){
		removeMostRecentTile();
		Toast.makeText(GameActivitySinglePlayer.this, error, Toast.LENGTH_LONG).show();		
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






	@Override
	public void onGridSolved(final int[] newGrid, final boolean solution, final int place,final int value) {
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				if(solution){
					newGrid[place]=value;
				}
				final GameUpdate update=lastUpdate;
				update.setNewGrid(CommonFunctions.intArToList(newGrid));
				update.setMoveCorrect(solution);
				update.setPlace(place);
				update.setValue(value);
				update.setMoveNr(lastUpdate.getMoveNr()+1);
				updateActivity(update);
				
			}
			
		});

	}
}
