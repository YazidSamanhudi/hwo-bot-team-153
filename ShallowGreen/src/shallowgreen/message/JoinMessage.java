package shallowgreen.message;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JoinMessage extends Message {
	@JsonProperty("data")
	private String name;

	public JoinMessage() {
	}

	public JoinMessage(String name) {
		this.name=name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name=name;
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.JOIN;
	}

}
