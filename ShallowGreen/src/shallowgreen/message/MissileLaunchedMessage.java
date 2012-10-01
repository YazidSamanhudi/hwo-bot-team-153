package shallowgreen.message;

import shallowgreen.model.MissileLaunch;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MissileLaunchedMessage extends Message {
	@JsonProperty("data")
	private MissileLaunch missileLaunch;

	public MissileLaunchedMessage() {
	}

	public MissileLaunchedMessage(MissileLaunch missileLaunch) {
		this.missileLaunch=missileLaunch;
	}

	public MissileLaunch getMissileLaunch() {
		return missileLaunch;
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.MISSILE_LAUNCHED;
	}

}
