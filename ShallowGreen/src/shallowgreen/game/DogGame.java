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

/**
 * Runs after the ball Y mindlessly, with full speed. And doesn't know what to
 * do with the ball if it ever catches it.
 */
public class DogGame extends Game {
	private static final Logger log=LoggerFactory.getLogger(DogGame.class);

	private double speed;
	private double lastPaddleY;

	@Override
	public void update(Update update) {
		// calculate which way we should be going
		Player me=update.getLeft();
		double yDiff=(update.getBallY()+update.getBallRadius())-(me.getY()+update.getPaddleHeight()/2);
		ChangeDirMessage cdm=null;
		if(yDiff>0.1d && speed<=0.0d) {
			cdm=new ChangeDirMessage(1.0d);
			speed=1.0d;
		} else if(yDiff<-0.1d && speed>=0.0d) {
			cdm=new ChangeDirMessage(-1.0d);
			speed=-1.0d;
		}

		// check if we're going the wrong way (bounce from the sides)
		if(cdm==null) {
			if(speed<0.0d && lastPaddleY<me.getY()) {
				cdm=new ChangeDirMessage(1.0d);
				speed=1.0d;
			} else if(speed>=0.0d && lastPaddleY>me.getY()) {
				cdm=new ChangeDirMessage(-1.0d);
				speed=-1.0d;
			}
		}
		lastPaddleY=me.getY();

		// send the command, if any
		if(cdm!=null) {
			try {
				connection.sendMessage(cdm);
			} catch(IOException e) {
				// TODO Auto-generated catch block
				log.error("Whooooops.",e);
			}
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

	@Override
	public void setRTTEstimator(RTT rttEstimator) {
	}

	@Override
	public int getPoints() {
		return 0;
	}

}
