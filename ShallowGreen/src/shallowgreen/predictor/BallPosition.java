/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shallowgreen.predictor;

import shallowgreen.model.Update;

/**
 *
 * @author dogo
 */
public class BallPosition {

	private final int MAX_ITER = 999999;  // simulate at most this many steps long ball travel
  private int iterations = 0;           // completed iterations on this leg (in- or outbound)
	private double simX;                  // simulated ball Y position
	private double simY;                  // simulated ball Y position
	private double angle;                 // ball angle, to determine direction
	
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
	public double nextMySide(Update update, double xVel, double yVel) {
		
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

	public double nextEnemySide(Update update, double xVel, double yVel) {

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
	 * Simulate ball traveling outwards from out paddle. Changes
	 * class' internal state to reflect ball position on enemy paddle line
	 * as the ball traverses towards it in straight line.
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
				yVel *= -1.0d;                                 // calculate distance by which roof was exceeded and bring back that amount
				simY = update.getFieldMaxHeight() - (simY - update.getFieldMaxHeight());
			}
			iterations++;
		}
	}

	/**
	 * 
	 * Simulate ball traveling towards our paddle. Changes
	 * class' internal state to reflect ball position on our paddle line
	 * as the ball traverses towards us in straight line.
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
	}
}
