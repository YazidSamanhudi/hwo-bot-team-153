package shallowgreen;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shallowgreen.message.ChangeDirMessage;
import shallowgreen.message.ErrorMessage;
import shallowgreen.message.GameIsOnMessage;
import shallowgreen.message.GameIsOverMessage;
import shallowgreen.message.GameStartedMessage;
import shallowgreen.message.JoinMessage;
import shallowgreen.message.JoinedMessage;
import shallowgreen.message.Message;
import shallowgreen.model.Update;
import shallowgreen.predictor.RTT;

public abstract class Game {
	private static final Logger log=LoggerFactory.getLogger(Game.class);

	protected Connection connection;

	public void setConnection(Connection connection) {
		this.connection=connection;
	}

//	public abstract void handleMessage(Message message);

	public void handleMessage(Message message) {
		switch(message.getMessageType()) {
			case CHANGE_DIR:
				changeDir((ChangeDirMessage)message);
				break;
			case ERROR:
				error(((ErrorMessage)message).getMessage());
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
				join((JoinMessage)message);
				break;
			case JOINED:
				joined((JoinedMessage)message);
				break;
			case UNKNOWN:
			default:
				unknown(message);
				// fall through
		}
	}

	public void changeDir(ChangeDirMessage message) {
		log.error("Server should not send change dir messages ({})",message);
	}

	public void error(String message) {
		log.error("We did something wrong: {}",message);
	}

	public void join(JoinMessage message) {
		log.error("Server should not send join messages ({})",message);
	}

	public void joined(JoinedMessage message) {
		log.info("joined game, url: {}",message.getUrl());
	}

	public void unknown(Message message) {
		log.error("Unknown message type: {}",message);
	}

	public abstract void update(Update update);
	public abstract void gameIsOver(String winner);
	public abstract void gameStarted(List<String> players);
	public abstract void setRTTEstimator(RTT rttEstimator);

	public Statistics getStatistics() {
		return new Statistics();
	}

}
