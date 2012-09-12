package shallowgreen.game;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shallowgreen.Game;
import shallowgreen.message.ChangeDirMessage;
import shallowgreen.model.Player;
import shallowgreen.model.Update;

/**
 * put the paddle where the ball is
 */
public class PetGame extends Game {
	private static final Logger log=LoggerFactory.getLogger(PetGame.class);

	private double speed;
	private double lastPaddleY;
    private double minVelocity = 999,maxVelocity = 0;
    private Update prevUpdate;
    private double paddleTarget = 240; // hardcoded middle FIXME
    private double prevAngle = 0;
    private long messageLimitTick = 0;
    private int messages = 0;
    private final long TICKS = 1000;
    private final int MESSAGES = 10;

	@Override
	public void update(Update update) {
	    double xVel,yVel;
	    long deltaTime;
	    boolean incoming = true;
		// calculate which way we should be going
		Player me=update.getLeft();
		if (prevUpdate != null) {
		    if ((update.getTime() - messageLimitTick) > TICKS)
			{
			    messages = 0;
			    messageLimitTick = update.getTime();
			}
		    Player prevMe = prevUpdate.getLeft();
		    double distance;
		    double angle;
		    double paddleVel;
		    xVel = update.getBallX() - prevUpdate.getBallX();
		    yVel = update.getBallY() - prevUpdate.getBallY();
		    deltaTime = update.getTime() - prevUpdate.getTime();
		    //due to multiplication, always positive
		    distance = Math.sqrt((xVel*xVel)+(yVel*yVel)) / deltaTime;
		    paddleVel = (me.getY() - prevMe.getY()) / deltaTime;
		    angle = Math.atan2(yVel,xVel);
		    System.out.println("Speed:" + distance +
				       ",Angle:" + angle +
				       ",PT:" + paddleTarget +
				       ",PV:" + paddleVel +
				       ",min:" + minVelocity + ",max:" + maxVelocity);
		    if (distance<minVelocity) minVelocity = distance;
		    if (distance>maxVelocity) maxVelocity = distance;
		    if (angle < (Math.PI/2) && angle > (Math.PI/-2))
			incoming = false;
		    else
			incoming = true;
		    if (incoming && prevAngle != angle)
			{
			    int safety = 999999;
			    double simX = update.getBallX();
			    double simY = update.getBallY();
			    while (simX > 0 && safety > 0) {
				/*System.out.println("X:" + simX + ",Y:" + simY + 
				  ",Xv:" + xVel + ",Yv:" + yVel);*/
				simX += xVel;
				simY += yVel;
				if (simY < 0) {
				    yVel *= -1.0d;
				    simY *= -1.0d;
				} else if(simY > update.getFieldMaxHeight()) {
				    yVel *= -1.0d;
				    simY = update.getFieldMaxHeight() - (simY - update.getFieldMaxHeight());
				}
				safety--;
			    }
			    paddleTarget = simY;
			    prevAngle = angle;
			}
		} 
		if (!incoming)
		    paddleTarget = update.getFieldMaxHeight()/2 - (update.getPaddleHeight()/2);
		/*else
		  paddleTarget = update.getBallY();*/

		// safety one pixel
		double deadZone = (update.getPaddleHeight()/2)-1.0d+update.getBallRadius();
		double yDiff=paddleTarget-me.getY()-(update.getPaddleHeight()/2);
		ChangeDirMessage cdm=null;
		if(yDiff>deadZone && speed<=0.0d) {
			cdm=new ChangeDirMessage(1.0d);
			speed=1.0d;
		} else if(yDiff<-deadZone && speed>=0.0d) {
		    cdm=new ChangeDirMessage(-1.0d);
			speed=-1.0d;
		} else if (speed!=0.0d && yDiff<deadZone && yDiff>-deadZone) {
		    cdm=new ChangeDirMessage(0.0d);
			speed=0.0d;
		}


		// check if we're going the wrong way (bounce from the sides)
		/*if(cdm==null) {
			if(speed<0.0d && lastPaddleY<me.getY()) {
				cdm=new ChangeDirMessage(1.0d);
				speed=1.0d;
			} else if(speed>=0.0d && lastPaddleY>me.getY()) {
				cdm=new ChangeDirMessage(-1.0d);
				speed=-1.0d;
			}
			}*/
		lastPaddleY=me.getY();
		prevUpdate = update;

		// send the command, if any
		if(cdm!=null && messages < MESSAGES) {
			try {
				connection.sendMessage(cdm);
				messages++;
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

}
