package shallowgreen.message;

import shallowgreen.model.Update;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GameIsOnMessage extends Message {
	@JsonProperty("data")
	private Update update;

	public Update getUpdate() {
		return update;
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.GAME_IS_ON;
	}

}
