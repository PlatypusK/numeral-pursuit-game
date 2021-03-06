/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * This code was generated by https://code.google.com/p/google-apis-client-generator/
 * (build: 2013-12-19 23:55:21 UTC)
 * on 2014-01-09 at 01:45:13 UTC 
 * Modify at your own risk.
 */

package com.appspot.numeralpursuit.gameserver.model;

/**
 * Model definition for GameUpdate.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the gameserver. For a detailed explanation see:
 * <a href="http://code.google.com/p/google-http-java-client/wiki/JSON">http://code.google.com/p/google-http-java-client/wiki/JSON</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class GameUpdate extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @com.google.api.client.json.JsonString
  private java.lang.Long firstUpdateTime;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @com.google.api.client.json.JsonString
  private java.lang.Long forfeitedBy;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @com.google.api.client.json.JsonString
  private java.lang.Long gameId;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String gameName;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Boolean gameTimedOut;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer gameType;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @com.google.api.client.json.JsonString
  private java.lang.Long maxGameTime;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @com.google.api.client.json.JsonString
  private java.lang.Long maxUpdateTime;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Boolean moveCorrect;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer moveNr;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<java.lang.Integer> newGrid;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer place;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @com.google.api.client.json.JsonString
  private java.lang.Long playerTimedOut;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @com.google.api.client.json.JsonString
  private java.util.List<java.lang.Long> players;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<java.lang.Integer> score;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer tilesFilled;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @com.google.api.client.json.JsonString
  private java.lang.Long updateTime;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer value;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @com.google.api.client.json.JsonString
  private java.lang.Long whoMoved;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @com.google.api.client.json.JsonString
  private java.lang.Long winner;

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Long getFirstUpdateTime() {
    return firstUpdateTime;
  }

  /**
   * @param firstUpdateTime firstUpdateTime or {@code null} for none
   */
  public GameUpdate setFirstUpdateTime(java.lang.Long firstUpdateTime) {
    this.firstUpdateTime = firstUpdateTime;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Long getForfeitedBy() {
    return forfeitedBy;
  }

  /**
   * @param forfeitedBy forfeitedBy or {@code null} for none
   */
  public GameUpdate setForfeitedBy(java.lang.Long forfeitedBy) {
    this.forfeitedBy = forfeitedBy;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Long getGameId() {
    return gameId;
  }

  /**
   * @param gameId gameId or {@code null} for none
   */
  public GameUpdate setGameId(java.lang.Long gameId) {
    this.gameId = gameId;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getGameName() {
    return gameName;
  }

  /**
   * @param gameName gameName or {@code null} for none
   */
  public GameUpdate setGameName(java.lang.String gameName) {
    this.gameName = gameName;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Boolean getGameTimedOut() {
    return gameTimedOut;
  }

  /**
   * @param gameTimedOut gameTimedOut or {@code null} for none
   */
  public GameUpdate setGameTimedOut(java.lang.Boolean gameTimedOut) {
    this.gameTimedOut = gameTimedOut;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getGameType() {
    return gameType;
  }

  /**
   * @param gameType gameType or {@code null} for none
   */
  public GameUpdate setGameType(java.lang.Integer gameType) {
    this.gameType = gameType;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Long getMaxGameTime() {
    return maxGameTime;
  }

  /**
   * @param maxGameTime maxGameTime or {@code null} for none
   */
  public GameUpdate setMaxGameTime(java.lang.Long maxGameTime) {
    this.maxGameTime = maxGameTime;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Long getMaxUpdateTime() {
    return maxUpdateTime;
  }

  /**
   * @param maxUpdateTime maxUpdateTime or {@code null} for none
   */
  public GameUpdate setMaxUpdateTime(java.lang.Long maxUpdateTime) {
    this.maxUpdateTime = maxUpdateTime;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Boolean getMoveCorrect() {
    return moveCorrect;
  }

  /**
   * @param moveCorrect moveCorrect or {@code null} for none
   */
  public GameUpdate setMoveCorrect(java.lang.Boolean moveCorrect) {
    this.moveCorrect = moveCorrect;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getMoveNr() {
    return moveNr;
  }

  /**
   * @param moveNr moveNr or {@code null} for none
   */
  public GameUpdate setMoveNr(java.lang.Integer moveNr) {
    this.moveNr = moveNr;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.util.List<java.lang.Integer> getNewGrid() {
    return newGrid;
  }

  /**
   * @param newGrid newGrid or {@code null} for none
   */
  public GameUpdate setNewGrid(java.util.List<java.lang.Integer> newGrid) {
    this.newGrid = newGrid;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getPlace() {
    return place;
  }

  /**
   * @param place place or {@code null} for none
   */
  public GameUpdate setPlace(java.lang.Integer place) {
    this.place = place;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Long getPlayerTimedOut() {
    return playerTimedOut;
  }

  /**
   * @param playerTimedOut playerTimedOut or {@code null} for none
   */
  public GameUpdate setPlayerTimedOut(java.lang.Long playerTimedOut) {
    this.playerTimedOut = playerTimedOut;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.util.List<java.lang.Long> getPlayers() {
    return players;
  }

  /**
   * @param players players or {@code null} for none
   */
  public GameUpdate setPlayers(java.util.List<java.lang.Long> players) {
    this.players = players;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.util.List<java.lang.Integer> getScore() {
    return score;
  }

  /**
   * @param score score or {@code null} for none
   */
  public GameUpdate setScore(java.util.List<java.lang.Integer> score) {
    this.score = score;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getTilesFilled() {
    return tilesFilled;
  }

  /**
   * @param tilesFilled tilesFilled or {@code null} for none
   */
  public GameUpdate setTilesFilled(java.lang.Integer tilesFilled) {
    this.tilesFilled = tilesFilled;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Long getUpdateTime() {
    return updateTime;
  }

  /**
   * @param updateTime updateTime or {@code null} for none
   */
  public GameUpdate setUpdateTime(java.lang.Long updateTime) {
    this.updateTime = updateTime;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getValue() {
    return value;
  }

  /**
   * @param value value or {@code null} for none
   */
  public GameUpdate setValue(java.lang.Integer value) {
    this.value = value;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Long getWhoMoved() {
    return whoMoved;
  }

  /**
   * @param whoMoved whoMoved or {@code null} for none
   */
  public GameUpdate setWhoMoved(java.lang.Long whoMoved) {
    this.whoMoved = whoMoved;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Long getWinner() {
    return winner;
  }

  /**
   * @param winner winner or {@code null} for none
   */
  public GameUpdate setWinner(java.lang.Long winner) {
    this.winner = winner;
    return this;
  }

  @Override
  public GameUpdate set(String fieldName, Object value) {
    return (GameUpdate) super.set(fieldName, value);
  }

  @Override
  public GameUpdate clone() {
    return (GameUpdate) super.clone();
  }

}
