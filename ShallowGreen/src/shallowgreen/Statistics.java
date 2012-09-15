/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shallowgreen;

import shallowgreen.model.Update;

/**
 *
 * @author dogo
 */
public class Statistics {

	private int gamesWon = 0;
	private int gamesPlayed = 0;
	private double minX = 100;
	private double maxX = 0;
	private double minY = 100;
	private double maxY = 0;

	public Statistics(Update update) {
		updatePositionStats(update);
	}

	public Statistics() {
	}

	public int getGamesWon() {
		return gamesWon;
	}

	public int getGamesPlayed() {
		return gamesPlayed;
	}

	public double getMinX() {
		return minX;
	}

	public double getMaxX() {
		return maxX;
	}

	public double getMinY() {
		return minY;
	}

	public double getMaxY() {
		return maxY;
	}

	public void gameWon() {
		this.gamesWon += 1;
		this.gamesPlayed += 1;
	}

	public void gameLost() {
		this.gamesPlayed += 1;
	}

	public final void updatePositionStats(Update update) {
		this.setMinX(Math.min(this.minX, update.getBallX()));
		this.setMaxX(Math.max(this.maxX, update.getBallX()));
		this.setMinY(Math.min(this.minY, update.getBallY()));
		this.setMaxY(Math.max(this.maxY, update.getBallY()));
	}

	public String toString() {
		return "Games played: " + gamesPlayed
						+ ", games won: " + gamesWon
						+ ", games lost: " + (gamesPlayed - gamesWon)
						+ ", minX: " + String.format("%3.2f", minX)
						+ ", maxX: " + String.format("%3.2f", maxX)
						+ ", minY: " + String.format("%3.2f", minY)
						+ ", maxY: " + String.format("%3.2f", maxY) + "\n";
	}

	/**
	 * @param minX the minX to set
	 */
	public void setMinX(double minX) {
		this.minX = minX;
	}

	/**
	 * @param maxX the maxX to set
	 */
	public void setMaxX(double maxX) {
		this.maxX = maxX;
	}

	/**
	 * @param minY the minY to set
	 */
	public void setMinY(double minY) {
		this.minY = minY;
	}

	/**
	 * @param maxY the maxY to set
	 */
	public void setMaxY(double maxY) {
		this.maxY = maxY;
	}
}