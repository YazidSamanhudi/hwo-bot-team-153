package shallowgreen.visualizer.game;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shallowgreen.Connection;
import shallowgreen.Game;
import shallowgreen.message.GameIsOnMessage;
import shallowgreen.message.GameIsOverMessage;
import shallowgreen.message.GameStartedMessage;
import shallowgreen.message.JoinedMessage;
import shallowgreen.message.Message;
import shallowgreen.model.Update;
import shallowgreen.predictor.RTT;
import shallowgreen.visualizer.VisualMessageTool;
import shallowgreen.visualizer.Visualizer;

public class FuturePathPredictionVisualizer extends Game {
	@SuppressWarnings("unused")
	private static final Logger log=LoggerFactory.getLogger(FuturePathPredictionVisualizer.class);

	private Game wrappedGame;
	private Update previousUpdate;

	@SuppressWarnings("unused")
	private FuturePathPredictionVisualizer() { }

	public FuturePathPredictionVisualizer(Game wrappedGame) {
		this.wrappedGame=wrappedGame;
	}

	@Override
	public void setConnection(Connection connection) {
		this.connection=connection;
		wrappedGame.setConnection(connection);
	}

	@Override
	public void handleMessage(Message message) {
		switch(message.getMessageType()) {
			case CHANGE_DIR:
				break;
			case ERROR:
				break;
			case GAME_IS_ON:
				update(((GameIsOnMessage)message).getUpdate());
				break;
			case GAME_IS_OVER:
				gameIsOver(((GameIsOverMessage)message).getWinner());
				break;
			case GAME_STARTED:
				gameStarted(((GameStartedMessage)message).getPlayers());
				break;
			case JOIN:
				break;
			case JOINED:
				Visualizer.gameURL=((JoinedMessage)message).getUrl();
				break;
			case UNKNOWN:
			default:
				// fall through
		}
		wrappedGame.handleMessage(message);
	}

	private double top;
	private double bottom;
	private double left;
	private double right;

	private static class BallStatus {
		private double x;
		private double y;
		private double xVel;
		private double yVel;
	}

	@Override
	public void update(Update update) {
		if(previousUpdate==null) {
			// initialize the status with the first Update
			previousUpdate=update;
			// top seems to bounce too early
			top=update.getBallRadius()*2;
			// official visualizer is 479px high which is 1px less than this.
			bottom=update.getFieldMaxHeight();
			left=update.getPaddleWidth();
			right=update.getFieldMaxWidth()-update.getPaddleWidth();
			return;
		}

		// top and bottom can be calibrated runtime
		if(top>update.getBallY()+update.getBallRadius())
			top=update.getBallY()+update.getBallRadius();
		if(bottom<update.getBallY()+update.getBallRadius())
			bottom=update.getBallY()+update.getBallRadius();
		// left and right cannot be calibrated - even losing balls affect the numbers
		//if(left>update.getBallX()+update.getBallRadius())
		//	left=update.getBallX()+update.getBallRadius();
		//if(right<update.getBallX()+update.getBallRadius())
		//	right=update.getBallX()+update.getBallRadius();

		BallStatus now=new BallStatus();
		now.x=update.getBallX()+update.getBallRadius();
		now.y=update.getBallY()+update.getBallRadius();
		now.xVel=update.getBallX()-previousUpdate.getBallX();
		now.yVel=update.getBallY()-previousUpdate.getBallY();

		for(int i=0; i<7; i++) {
			BallStatus next=calculateNext(now);
			Visualizer.broadcastMessage(VisualMessageTool.updateMessage("line","future"+i,"class","pretr"
							,"x1",now.x
							,"y1",now.y
							,"x2",next.x
							,"y2",next.y
			).toString());
			now=next;
		}

		previousUpdate=update;
	}

	private BallStatus calculateNext(BallStatus from) {
		BallStatus to=new BallStatus();

		double distanceToX;
		if(from.xVel<0.0d) {
                distanceToX=(from.x-left);
            }
		else {
                distanceToX=(right-from.x);
            }

		double distanceToY;
		if(from.yVel<0.0d) {
                distanceToY=(from.y-top);
            }
		else {
                distanceToY=(bottom-from.y);
            }

		// which one is closer, paddle or a wall
		if((distanceToX/Math.abs(from.xVel))<(distanceToY/Math.abs(from.yVel))) {
			// we are hitting a paddle
			double yTravel=(distanceToX/Math.abs(from.xVel))*Math.abs(from.yVel);
			to.x=from.xVel<0.0d ? from.x-distanceToX : from.x+distanceToX;
			to.y=from.yVel<0.0d ? from.y-yTravel : from.y+yTravel;
			to.xVel=from.xVel*-1.0d;
			to.yVel=from.yVel;
		} else {
			// we are hitting a wall
			double xTravel=(distanceToY/Math.abs(from.yVel))*Math.abs(from.xVel);
			to.x=from.xVel<0.0d ? from.x-xTravel : from.x+xTravel;
			to.y=from.yVel<0.0d ? from.y-distanceToY : from.y+distanceToY;
			to.xVel=from.xVel;
			to.yVel=from.yVel*-1.0d;
		}

		return to;
	}

	@Override
	public void gameIsOver(String winner) {
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
