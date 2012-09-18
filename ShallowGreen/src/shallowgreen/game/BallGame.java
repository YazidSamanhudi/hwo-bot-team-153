package shallowgreen.game;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

import shallowgreen.Game;
import shallowgreen.Statistics;
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
	double degAngle, prevDegAngle, incomingAngle, outgoingAngle, prevPt;

	@Override
	public void update(Update update) {

		boolean incoming = true;
		double ballXVelocity, ballYVelocity;
		long updateDeltaTime;
		// calculate which way we should be going
		Player myPreviousPosition;
		Player myCurrentPosition = update.getLeft();

		if (firstUpdate) {
			rttEstimator.stop();
			bpEstimator = new BallPosition();
			stats = new Statistics(update);
			missiles = update.getNrOfMissiles();
			log.debug("Missiles: " + missiles + ", current RTT EMA: " + rttEstimator.getRTTmsEstimate());
			if (!incoming) {
				paddleTarget = update.getFieldMaxHeight() / 2 - (update.getPaddleHeight() / 2);
			}
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
			checkAngleChange(ballAngle, ballTravelDistance);
			incoming = ballIsIncoming(ballAngle);

			paddleTarget = round(bpEstimator.testMySide(update, ballXVelocity, ballYVelocity));
			if (prevPt != paddleTarget) {
				log.info("paddleTarget = {}", paddleTarget);
				prevPt = paddleTarget;
			}
			stats.updateStatistics(update, rttEstimator.getRTTmsEstimate());
//		  log.info("receiveTime: {}, game time: {}", update.getReceiveTime(), update.getTime());
//			log.info("{}", stats);
		}

		double deadZone = (update.getPaddleHeight() / 2) - 2.0d;
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
//try {
//	connection.sendMessage(new ChangeDirMessage(1.0d));
//} catch(JsonGenerationException e) {
//	// TODO Auto-generated catch block
//	e.printStackTrace();
//} catch(JsonMappingException e) {
//	// TODO Auto-generated catch block
//	e.printStackTrace();
//} catch(IOException e) {
//	// TODO Auto-generated catch block
//	e.printStackTrace();
//}
	}

	private void checkAngleChange(double ballAngle, double ballTravelDistance) {
		prevDegAngle = degAngle;
		degAngle = ((ballAngle / Math.PI * 180) + (ballAngle > 0 ? 0 : 360) + 90) % 360;
		if (Math.abs(degAngle - prevDegAngle) > 0.01) {  // direction change
//			log.debug("{}", new Object[]{
//								String.format("Speed: %01.3f angle: %06.3f minVel: %01.3f maxVel: %01.3f", ballTravelDistance, degAngle, minVelocity, maxVelocity)
//							});
			// FIXME tähän nyt ne kulmien, laskettujen osumakohtien ja vastustajan/oman mailan sijantien tallennukset
			//       Aina ei ole 'varmaa' tietoa pomppukulmasta jos pallo lähes välittömästi pomppaa seinästä (kun ollaan lähellä kulmaa)
		}
	}

	private double round(double d) {
		return ((double) Math.round(d * 10.0d)) / 10.0d;
	}
}
