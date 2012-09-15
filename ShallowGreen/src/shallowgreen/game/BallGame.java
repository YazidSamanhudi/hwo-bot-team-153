package shallowgreen.game;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shallowgreen.Game;
import shallowgreen.message.ChangeDirMessage;
import shallowgreen.model.Player;
import shallowgreen.model.Update;
import shallowgreen.predictor.RTT;
import shallowgreen.predictor.BallPosition;

/**
 * put the paddle where the ball is
 */
public class BallGame extends Game {

	private static final Logger log = LoggerFactory.getLogger(BallGame.class);
	private static final long TICKS = 1000;
	private static final int MESSAGES = 10;
	private String myName;
	private double speed;
	private double minVelocity = 999;
	private double maxVelocity;
	private Update prevUpdate;
	private double paddleTarget = 240; // FIXME: hardcoded middle
	private double prevAngle;
	private long messageLimitTick;
	private int messages;
	private int gamesWon = 0;
	private boolean firstUpdate = true;
	private double missiles = 0.0;
	private RTT rttEstimator;
	private BallPosition bpEstimator;
	private Statistics stats;

	@Override
	public void update(Update update) {

		boolean incoming = true;
		double ballXVelocity, ballYVelocity;
		long updateDeltaTime;
		// calculate which way we should be going
		Player myPreviousPosition;
		Player myCurrentPosition = update.getLeft();

		if (firstUpdate) {
			bpEstimator = new BallPosition();
			stats = new Statistics(update);
			missiles = update.getNrOfMissiles();
			log.debug("Missiles: " + missiles + ", current RTT EMA: " + rttEstimator.getRTTmsEstimate());
			if (!incoming) {
				paddleTarget = update.getFieldMaxHeight() / 2 - (update.getPaddleHeight() / 2);
			}
		}

		else {

			if (update.getNrOfMissiles() != missiles) {
				log.info("Wahoo, missiles says '" + update.getNrOfMissiles() + "'!");
			}

			checkPerTickMessageLimit(update);
			updateDeltaTime = update.getTime() - prevUpdate.getTime();
			myPreviousPosition = prevUpdate.getLeft();

			ballXVelocity = update.getBallX() - prevUpdate.getBallX();
			ballYVelocity = update.getBallY() - prevUpdate.getBallY();
			// due to multiplication, always positive
			// this is travel distance per update interval time
			double ballTravelDistance = Math.sqrt((ballXVelocity * ballXVelocity) + (ballYVelocity * ballYVelocity)) / updateDeltaTime;
			double ballAngle = Math.atan2(ballYVelocity, ballXVelocity);

//			double paddleVelocity = (myCurrentPosition.getY() - myPreviousPosition.getY()) / updateDeltaTime;
//			log.debug("Speed: {}, Angle: {}, PT: {}, PV: {}, min: {}, max: {}", new Object[]{ballTravelDistance, ballAngle, paddleTarget, paddleVelocity, minVelocity, maxVelocity});

			setSeenBallVelocityLimits(ballTravelDistance);

			incoming = ballIsIncoming(ballAngle);

//			if (incoming && prevAngle != ballAngle) {
				paddleTarget = bpEstimator.nextMySide(update, ballXVelocity, ballYVelocity);
//			}

//			if (!incoming && prevAngle != ballAngle) {
//				paddleTarget = bpEstimator.nextMySide(update, ballXVelocity, ballYVelocity);
//			}
				stats.updatePositionStats(update);
		}

		// safety one pixel
		double deadZone = (update.getPaddleHeight() / 2) - 2.0d + update.getBallRadius();
		double yDiff = paddleTarget - myCurrentPosition.getY() - (update.getPaddleHeight() / 2);
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
		firstUpdate = false;

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

	private void setSeenBallVelocityLimits(double ballTravelDistance) {
		if (ballTravelDistance < minVelocity) {
			minVelocity = ballTravelDistance;
		}
		if (ballTravelDistance > maxVelocity) {
			maxVelocity = ballTravelDistance;
		}
	}

	private void checkPerTickMessageLimit(Update update) {
		if ((update.getTime() - messageLimitTick) > TICKS) {
			messages = 0;
			messageLimitTick = update.getTime();
		}
	}

	private boolean ballIsIncoming(double angle) {
		return !(angle < (Math.PI / 2) && angle > (Math.PI / -2));
	}

	@Override
	public Statistics getStatistics() {
		return stats;
	}
	
	@Override
	public void setRTTEstimator(RTT rttEstimator) {
		this.rttEstimator = rttEstimator;
	}

	@Override
	public void gameIsOver(String winner) {
		if (winner.equalsIgnoreCase(myName)) {
			stats.gameWon();
		} else {
			stats.gameLost();
		}
	}
	
	@Override
	public void gameStarted(List<String> players) {
		myName = players.get(0);
		log.info("New game. Players: {}", players);
	}
}
