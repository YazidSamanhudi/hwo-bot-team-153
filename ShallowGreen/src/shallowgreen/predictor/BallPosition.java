/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shallowgreen.predictor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shallowgreen.game.BallGame;
import shallowgreen.model.Update;

/**
 *
 * @author dogo
 */
public class BallPosition {

	private static final Logger log = LoggerFactory.getLogger(BallGame.class);
	private final int MAX_ITER = 999999;        // simulate at most this many steps long ball travel
	private int iterations = 0;                 // completed iterations on this leg (in- or outbound)
	private double simX;                        // simulated ball Y position
	private double simY;                        // simulated ball Y position
	private double angle;                       // ball angle, to determine direction
	private double top, left, right, ySpace, preSimY;
	private final double S_CHANGE_PIXELS = 30;  // Estimate: ball trajectory slope changes about
	private final double S_CHANGE = 0.4;        //           +-0.4 per 30 pixels of offset on paddle
	private final double USABLE_PADDLE_AREA = 0.6; // Estimate: aboout 60 percent usable based on observations (n = 801)

	public BallPosition() {
	}

	/**
	 * Estimate ball Y-position when it comes to us next time.
	 *
	 * @param update Message from server, contains data such as ball position,
	 * field size, paddle positions
	 * @param xVel Ball direction vector X-part i.e. it's speed in pixels in
	 * X-direction (per time unit)
	 * @param yVel Ball direction vector Y-part i.e. it's speed in pixels in
	 * Y-direction (per time unit)
	 * @return Estimated Y-position where ball will land when it bounces from
	 * opponent paddle
	 */
	public double naiveNextMySide(Update update, double xVel, double yVel) {

		setStartParameters(update, xVel, yVel); // figure out position and direction

		if (incoming(angle)) {
			simulateInbound(update, xVel, yVel);
		} else {
			simulateOutbound(update, xVel, yVel);
			xVel *= -1.0d;                       // Bounce from opponent paddle
			simulateInbound(update, xVel, yVel);
		}
		return simY;
	}

	/**
	 * Estimate ball Y-position on enemy side when it lands there next time.
	 *
	 * @param update Message from server, contains data such as ball position,
	 * field size, paddle positions
	 * @param xVel Ball direction vector X-part i.e. it's speed in pixels in
	 * X-direction (per time unit)
	 * @param yVel Ball direction vector Y-part i.e. it's speed in pixels in
	 * Y-direction (per time unit)
	 * @return Estimated Y-position where ball will land when it bounces from
	 * opponent paddle
	 */
	public double naiveNextEnemySide(Update update, double xVel, double yVel) {

		setStartParameters(update, xVel, yVel); // figure out position and direction
		if (incoming(angle)) {
			simulateInbound(update, xVel, yVel);
			xVel *= -1.0d;                       // Bounce from our paddle
			simulateOutbound(update, xVel, yVel);
		} else {
			simulateOutbound(update, xVel, yVel);
		}
		return simY;
	}

	/**
	 *
	 * Simulate ball traveling outwards from out paddle. Changes class' internal
	 * state to reflect ball position on enemy paddle line as the ball traverses
	 * towards it in straight line.
	 *
	 * @param update Message from server, contains data such as ball position,
	 * field size, paddle positions
	 * @param xVel Ball direction vector X-part i.e. it's speed in pixels in
	 * X-direction (per time unit)
	 * @param yVel Ball direction vector Y-part i.e. it's speed in pixels in
	 * Y-direction (per time unit)
	 */
	private void simulateOutbound(Update update, double xVel, double yVel) {

		// Until we hit end of field, add yVel to Y-value and xVel to X-value
		while (simX < update.getFieldMaxWidth() && iterations < MAX_ITER) {
			simX += xVel;      // Move simulated ball
			simY += yVel;      // in both X- and Y-directions
			if (simY < update.getBallRadius()) {    // If simulated ball hits bottom, 
				yVel *= -1.0d;   // switch y-direction and 
				simY *= -1.0d;   // make negative position positive (bring ball back to field)
			} else if (simY > update.getFieldMaxHeight() - update.getBallRadius()) {  // If it hits roof,
				yVel *= -1.0d;                                                         // calculate distance by which roof was exceeded and bring back that amount
				simY = update.getFieldMaxHeight() - (simY - update.getFieldMaxHeight());
			}
			iterations++;
		}
	}

	/**
	 *
	 * Simulate ball traveling towards our paddle. Changes class' internal state
	 * to reflect ball position on our paddle line as the ball traverses towards
	 * us in straight line.
	 *
	 * @param update Message from server, contains data such as ball position,
	 * field size, paddle positions
	 * @param xVel Ball direction vector X-part i.e. it's speed in pixels in
	 * X-direction (per time unit)
	 * @param yVel Ball direction vector Y-part i.e. it's speed in pixels in
	 * Y-direction (per time unit)
	 */
	private void simulateInbound(Update update, double xVel, double yVel) {

		// For as long as the ball has not reached our end of field
		while (simX > update.getBallRadius() && iterations < MAX_ITER) {
			simX += xVel;
			simY += yVel;
			if (simY < 0) {   // SEE COMMENTS ON simulateOutbound()
				yVel *= -1.0d;
				simY *= -1.0d;
			} else if (simY > update.getFieldMaxHeight() - update.getBallRadius()) {
				yVel *= -1.0d;
				simY = update.getFieldMaxHeight() - (simY - update.getFieldMaxHeight());
			}
			iterations++;
		}
	}

	private boolean incoming(double angle) {
		return !(angle < (Math.PI / 2) && angle > (Math.PI / -2));
	}

	/**
	 * Set instance variables to reflect playfield state. Initialization routine.
	 *
	 * @param update Message from server, contains data such as ball position,
	 * field size, paddle positions
	 * @param xVel Ball direction vector X-part i.e. it's speed in pixels in
	 * X-direction (per time unit) for direction calculation
	 * @param yVel Ball direction vector Y-part i.e. it's speed in pixels in
	 * Y-direction (per time unit) for direction calculation
	 */
	private void setStartParameters(Update update, double xVel, double yVel) {
		this.simX = update.getBallX();       // simulated ball start X position
		this.simY = update.getBallY();       // simulated ball start Y position
		this.angle = Math.atan2(yVel, xVel); // figure out direction
		this.iterations = 0;
		top = update.getBallRadius();
		left = update.getPaddleWidth();
		right = update.getFieldMaxWidth() - 2 * update.getPaddleWidth();
		ySpace = update.getFieldMaxHeight() - update.getBallRadius() * 2;
	}

	public double nextMySide(Update update, double xVel, double yVel) {
		setStartParameters(update, xVel, yVel);
		nextMyY(update, xVel, yVel);
		return simY;
	}

	public double nextEnemySide(Update update, double xVel, double yVel) {
		setStartParameters(update, xVel, yVel);
		nextEnemyY(update, xVel, yVel);
		return simY;
	}

	public int targetFarthest(Update update, double xVel, double yVel) {
		HashMap<Double, Double> offset = new HashMap<>();
		double target;
		double tempBallXPosition = update.getBallX();
		double tempBallYPosition = update.getBallY();
		update.getBall().getPosition().setX(update.getPaddleWidth() * 2);    // Simulate ball bouncing
		update.getBall().getPosition().setY(nextMySide(update, xVel, yVel)); // back from our paddle

		double slope = (yVel / (-1.0 * xVel));  // called only when ball is incoming
		// 0.4 per 30 pikseliä offsettiä
		// rajat: 15 .. 45 (empiirisestä havainnosta http://www.cs.helsinki.fi/u/mcrantan/hwo/slope-pos-xyplot-2.png)
		// pelimoottorin tekijät munanneet pallon säteen verran offsettiä
		// kun pallo on < 15 tai > 45 kohdalla mailaa, tulos muuttuu hyvin epävarmaksi
		// käytetään 60% mailan koosta, siis väli 15 .. 45 = 30 pikseliä 50:stä

		int paddleOffset = (int) update.getBallRadius();
		int paddleHalfHeight = (int) (update.getPaddleHeight() / 2);
		boolean targetMaxY = (targetFarthestSide(update) == 0) ? false : true;
		int halfOfUsableAreaStart = (int) (-1.0 * (USABLE_PADDLE_AREA * paddleHalfHeight));
		for (double centerOffset = halfOfUsableAreaStart; centerOffset <= -1.0d * halfOfUsableAreaStart; centerOffset++) {
			offset.put(nextEnemySide(update, 1.0d, (centerOffset / S_CHANGE_PIXELS * S_CHANGE + (-1.0 * slope))), centerOffset);
		}
		/*		
		 if (targetZeroY) {
		 target = offset.get(Collections.min(offset.keySet())) + paddleOffset + paddleHalfHeight;
		 } else {
		 target = offset.get(Collections.max(offset.keySet())) + paddleOffset + paddleHalfHeight;
		 }
		 */
		if (targetMaxY) {
			target = offset.get(Collections.max(offset.keySet())) + paddleOffset;
			log.info("Chosen largest target: {}, enemy-Y: {}", Collections.max(offset.keySet()), update.getRightY());
		} else {
			target = offset.get(Collections.min(offset.keySet())) + paddleOffset;
			log.info("Chosen smallest target: {}", Collections.min(offset.keySet()));
		}
		
		update.getBall().getPosition().setX(tempBallXPosition);
		update.getBall().getPosition().setY(tempBallYPosition);

		return (int) target;
	}

	private double targetFarthestSide(Update update) {
		double enemyPaddlePosition = update.getRightY();
		return (enemyPaddlePosition > update.getFieldMaxHeight() / 2) ? 0 : update.getFieldMaxHeight();
	}

	/**
	 *
	 * Calculate next ball Y-position our side based on current trajectory slope.
	 * Calls itself recursively if ball is traveling towards enemy.
	 *
	 * @param update Message from server, contains data such as ball position,
	 * field size, paddle positions
	 * @param xVel Ball direction vector X-part i.e. it's speed in pixels in
	 * X-direction (per time unit)
	 * @param yVel Ball direction vector Y-part i.e. it's speed in pixels in
	 * Y-direction (per time unit)
	 */
	private void nextMyY(Update update, double xVel, double yVel) {
		double dy;                     // slope factor
		int bounces = 0;               // bounces from top and bottom walls
		double tempBallX, tempBallY;   // save and restore original values of ball position in 'update' object

		if (incoming(angle)) {
			dy = (yVel / (-1.0 * xVel));
			preSimY = dy * (update.getBallX() - left) + update.getBallY();
			handleBounce();
			log.debug("nextMyY incoming: dy: {}, preSimY = {}, ballX: {}, ballY: {}.", new Object[]{dy, preSimY, update.getBallX(), update.getBallY()});
			simY = preSimY;
		} else {
			dy = (yVel / xVel);
			preSimY = dy * (right - update.getBallX()) + update.getBallY();
			log.debug("nextMyY outgoing1: dy: {}, preSimY = {}, ballX: {}, ballY: {}.", new Object[]{dy, preSimY, update.getBallX(), update.getBallY()});
			handleBounce();
			tempBallX = update.getBallX();
			tempBallY = update.getBallY();
			update.getBall().getPosition().setX(right);
			update.getBall().getPosition().setY(preSimY);
			log.debug("nextMyY outgoing2: dy: {}, preSimY = {}, ballX: {}, ballY: {}.", new Object[]{dy, preSimY, update.getBallX(), update.getBallY()});
			nextMySide(update, (-1.0 * xVel), ((bounces % 2 == 1) ? (-1.0 * yVel) : yVel));
			update.getBall().getPosition().setX(tempBallX);
			update.getBall().getPosition().setY(tempBallY);
			log.debug("nextMyY outgoing3: dy: {}, preSimY = {}, ballX: {}, ballY: {}.", new Object[]{dy, preSimY, update.getBallX(), update.getBallY()});
			simY = preSimY;
		}

		//	log.info("nextY predict: dy: {}, simY = {}, ballX: {}, ballY: {}.", new Object[]{dy, simY, update.getBallX(), update.getBallY()});
	}

	private void nextEnemyY(Update update, double xVel, double yVel) {
		double dy;                     // slope factor
		int bounces = 0;               // bounces from top and bottom walls
		double tempBallX, tempBallY;   // save and restore original values of ball position in 'update' object

		if (!incoming(angle)) {
			dy = (yVel / xVel);
			preSimY = dy * (update.getBallX() - right) + update.getBallY();
			handleBounce();
			log.debug("nextEnemyY outgoing: dy: {}, preSimY = {}, ballX: {}, ballY: {}.", new Object[]{dy, preSimY, update.getBallX(), update.getBallY()});
			simY = preSimY;
		} else {
			dy = (yVel / (-1.0 * xVel));
			preSimY = dy * (left - update.getBallX()) + update.getBallY();
			log.debug("nextEnemyY outgoing1: dy: {}, preSimY = {}, ballX: {}, ballY: {}.", new Object[]{dy, preSimY, update.getBallX(), update.getBallY()});
			handleBounce();
			tempBallX = update.getBallX();
			tempBallY = update.getBallY();
			update.getBall().getPosition().setX(left);
			update.getBall().getPosition().setY(preSimY);
			log.debug("nextEnemyY outgoing2: dy: {}, preSimY = {}, ballX: {}, ballY: {}.", new Object[]{dy, preSimY, update.getBallX(), update.getBallY()});
			nextEnemySide(update, (-1.0 * xVel), ((bounces % 2 == 1) ? (-1.0 * yVel) : yVel));
			update.getBall().getPosition().setX(tempBallX);
			update.getBall().getPosition().setY(tempBallY);
			log.debug("nextEnemyY outgoing3: dy: {}, preSimY = {}, ballX: {}, ballY: {}.", new Object[]{dy, preSimY, update.getBallX(), update.getBallY()});
			simY = preSimY;
		}

		//	log.info("nextY predict: dy: {}, simY = {}, ballX: {}, ballY: {}.", new Object[]{dy, simY, update.getBallX(), update.getBallY()});
	}

	private int handleBounce() {
		int bounces = 0;
		if (preSimY < top) {
			bounces = Math.abs((int) (preSimY / ySpace)) + 1; // round down, then add one
			if (bounces % 2 == 1) {
				preSimY = (ySpace + top) - (preSimY + (bounces * ySpace)) + top;
				log.debug("nextY: preSimY < top, odd bounces, simY now: {}.", simY);
				return bounces;
			} else {
				preSimY = preSimY + (bounces * ySpace) + top;
				log.debug("nextY: preSimY < top, even bounces, simY now: {}.", simY);
				return bounces;
			}
		}
		if (preSimY > ySpace + top) {
			bounces = (int) (preSimY / ySpace); // round down
			if (bounces % 2 == 1) {
				preSimY = (ySpace + top) - (preSimY - bounces * ySpace) + top;
				log.debug("nextY: preSimY was > field_height, odd bounces, simY now: {}.", simY);
				return bounces;
			} else {
				preSimY = preSimY - bounces * ySpace + top;
				log.debug("nextY: preSimY was > field_height, even bounces, simY now: {}.", simY);
				return bounces;
			}
		}
		return bounces;
	}
}
