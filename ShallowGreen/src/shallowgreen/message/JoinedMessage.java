package shallowgreen.message;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JoinedMessage extends Message {
	@JsonProperty("data")
	private String url;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url=url;
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.JOINED;
	}

}
