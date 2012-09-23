package shallowgreen.game;

import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shallowgreen.Game;
import shallowgreen.Statistics;
import shallowgreen.message.ChangeDirMessage;
import shallowgreen.model.Player;
import shallowgreen.model.Update;
import shallowgreen.predictor.BallPosition;
import shallowgreen.predictor.RTT;

/**
 * put the paddle where the ball is
 */
public class BallGame extends Game {

	private static final Logger log = LoggerFactory.getLogger(BallGame.class);
	private static final long TICKS = 1000;
	private static final int MESSAGES = 10;
	private static final double DEADZONE_1 = 22.0d;
	private static final double DEADZONE_2 = 7.0d;
	private static final double DZ_1_SPEED = 1.0d;
	private static final double DZ_2_SPEED = 0.3d;
	private String myName;
	private double speed;
	private double minVelocity = 999;
	private double maxVelocity;
	private Update prevUpdate;
	private double paddleTarget = 240; // FIXME: hardcoded middle
	private double prevAngle;
	private long messageLimitTick;
	private int messages;
	private boolean firstUpdate = true;
	private double missiles = 0.0;
	private RTT rttEstimator;
	private BallPosition bpEstimator;
	private Statistics stats;
	double degAngle, prevDegAngle, incomingAngle, outgoingAngle, prevPt, currentTargetZone;

	@Override
	public void update(Update update) {

		boolean incoming = true;
		double ballXVelocity, ballYVelocity;
		long updateDeltaTime;
		// calculate which way we should be going
		Player myPreviousPosition;
		Player myCurrentPosition = update.getLeft();

		if (firstUpdate) {
			currentTargetZone = DEADZONE_1;
			speed = 0.0d;
			rttEstimator.stop();
			bpEstimator = new BallPosition();
			stats = new Statistics(update);
			missiles = update.getNrOfMissiles();
			log.debug("Missiles: " + missiles + ", current RTT EMA: " + rttEstimator.getRTTmsEstimate());
			if (!incoming) {
				paddleTarget = update.getFieldMaxHeight() / 2 - (update.getPaddleHeight() / 2);
			}
			prevUpdate = update;
		} else {

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

			if (!incoming || incoming && update.getBallX() > 50) {
				paddleTarget = round(bpEstimator.nextMySide(update, ballXVelocity, ballYVelocity));
			}

//			if (prevPt != paddleTarget) {
//				log.debug("paddleTarget = {}, targetFarthest = {}", paddleTarget, round(bpEstimator.targetFarthest(update, ballXVelocity, ballYVelocity)));
//				prevPt = paddleTarget;
//			}

//			if (paddleTarget > (update.getFieldMaxHeight() - update.getPaddleHeight())) {
//				log.info("paddleTarget is {}: does paddle bounce from wall?", paddleTarget);
//			}

//			if (incoming && update.getBallX() < 200) {
			if (incoming && update.getBallX() > 50) {  // arbitrary limit after which we don't change our mind
				paddleTarget += (int) bpEstimator.targetFarthest(update, ballXVelocity, ballYVelocity);
			}

			if (paddleTarget < 1) {
				paddleTarget = 0;
			} else if (paddleTarget > (update.getFieldMaxHeight() - 1)) {
				paddleTarget = (update.getFieldMaxHeight() - 1);
			}
//				paddleTarget = ((paddleTarget < 1) : 0.0d ? (paddleTarget > (update.getFieldMaxHeight() - update.getPaddleHeight() - 1) ? (update.getFieldMaxHeight() - update.getPaddleHeight()) : paddleTarget));


			stats.updateStatistics(update, rttEstimator.getRTTmsEstimate());
//		  log.info("receiveTime: {}, game time: {}", update.getReceiveTime(), update.getTime());
//			log.info("{}", stats);
		}

		//return paddleTarget - update.getLeftY() - (update.getPaddleHeight() / 2);
		if (paddleDistanceFromTarget(update) > -DEADZONE_1 && paddleDistanceFromTarget(update) < DEADZONE_1) {
			currentTargetZone = DEADZONE_2;
		} else {
			currentTargetZone = DEADZONE_1;
		}
		log.debug("Chosen currentTargetZone: {}", currentTargetZone);

		ChangeDirMessage cdm = putPaddleToTarget(update, currentTargetZone);

// TODO: the deadZone-stuff is too lax, needs possibly another slower speed fine tune.
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

	private double round(double d) {
		return ((double) Math.round(d * 10.0d)) / 10.0d;
	}

	private double paddleDistanceFromTarget(Update update) {
		return paddleTarget - update.getLeftY() - (update.getPaddleHeight() / 2);
	}

	private ChangeDirMessage putPaddleToTarget(Update update, double deadZone) {
		//		double deadZone = (update.getPaddleHeight() / 2) - 10.0d;
//		double deadZone = targetDistance;
		double speedBase = (deadZone >= DEADZONE_1 ? DZ_1_SPEED : DZ_2_SPEED);
//		double yDiff = paddleTarget - update.getLeftY() - (update.getPaddleHeight() / 2);
		double moveDownAmount = paddleDistanceFromTarget(update);
		ChangeDirMessage cdm = null;
//		log.debug("deadZone: {}, speedBase: {}, yDiff: {}", deadZone, speedBase, yDiff);

		if (moveDownAmount > deadZone && speed != speedBase) {  // if paddle is far below target and speed is not right,
			cdm = new ChangeDirMessage(1.0d * speedBase);         // make paddle go fast towards target
			// (update.getLeftY() - prevUpdate.getLeftY())
			speed = 1.0d * speedBase;
		} else if (moveDownAmount < -deadZone && speed != (-speedBase)) {
			cdm = new ChangeDirMessage(-1.0d * speedBase);        // same for the case where paddle is far higher than target
			speed = -1.0d * speedBase;                            // and not moving at right speed
		} else if (speed != 0.0d && moveDownAmount < deadZone && moveDownAmount > -deadZone) {
			cdm = new ChangeDirMessage(0.0d);                     // ..but if paddle is moving at right speed
			speed = 0.0d;                                         // and is in the zone, stop it (there may be more fine-grained
		}                                                       // zones later)

		if (cdm != null) {
			log.debug("cdm created, speed: {}", speed);
		}

		if ((update.getLeftY() - prevUpdate.getLeftY()) > 0.0d && speed < 0.0d) {  // moving ball, bounce from top wall?
			speed = -speedBase;
			cdm = new ChangeDirMessage(speed);
			log.debug("Wrong dir, changing (1), speed: {}, my Y-pos: {}, ballY: {}", speed, update.getLeftY(), update.getBallY());
		}
		if ((update.getLeftY() - prevUpdate.getLeftY()) < 0.0d && speed > 0.0d) {  // moving ball, bounce from bottom wall?
			speed = speedBase;
			cdm = new ChangeDirMessage(speed);
			log.debug("Wrong dir, changing (2), speed: {}, my Y-pos: {}, ballY: {}", speed, update.getLeftY(), update.getBallY());
		}

		if ((-DZ_2_SPEED <= speed && speed <= DZ_2_SPEED) && speed != 0.0) {
			log.debug("LOWSPEED: ballY: {}, my Y-pos: {}, my Y-diff: {}, speedBase: {}, speed: {}, paddleTarget: {}", update.getBallY(), update.getLeftY(), prevUpdate.getLeftY() - update.getLeftY(), speedBase, speed, paddleTarget);
		}
		return cdm;
	}
}
