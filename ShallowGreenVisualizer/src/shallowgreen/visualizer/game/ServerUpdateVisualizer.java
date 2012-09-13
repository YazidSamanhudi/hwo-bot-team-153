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

public class ServerUpdateVisualizer extends Game {
	private static final Logger log=LoggerFactory.getLogger(ServerUpdateVisualizer.class);

	private Game wrappedGame;
	private Update previousUpdate;
	private static String[] ballPath=new String[70];
	private static int ballPathIndex=0;

	@SuppressWarnings("unused")
	private ServerUpdateVisualizer() { }

	public ServerUpdateVisualizer(Game wrappedGame) {
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

	@Override
	public void update(Update update) {
		// ball
		Visualizer.broadcastMessage(VisualMessageTool.updateMessage("rect","ball","class","srvu"
						,"x",update.getBallX()
						,"y",update.getBallY()
						,"width",(update.getBallRadius()*2)
						,"height",(update.getBallRadius()*2)
		).toString());

		// left paddle
		Visualizer.broadcastMessage(VisualMessageTool.removeMessage("left").toString());
		Visualizer.broadcastMessage(VisualMessageTool.updateMessage("rect","left","class","srvu"
						,"x","0"
						,"y",update.getLeftY()
						,"width",update.getPaddleWidth()
						,"height",update.getPaddleHeight()
		).toString());


		// right paddle
		Visualizer.broadcastMessage(VisualMessageTool.removeMessage("right").toString());
		Visualizer.broadcastMessage(VisualMessageTool.updateMessage("rect","right","class","srvu"
						,"x",(update.getFieldMaxWidth()-update.getPaddleWidth())
						,"y",update.getRightY()
						,"width",update.getPaddleWidth()
						,"height",update.getPaddleHeight()
		).toString());

		// ball trail
		if(previousUpdate!=null) {
			// clean out trail
			if(ballPath[ballPathIndex]!=null) {
				Visualizer.broadcastMessage(VisualMessageTool.removeMessage(ballPath[ballPathIndex]).toString());
				Visualizer.broadcastMessage(VisualMessageTool.removeMessage(ballPath[ballPathIndex]+"Loc").toString());
			}
			// trail from previous update
			ballPath[ballPathIndex]="ballPath"+ballPathIndex;
			Visualizer.broadcastMessage(VisualMessageTool.updateMessage("line",ballPath[ballPathIndex],"class","srvutr"
							,"x1",previousUpdate.getBallX()+previousUpdate.getBallRadius()
							,"y1",previousUpdate.getBallY()+previousUpdate.getBallRadius()
							,"x2",update.getBallX()+update.getBallRadius()
							,"y2",update.getBallY()+update.getBallRadius()
			).toString());

			// previous update location trail
			Visualizer.broadcastMessage(VisualMessageTool.updateMessage("rect",ballPath[ballPathIndex]+"Loc","class","srvuh"
							,"x",previousUpdate.getBallX()
							,"y",previousUpdate.getBallY()
							,"width",previousUpdate.getBallRadius()*2
							,"height",previousUpdate.getBallRadius()*2
			).toString());

			if(++ballPathIndex>=ballPath.length)
				ballPathIndex=0;
		}

		previousUpdate=update;
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
	public int getPoints() {
		return wrappedGame.getPoints();
	}

}
