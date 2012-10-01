package shallowgreen.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Ball {

	@JsonProperty("pos")
	private Position position;

	public Ball() {
	}

	public Ball(Position position) {
		this.position=position;
	}

	public Position getPosition() {
		return position;
	}

	// shortcut
	public double getX() {
		return position.getX();
	}

	// shortcut
	public double getY() {
		return position.getY();
	}

}
