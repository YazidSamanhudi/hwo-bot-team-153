package shallowgreen.game;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shallowgreen.Game;
import shallowgreen.message.ChangeDirMessage;
import shallowgreen.message.GameIsOnMessage;
import shallowgreen.message.GameIsOverMessage;
import shallowgreen.message.JoinedMessage;
import shallowgreen.message.Message;
import shallowgreen.model.Player;
import shallowgreen.model.Update;

public class DogGame extends Game {
	private static final Logger log=LoggerFactory.getLogger(DogGame.class);

	private double speed;

	@Override
	public void handleMessage(Message message) {
		switch(message.getMessageType()) {
			case CHANGE_DIR:
				log.error("Server should not send change dir messages ({})",message);
				break;
			case ERROR:
				log.error("We did something wrong: {}",message);
				break;
			case GAME_IS_ON:
				update(((GameIsOnMessage)message).getUpdate());
				break;
			case GAME_IS_OVER:
				log.info("Game over, {} won",((GameIsOverMessage)message).getWinner());
				break;
			case GAME_STARTED:
				log.info("game started: {}",message);
				break;
			case JOIN:
				log.error("Server should not send join messages ({})",message);
				break;
			case JOINED:
				log.info("joined game, url: {}",((JoinedMessage)message).getUrl());
				break;
			case UNKNOWN:
			default:
				log.error("Unknown message type: {}",message);
				break;
			
		}
	}

	private void update(Update update) {
		Player me=update.getLeft();
		double yDiff=(update.getBallY()+update.getBallRadius()/2)-(me.getY()+update.getPaddleHeight()/2);
		ChangeDirMessage cdm=null;
		if(yDiff>0.1d && speed<=0.0d) {
			cdm=new ChangeDirMessage(1.0d);
			speed=1.0d;
		} else if(yDiff<-0.1d && speed>=0.0d) {
			cdm=new ChangeDirMessage(-1.0d);
			speed=-1.0d;
		}
		if(cdm!=null)
			try {
				connection.sendMessage(cdm);
			} catch(IOException e) {
				// TODO Auto-generated catch block
				log.error("Whooooops.",e);
			}
	}

}
