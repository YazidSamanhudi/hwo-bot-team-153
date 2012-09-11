package shallowgreen.message;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GameStartedMessage extends Message {
	@JsonProperty("data")
	private List<String> players=new ArrayList<String>();

	public List<String> getPlayers() {
		return players;
	}

	public void setPlayers(List<String> players) {
		this.players=players;
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.GAME_STARTED;
	}

}
