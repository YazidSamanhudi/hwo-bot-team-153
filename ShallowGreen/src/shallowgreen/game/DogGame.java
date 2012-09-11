package shallowgreen.game;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shallowgreen.Game;
import shallowgreen.message.ChangeDirMessage;
import shallowgreen.model.Player;
import shallowgreen.model.Update;

public class DogGame extends Game {
	private static final Logger log=LoggerFactory.getLogger(DogGame.class);

	private double speed;

	@Override
	public void update(Update update) {
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

	@Override
	public void gameIsOver(String winner) {
		log.info("winner: {}",winner);
	}

	@Override
	public void gameStarted(List<String> players) {
		log.info("new game with players: {}",players);
	}

}
