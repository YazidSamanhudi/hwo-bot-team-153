/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shallowgreen.predictor;

import shallowgreen.model.Ball;
import shallowgreen.model.Update;

/**
 *
 * @author dogo
 */
public class BallPosition {

	public BallPosition() {
	}

	public double returnPosition(Update update, double xVel, double yVel, double angle) {

		int safety = 999999;
		double simX = update.getBallX();
		double simY = update.getBallY();
		
		while (simX < update.getFieldMaxWidth() - update.getPaddleWidth() && safety > 0) {
			simX += xVel;
			simY += yVel;
			if (simY < 0) {
				yVel *= -1.0d;
				simY *= -1.0d;
			} else if (simY > update.getFieldMaxHeight()) {
				yVel *= -1.0d;
				simY = update.getFieldMaxHeight() - (simY - update.getFieldMaxHeight());
			}
			safety--;
		}

		xVel *= -1.0d;  // Regular bounce from opponent paddle

		while (simX > 0 && safety > 0) {
			simX += xVel;
			simY += yVel;
			if (simY < 0) {
				yVel *= -1.0d;
				simY *= -1.0d;
			} else if (simY > update.getFieldMaxHeight()) {
				yVel *= -1.0d;
				simY = update.getFieldMaxHeight() - (simY - update.getFieldMaxHeight());
			}
			safety--;
		}

		return simY;
	}

	public double targetPosition(Update update, double xVel, double yVel, double angle) {

		int safety = 999999;
		double simX = update.getBallX();
		double simY = update.getBallY();
		
		while (simX > 0 && safety > 0) {
			simX += xVel;
			simY += yVel;
			if (simY < 0) {
				yVel *= -1.0d;
				simY *= -1.0d;
			} else if (simY > update.getFieldMaxHeight()) {
				yVel *= -1.0d;
				simY = update.getFieldMaxHeight() - (simY - update.getFieldMaxHeight());
			}
			safety--;
		}
		
		return simY;
	}
}
