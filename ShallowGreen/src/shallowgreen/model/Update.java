package shallowgreen.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Update {

	private long time;
	private Player left;
	private Player right;
	private Ball ball;
	private double nrOfMissiles;
	@JsonProperty("conf")
	private Field field;

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

	// shortcut
	public double getLeftY() {
		return left.getY();
	}

	// shortcut
	public String getLeftPlayerName() {
		return left.getPlayerName();
	}

	// shortcut
	public double getRightY() {
		return right.getY();
	}

	// shortcut
	public String getRightPlayerName() {
		return right.getPlayerName();
	}

	// shortcut
	public double getBallX() {
		return ball.getPosition().getX();
	}

	// shortcut
	public double getBallY() {
		return ball.getPosition().getY();
	}

	// shortcut
	public double getFieldMaxWidth() {
		return field.getMaxWidth();
	}

	// shortcut
	public double getFieldMaxHeight() {
		return field.getMaxHeight();
	}

	// shortcut
	public double getPaddleHeight() {
		return field.getPaddleHeight();
	}

	// shortcut
	public double getPaddleWidth() {
		return field.getPaddleWidth();
	}

	// shortcut
	public double getBallRadius() {
		return field.getBallRadius();
	}

	// shortcut
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
