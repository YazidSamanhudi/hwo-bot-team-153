package shallowgreen.message;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GameIsOverMessage extends Message {
	@JsonProperty("data")
	private String winner;

	public String getWinner() {
		return winner;
	}

	public void setWinner(String winner) {
		this.winner=winner;
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.GAME_IS_OVER;
	}

}
