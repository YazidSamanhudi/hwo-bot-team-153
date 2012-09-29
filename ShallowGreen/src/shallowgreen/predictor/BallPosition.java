/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shallowgreen.predictor;

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
//	private double angle;                       // ball angle, to determine direction
	private double top, left, right, ySpace, preSimY;
	private boolean incoming;
	private final double S_CHANGE_PIXELS = 30;  // Estimate: ball trajectory slope changes about
	private final double S_CHANGE = 0.40;       //           +-0.40 per 30 pixels of offset on paddle
	private final double USABLE_PADDLE_AREA = 0.90; // Estimate about usable paddle area based on observations

	public BallPosition() {
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
//		this.angle = Math.atan2(yVel, xVel); // figure out direction
		this.incoming = (xVel < 0.0d);
		this.iterations = 0;
		this.top = update.getBallRadius();
		this.left = update.getPaddleWidth();
//		right = update.getFieldMaxWidth() - 2 * update.getPaddleWidth();
		this.right = update.getFieldMaxWidth() - update.getPaddleWidth();
		this.ySpace = update.getFieldMaxHeight() - update.getBallRadius() * 2;
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

	/**
	 * Estimate offset from center of paddle to increase or decrease ball
	 * trajectory slope so that it is as far as possible from enemy paddle upon
	 * arrival.
	 *
	 * We have observed that when the ball bounces slightly off-center from the
	 * paddle, it's slope upon bouncing back is altered. Hitting top of the paddle
	 * increases the angle (when traveling towards enemy) and hitting bottom of
	 * the paddle decreases the angle (when traveling towards enemy). This is
	 * helpful as the change in slope can be simply summed with expected slope.
	 *
	 * We have observed the change in slope to be about n*30/0.35 where n is the
	 * amount of pixels from the center of the paddle, negative-n for offset
	 * towards up and positive for offset towards bottom.
	 *
	 * This method first determines if the ball should be launched towards top or
	 * bottom of the field. It then iterates through paddle area offsets from
	 * paddle center and determines which one is nearest the top/bottom of the
	 * field, as required to make the ball land as far from enemy paddle as
	 * possible. The results are stored in HashMap as (position, offset) pairs and
	 * min/max from the keySet is chosen; this gives us the calculated offset
	 * which is returned to the calling process.
	 *
	 * @param update Message from server, contains data such as ball position,
	 * field size, paddle positions
	 * @param xVel Ball direction vector X-part i.e. it's speed in pixels in
	 * X-direction (per time unit) for direction calculation
	 * @param yVel Ball direction vector Y-part i.e. it's speed in pixels in
	 * Y-direction (per time unit) for direction calculation
	 * @return Offset from center of paddle
	 */
	public double targetFarthest(Update update, double xVel, double yVel) {
		HashMap<Double, Double> offset = new HashMap<>();
//		double target;
		double tempBallXPosition = update.getBallX();
		double tempBallYPosition = update.getBallY();
		double spreadMin, spreadMax;
//		update.getBall().getPosition().setX(update.getPaddleWidth() * 2);    // Simulate ball bouncing
		update.getBall().getPosition().setX(left);                           // Simulate position of ball bouncing
		update.getBall().getPosition().setY(nextMySide(update, xVel, yVel)); // back from our paddle

		double slope = (yVel / (-1.0 * xVel));  // called only when ball is incoming
		// rajat: 15 .. 45 (empiirisestä havainnosta http://www.cs.helsinki.fi/u/mcrantan/hwo/slope-pos-xyplot-2.png)
		// pelimoottorin tekijät munanneet pallon säteen verran offsettiä?
		int paddleOffset = (int) update.getBallRadius();
		paddleOffset = 0;
		int paddleHalfHeight = (int) (update.getPaddleHeight() / 2);
		int halfOfUsableAreaStart = (int) (-1.0 * (USABLE_PADDLE_AREA * paddleHalfHeight));
		for (double centerOffset = halfOfUsableAreaStart; centerOffset <= -1.0d * halfOfUsableAreaStart; centerOffset++) {
			offset.put(nextEnemySide(update, 1.0d, (centerOffset / S_CHANGE_PIXELS * S_CHANGE + (-1.0 * slope))), centerOffset);
		}

		// restore original ball position in update as other methods need this info intact
		update.getBall().getPosition().setX(tempBallXPosition);
		update.getBall().getPosition().setY(tempBallYPosition);

		// offset's smallest key represents the estimated ball target position spread minimum
		spreadMin = Collections.min(offset.keySet());
		// offset's largest key represents the estimated ball target position spread maximum
		spreadMax = Collections.max(offset.keySet());
		
		// ..so we want to return the farthest possible place to put the ball into from enemy paddle. Naive strategy and
		// TODO: the strategy can be enhanced by calculating the resulting enemy serve spread and, if it causes a situation
		//       where our side paddle might not make it to the ball, change the strategy.
		//       One example of losing proposition is when ball comes to our side near top or bottom wall.
		//       Let's assume the ball has already gained some speed (especially Y-speed). If we decide to bounce
		//       the ball back in such a way that the slope increases as much as possible, the return ball might land
		//       near center at enemy side and enemy might change the slope to land the ball in very top corner
		//       of our side, resulting in a situation where it is not possible for us to make it to the corner.
		return ((Math.abs(spreadMin - update.getRightY())) > (Math.abs(spreadMax - update.getRightY())) ? offset.get(spreadMin) : offset.get(spreadMax));
	}

	/**
	 *
	 * Calculate next ball Y-position on our side based on current trajectory
	 * slope. Calls itself recursively if ball is traveling towards enemy.
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
		int bounces;                   // bounces from top and bottom walls
		double tempBallX, tempBallY;   // save and restore original values of ball position in 'update' object

		if (incoming) {
			dy = (yVel / (-1.0 * xVel));
			preSimY = dy * (update.getBallX() - left) + update.getBallY();
			handleBounce();
			log.debug("nextMyY incoming: dy: {}, preSimY = {}, ballX: {}, ballY: {}.", new Object[]{dy, preSimY, update.getBallX(), update.getBallY()});
			simY = preSimY;
		} else {
			dy = (yVel / xVel);
			preSimY = dy * (right - update.getBallX()) + update.getBallY(); // Y-position when ball hits right side paddle
			log.debug("nextMyY outgoing1: dy: {}, preSimY = {}, ballX: {}, ballY: {}.", new Object[]{dy, preSimY, update.getBallX(), update.getBallY()});
			bounces = handleBounce();
			tempBallX = update.getBallX();                                  // Store current
			tempBallY = update.getBallY();                                  // ball position
			update.getBall().getPosition().setX(right);
			update.getBall().getPosition().setY(preSimY);                   // Make ball look like it hits enemy side paddle
			log.debug("nextMyY outgoing2: dy: {}, preSimY = {}, ballX: {}, ballY: {}.", new Object[]{dy, preSimY, update.getBallX(), update.getBallY()});
			nextMySide(update, (-1.0 * xVel), ((bounces % 2 == 1) ? (-1.0 * yVel) : yVel));
			update.getBall().getPosition().setX(tempBallX);                 // Restore ball
			update.getBall().getPosition().setY(tempBallY);                 // position
			log.debug("nextMyY outgoing3: dy: {}, preSimY = {}, ballX: {}, ballY: {}.", new Object[]{dy, preSimY, update.getBallX(), update.getBallY()});
//			simY = preSimY;
		}

		//	log.info("nextY predict: dy: {}, simY = {}, ballX: {}, ballY: {}.", new Object[]{dy, simY, update.getBallX(), update.getBallY()});
	}

	/**
	 *
	 * Calculate next ball Y-position on enemy side based on current trajectory
	 * slope. Calls itself recursively if ball is traveling towards enemy.
	 *
	 * @param update Message from server, contains data such as ball position,
	 * field size, paddle positions
	 * @param xVel Ball direction vector X-part i.e. it's speed in pixels in
	 * X-direction (per time unit)
	 * @param yVel Ball direction vector Y-part i.e. it's speed in pixels in
	 * Y-direction (per time unit)
	 */
	private void nextEnemyY(Update update, double xVel, double yVel) {
		double dy;                     // slope factor
		int bounces = 0;               // bounces from top and bottom walls
		double tempBallX, tempBallY;   // save and restore original values of ball position in 'update' object

		if (!incoming) {
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

	/**
	 * Calculate real position from projected Y-position that may be outside
	 * playfield.
	 *
	 * Internal variable preSimY is set to estimated ball Y-position when it hits
	 * next wall.
	 *
	 * nextMySide and nextEnemySide return projected Y-position, that may be
	 * negative or greater than actual playfield size, which means the ball has
	 * bounced. If the count of bounces is odd, the slope sign changes and ball
	 * Y-position is (projected-Y % playfield_y_size) distance from top or bottom
	 * wall (depending on direction of travel; top wall for upwards travel, bottom
	 * wall for downward travel).
	 *
	 * If the count of bounces is even, slope sign stays the same and ball
	 * Y-position is (projected-Y % playfield_y_size) distance from top wall for
	 * downwards traveling ball and (projected-Y % playfield_y_size) distance from
	 * bottom wall for upwards traveling ball.
	 *
	 * @return Count of bounces.
	 *
	 */
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

		if (incoming) {
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
		if (incoming) {
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

}
