package shallowgreen.message;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MissileReadyMessage extends Message {
	@JsonProperty("data")
	private long time;

	public MissileReadyMessage() {
	}

	public MissileReadyMessage(long time) {
		this.time=time;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time=time;
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.MISSILE_READY;
	}

}
