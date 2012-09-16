package shallowgreen.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Update {

	private long receiveTime=System.currentTimeMillis();
	private long time;
	private Player left;
	private Player right;
	private Ball ball;
	private double nrOfMissiles;
	@JsonProperty("conf")
	private Field field;

	public long getReceiveTime() {
		return receiveTime;
	}

	public long getTime() {
		return time;
	}

	public Player getLeft() {
		return left;
	}

	public Player getRight() {
		return right;
	}

	public Ball getBall() {
		return ball;
	}

	public double getNrOfMissiles() {
		return nrOfMissiles;
	}

	public Field getField() {
		return field;
	}

	/** shortcut/convenience to getLeft().getY() */
	public double getLeftY() {
		return left.getY();
	}

	/** shortcut/convenience to getLeft().getPlayerName() */
	public String getLeftPlayerName() {
		return left.getPlayerName();
	}

	/** shortcut/convenience to getRight().getY() */
	public double getRightY() {
		return right.getY();
	}

	/** shortcut/convenience to getRight().getPlayerName() */
	public String getRightPlayerName() {
		return right.getPlayerName();
	}

	/** shortcut/convenience to getPosition().getBallX() */
	public double getBallX() {
		return ball.getPosition().getX();
	}

	/** shortcut/convenience to getPosition().getBallY() */
	public double getBallY() {
		return ball.getPosition().getY();
	}

	/** shortcut/convenience to getField().getFieldMaxWidth() */
	public double getFieldMaxWidth() {
		return field.getMaxWidth();
	}

	/** shortcut/convenience to getField().getFieldMaxHeight() */
	public double getFieldMaxHeight() {
		return field.getMaxHeight();
	}

	/** shortcut/convenience to getField().getPaddleHeight() */
	public double getPaddleHeight() {
		return field.getPaddleHeight();
	}

	/** shortcut/convenience to getField().getPaddleWidth() */
	public double getPaddleWidth() {
		return field.getPaddleWidth();
	}

	/** shortcut/convenience to getField().getBallRadius() */
	public double getBallRadius() {
		return field.getBallRadius();
	}

	/** shortcut/convenience to getField().getTickInterval() */
	public double getTickInterval() {
		return field.getTickInterval();
	}

}

/*
{
	"msgType":"gameIsOn",
	"data":{
		"time":1347324173910,
		"left":{
			"y":240.99999999999994,
			"playerName":"foobarista"
		},
		"right":{
			"y":240.0,
			"playerName":"becker"
		},
		"ball":{
			"pos":{
				"x":82.30162336056428,
				"y":157.7908197890153
			}
		},
		"nrOfMissiles":0,
		"conf":{
			"maxWidth":640,
			"maxHeight":480,
			"paddleHeight":50,
			"paddleWidth":10,
			"ballRadius":5,
			"tickInterval":30
		}
	}
}
*/
