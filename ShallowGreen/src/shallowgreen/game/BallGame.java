package shallowgreen.game;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shallowgreen.Game;
import shallowgreen.message.ChangeDirMessage;
import shallowgreen.model.Player;
import shallowgreen.model.Update;

/**
 * put the paddle where the ball is
 */
public class BallGame extends Game {
	private static final Logger log = LoggerFactory.getLogger(BallGame.class);

	private static final long TICKS = 1000;
	private static final int MESSAGES = 10;

	private double speed;
	private double minVelocity = 999;
	private double maxVelocity;
	private Update prevUpdate;
	private double paddleTarget = 240; // FIXME: hardcoded middle
	private double prevAngle;
	private long messageLimitTick;
	private int messages;

	@Override
	public void update(Update update) {
		double xVel, yVel;
		long deltaTime;
		boolean incoming = true;
		// calculate which way we should be going
		Player me = update.getLeft();
		if (prevUpdate != null) {
			if ((update.getTime() - messageLimitTick) > TICKS) {
				messages = 0;
				messageLimitTick = update.getTime();
			}
			Player prevMe = prevUpdate.getLeft();
			xVel = update.getBallX() - prevUpdate.getBallX();
			yVel = update.getBallY() - prevUpdate.getBallY();
			deltaTime = update.getTime() - prevUpdate.getTime();
			//due to multiplication, always positive
			double distance = Math.sqrt((xVel * xVel) + (yVel * yVel)) / deltaTime;
			double paddleVel = (me.getY() - prevMe.getY()) / deltaTime;
			double angle = Math.atan2(yVel, xVel);
			log.debug("Speed: {}, Angle: {}, PT: {}, PV: {}, min: {}, max: {}", new Object[]{distance, angle, paddleTarget, paddleVel, minVelocity, maxVelocity});
			if (distance < minVelocity) {
				minVelocity = distance;
			}
			if (distance > maxVelocity) {
				maxVelocity = distance;
			}
			
			incoming = ballIsIncoming(angle);

			if (incoming && prevAngle != angle) {
				estimateBallReturnYPosition(update, xVel, yVel, angle);
			}
		}
		if (!incoming) {
			paddleTarget = update.getFieldMaxHeight() / 2 - (update.getPaddleHeight() / 2);
		}
//		else
//			paddleTarget = update.getBallY();

		// safety one pixel
		double deadZone = (update.getPaddleHeight() / 2) - 1.0d + update.getBallRadius();
		double yDiff = paddleTarget - me.getY() - (update.getPaddleHeight() / 2);
		ChangeDirMessage cdm = null;
		if (yDiff > deadZone && speed <= 0.0d) {
			cdm = new ChangeDirMessage(1.0d);
			speed = 1.0d;
		} else if (yDiff < -deadZone && speed >= 0.0d) {
			cdm = new ChangeDirMessage(-1.0d);
			speed = -1.0d;
		} else if (speed != 0.0d && yDiff < deadZone && yDiff > -deadZone) {
			cdm = new ChangeDirMessage(0.0d);
			speed = 0.0d;
		}

		prevUpdate = update;

		// send the command, if any
		if (cdm != null && messages < MESSAGES) {
			try {
				connection.sendMessage(cdm);
				messages++;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.error("Whooooops.", e);
			}
		}
	}

	private boolean ballIsIncoming(double angle) {
		return !(angle < (Math.PI / 2) && angle > (Math.PI / -2));
	}

	private void estimateBallReturnYPosition(Update update, double xVel, double yVel, double angle) {
		{
			int safety = 999999;
			double simX = update.getBallX();
			double simY = update.getBallY();
			while (simX > 0 && safety > 0) {
//					System.out.println("X:" + simX + ",Y:" + simY + ",Xv:" + xVel + ",Yv:" + yVel);
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
			paddleTarget = simY;
			prevAngle = angle;
		}
	}

	@Override
	public void gameIsOver(String winner) {
		log.info("winner: {}", winner);
	}

	@Override
	public void gameStarted(List<String> players) {
		log.info("new game with players: {}", players);
	}
}
