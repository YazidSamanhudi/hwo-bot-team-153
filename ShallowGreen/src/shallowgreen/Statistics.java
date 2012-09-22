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

	private final char UPDATES_TO_KEEP = 100;
	private int counter = 0;
	private int gamesWon = 0;
	private int gamesPlayed = 0;
	private double minX = 100;
	private double maxX = 0;
	private double minY = 100;
	private double maxY = 0;
	private double enemyPaddleSpeed = 0;
	private double enemyPaddleDirection = 0;
	private double enemyPaddlePosition;
	private long[] jitter = new long[UPDATES_TO_KEEP];
	private long jitterLast = 0;
	private double[] rtts = new double[UPDATES_TO_KEEP];
	private double rttLast = 0;
	private long[] dt = new long[UPDATES_TO_KEEP];
	private long dtLast = 0;
	private Update[] updates = new Update[UPDATES_TO_KEEP];
	
	public Statistics() {
	}
	
	// When initialized, put first received update twice into ring buffer arrays
	public Statistics(Update update) {
		updates[0] = update;
		jitter[0] = 0;
		counter++;
		updateStatistics(update, 0.0);
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

	public final void updateStatistics(Update update, double rtt) {
		int curr = (counter % UPDATES_TO_KEEP);
		int prev = ((counter - 1) % UPDATES_TO_KEEP);
		updates[curr] = update;
		rtts[curr] = rtt;
		rttLast = rtt;
		dtLast = updates[curr].getReceiveTimeMillis() - updates[prev].getReceiveTimeMillis();
		dt[curr] = dtLast;
		setMinX(Math.min(minX, update.getBallX()));
		setMaxX(Math.max(maxX, update.getBallX()));
		setMinY(Math.min(minY, update.getBallY()));
		setMaxY(Math.max(maxY, update.getBallY()));
		enemyPaddlePosition = update.getRightY();
		jitter[curr] = dtLast - (updates[curr].getTime() - updates[prev].getTime());
		jitterLast = jitter[curr];
		counter++;
	}

	@Override
	public String toString() {
		int updatesAvailable = Math.min(UPDATES_TO_KEEP, counter);
		long minJitter = 999999, avgJitter = 0, maxJitter = 0;		
		for (int i = 0; i < updatesAvailable; i++) {
			minJitter = Math.min(jitter[i], minJitter);
			maxJitter = Math.max(jitter[i], maxJitter);
			avgJitter += jitter[i];
		}
		avgJitter /= updatesAvailable;
		return "minX: " + String.format("%3.2f", minX)
						+ ", maxX: " + String.format("%3.2f", maxX)
						+ ", minY: " + String.format("%3.2f", minY)
						+ ", maxY: " + String.format("%3.2f", maxY)
						+ ", RTT: " + String.format("%3.2f", rttLast)
						+ ", dt: " + String.format("%3d", dtLast)
						+ ", jitter now/min/avg/max: " + String.format("%d", jitterLast) + "/" + String.format("%d", minJitter) + "/" + String.format("%d", avgJitter) +"/" + String.format("%d", maxJitter) + " ms";
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