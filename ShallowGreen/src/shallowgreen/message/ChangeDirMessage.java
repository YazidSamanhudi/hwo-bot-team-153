package shallowgreen.message;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChangeDirMessage extends Message {
	@JsonProperty("data")
	private double speed;

	public ChangeDirMessage() {
	}

	public ChangeDirMessage(double speed) {
		this.speed=speed;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed=speed;
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.CHANGE_DIR;
	}

}
