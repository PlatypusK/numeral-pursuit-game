package com.games.numeral.pursuit.adfree;

import java.util.Arrays;
import java.util.List;

import com.appspot.numeralpursuit.gameserver.model.GameUpdate;
import com.games.common.gsonmessages.ParentGameOverMessage;



public class GameUpdateHelper {
	public static final List<Integer> EMPTY_GRID=
			Arrays.asList(new Integer[]{
					0,0,0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,0,0,});
	public static final Long MAX_UPDATE_TIME_DEFAULT=1000l*60l*30l;
	public static final Long MAX_GAME_TIME_DEFAULT=1000l*60l*60l*24l*7l;
	private GameUpdate update;
	private static final Integer MAX_TILES_ON_BOARD = 81;


	GameUpdateHelper(final GameUpdate update){
		this.update=update;
	}
	public void setUpdate(final GameUpdate update){
		this.update=update;
	}
	public GameUpdate getUpdate(){
		return this.update;
	}
	public boolean isMostRecentUpdate(final GameUpdate otherUpdate){
		return otherUpdate.getMoveNr()<update.getMoveNr();
	}
	public boolean isFinished(){
		return update.getWinner()!=null;
	}
	public boolean isForfeited(){
		return update.getForfeitedBy()!=null;
	}
	public boolean playerTimedOut(final GameUpdate oldUpdate){
		Long time=System.currentTimeMillis();
		if(update.getGameTimedOut()){
			return true;
		}
		if(time-oldUpdate.getUpdateTime()>update.getMaxUpdateTime()){
			update.setGameTimedOut(true);
			return true;
		}
		return false;
	}
	public boolean gameTimedOut(){
		Long time=System.currentTimeMillis();
		return time-update.getFirstUpdateTime()>update.getMaxGameTime();
	}
	public Long getOtherPlayer(final Long player){
		return update.getPlayers().get(0).equals(player) ? update.getPlayers().get(1):update.getPlayers().get(0);
	}
	public int getPlayerNumber(final Long player){
		return update.getPlayers().get(0).equals(player) ? 0:1;
	}
	public void updatePlayerScore(){
		final int playerNr=getPlayerNumber(update.getWhoMoved());
		List<Integer> scores=update.getScore();
		Integer score=scores.get(playerNr);
		Integer change=(update.getMoveCorrect()?1:(-1));
		score=score+change;
		scores.set(playerNr, score);
		update.setScore(scores);
	}

	public void addToPlayerActiveGames(){

	}
	public Integer getPlayerScore(final Long player){
		return (player.equals(update.getPlayers().get(0)))?update.getScore().get(0):update.getScore().get(1);
	}
	public int getGameOverReason() {
		if(!isFinished()) return ParentGameOverMessage.GameOverReason.NOT_FINISHED;
		if(update.getGameTimedOut()) return ParentGameOverMessage.GameOverReason.GAME_TIMED_OUT;
		if(this.isForfeited()) return ParentGameOverMessage.GameOverReason.GAME_FORFEITED;
		if(update.getPlayerTimedOut()!=null) return ParentGameOverMessage.GameOverReason.PLAYER_TIMED_OUT;
		if(this.isBoardFull()) return ParentGameOverMessage.GameOverReason.BOARD_FILLED;
		throw new IllegalArgumentException("Could not find a reason. Game is probably corrupt");
	}
	private boolean isBoardFull() {
		return this.update.getTilesFilled().equals(MAX_TILES_ON_BOARD);
	}
}
