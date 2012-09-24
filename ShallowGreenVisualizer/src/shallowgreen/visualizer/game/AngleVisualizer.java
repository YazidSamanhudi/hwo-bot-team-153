package shallowgreen.visualizer.game;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shallowgreen.Connection;
import shallowgreen.Game;
import shallowgreen.Statistics;
import shallowgreen.message.GameIsOnMessage;
import shallowgreen.message.GameIsOverMessage;
import shallowgreen.message.GameStartedMessage;
import shallowgreen.message.JoinedMessage;
import shallowgreen.message.Message;
import shallowgreen.model.Update;
import shallowgreen.predictor.RTT;
import shallowgreen.visualizer.VisualMessageTool;
import shallowgreen.visualizer.Visualizer;

public class AngleVisualizer extends Game {

	private static final Logger log = LoggerFactory.getLogger(AngleVisualizer.class);
	private Game wrappedGame;
	private Update previousUpdate;

	@SuppressWarnings("unused")
	private AngleVisualizer() {
	}

	public AngleVisualizer(Game wrappedGame) {
		this.wrappedGame = wrappedGame;
	}

	@Override
	public void setConnection(Connection connection) {
		this.connection = connection;
		wrappedGame.setConnection(connection);
	}

	@Override
	public void handleMessage(Message message) {
		switch (message.getMessageType()) {
			case CHANGE_DIR:
				break;
			case ERROR:
				break;
			case GAME_IS_ON:
				update(((GameIsOnMessage) message).getUpdate());
				break;
			case GAME_IS_OVER:
				gameIsOver(((GameIsOverMessage) message).getWinner());
				break;
			case GAME_STARTED:
				gameStarted(((GameStartedMessage) message).getPlayers());
				break;
			case JOIN:
				break;
			case JOINED:
				Visualizer.gameURL = ((JoinedMessage) message).getUrl();
				break;
			case UNKNOWN:
			default:
			// fall through
		}
		wrappedGame.handleMessage(message);
	}

	private static class Point {

		private double x;
		private double y;

		private Point(double x, double y) {
			this.x = x;
			this.y = y;
		}
	}
	private double previousLineSlope;
	private Update enterSameSlopeUpdate1;
	private Update enterSameSlopeUpdate2;
	private int skippedNotSameSlopes;
	private static int nextAngle;

//private static int nextBall;
	@Override
	public void update(Update update) {
		if (previousUpdate == null) {
			// initialize the status with the first Update
			previousUpdate = update;
			return;
		}

		double lineSlope = (previousUpdate.getBallY() - update.getBallY()) / (previousUpdate.getBallX() - update.getBallX());
//log.debug("lineslope: {}",lineSlope);

		if (previousLineSlope == 0.0d) {
			previousLineSlope = lineSlope;
			return;
		}

//log.debug("lineslopes: {} {} {}",previousLineSlope,lineSlope,(previousLineSlope-lineSlope));
//log.debug("lineslopes: {}",(previousLineSlope-lineSlope));

		if (Math.abs(previousLineSlope - lineSlope) < 0.00001d) {
//Visualizer.broadcastMessage(VisualMessageTool.updateMessage("circle","angleBall"+nextBall++,"class","inf"
//	,"cx",update.getBallX()+update.getBallRadius()
//	,"cy",update.getBallY()+update.getBallRadius()
//	,"r","3"
//).toString());
			// the slope is the same as with the previous update
			if (skippedNotSameSlopes == 0) {
				// still looking for the enter updates
				enterSameSlopeUpdate1 = previousUpdate;
				enterSameSlopeUpdate2 = update;
			} else {
				// we're looking for exit same slope and found it
				Point intersection = calculateLineIntersection(
								enterSameSlopeUpdate1.getBallX() + enterSameSlopeUpdate1.getBallRadius(),
								enterSameSlopeUpdate1.getBallY() + enterSameSlopeUpdate1.getBallRadius(),
								enterSameSlopeUpdate2.getBallX() + enterSameSlopeUpdate2.getBallRadius(),
								enterSameSlopeUpdate2.getBallY() + enterSameSlopeUpdate2.getBallRadius(),
								previousUpdate.getBallX() + previousUpdate.getBallRadius(),
								previousUpdate.getBallY() + previousUpdate.getBallRadius(),
								update.getBallX() + update.getBallRadius(),
								update.getBallY() + update.getBallRadius());
				if (intersection != null) {
//					log.debug("Coordinates for edge: x = {}, y = {}", intersection.x, intersection.y);
					Visualizer.broadcastMessage(VisualMessageTool.updateMessage("line","angleIn"+nextAngle,"class","not"
									,"x1",intersection.x
									,"y1",intersection.y
									,"x2",enterSameSlopeUpdate1.getBallX()+enterSameSlopeUpdate1.getBallRadius()
									,"y2",enterSameSlopeUpdate1.getBallY()+enterSameSlopeUpdate1.getBallRadius()).toString());
					Visualizer.broadcastMessage(VisualMessageTool.updateMessage("line","angleOut"+nextAngle,"class","not"
									,"x1",intersection.x
									,"y1",intersection.y
									,"x2",previousUpdate.getBallX()+previousUpdate.getBallRadius()
									,"y2",previousUpdate.getBallY()+previousUpdate.getBallRadius()).toString());
					final boolean isLeft=previousUpdate.getBallX()-update.getBallX()<0.0d;
					final double inAngle=(intersection.y-(enterSameSlopeUpdate1.getBallY()+enterSameSlopeUpdate1.getBallRadius()))/(intersection.x-(enterSameSlopeUpdate1.getBallX()+enterSameSlopeUpdate1.getBallRadius()));
					final double outAngle=(intersection.y-(previousUpdate.getBallY()+previousUpdate.getBallRadius()))/(intersection.x-(previousUpdate.getBallX()+previousUpdate.getBallRadius()));
					double paddleHit = intersection.y - (isLeft ? previousUpdate.getLeftY() : previousUpdate.getRightY());
					double paddleSpeedTicks = 0;
					double paddleSpeedReceived = 0;
					
					if (isLeft) {
						paddleSpeedTicks = (update.getLeftY() - previousUpdate.getLeftY()) / (update.getTime() - previousUpdate.getTime());
						paddleSpeedReceived = (update.getLeftY() - previousUpdate.getLeftY()) / (update.getReceiveTimeMillis() - previousUpdate.getReceiveTimeMillis());
					} else {
						paddleSpeedTicks = (update.getRightY() - previousUpdate.getRightY()) / (update.getTime() - previousUpdate.getTime());
						paddleSpeedReceived = (update.getRightY() - previousUpdate.getRightY()) / (update.getReceiveTimeMillis() - previousUpdate.getReceiveTimeMillis());
					}

					if(0.0d < paddleHit && paddleHit <= previousUpdate.getPaddleHeight()) {
						log.info("in, out, paddleHit, isLeft, paddleSpeed (px/time, px/realtime): {}\t{}\t{}\t{}\t{}\t{}",
										inAngle,
										outAngle,
										paddleHit,
										isLeft,
										paddleSpeedTicks,
										paddleSpeedReceived);
					}
					// housekeeping for the visualization graphics, only keep last 10
					if (++nextAngle >= 10) {
						nextAngle = 0;
					}
				} else {
					log.debug("intersection: {}", intersection);
				}
				skippedNotSameSlopes = 0;
				enterSameSlopeUpdate1 = previousUpdate;
				enterSameSlopeUpdate2 = update;
			}
		} else {
			// the slope changed -> we have bounced
			// next we'll need to find two adjacent slopes for the exit angle
			// but only if we already have found same slope
			if (enterSameSlopeUpdate1 != null) {
				if (skippedNotSameSlopes >= 2) {
					// too many non-same angles, we can't determine the angle (maybe two bounces close to each other), so lets just skip entirely
					skippedNotSameSlopes = 0;
					enterSameSlopeUpdate1 = null;
					enterSameSlopeUpdate2 = null;
				} else {
					skippedNotSameSlopes++;
				}
			}
		}


		previousLineSlope = lineSlope;
		previousUpdate = update;
	}

	private Point calculateLineIntersection(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
		double x12 = x1 - x2;
		double y12 = y1 - y2;
		double x34 = x3 - x4;
		double y34 = y3 - y4;

		double d = x12 * y34 - y12 * x34;

		if (Math.abs(d) < 0.1d) {
			return null;
		} else {
			double a = x1 * y2 - y1 * x2;
			double b = x3 * y4 - y3 * x4;

			double x = (a * x34 - b * x12) / d;
			double y = (a * y34 - b * y12) / d;

			return new Point(x, y);
		}
	}

	@Override
	public void gameIsOver(String winner) {
		// unused - see handleMessage
	}

	@Override
	public void gameStarted(List<String> players) {
		// unused - see handleMessage
	}

	@Override
	public void setRTTEstimator(RTT rttEstimator) {
		wrappedGame.setRTTEstimator(rttEstimator);
	}

	@Override
	public Statistics getStatistics() {
		return wrappedGame.getStatistics();
	}
}
