package shallowgreen.message;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorMessage extends Message {
	@JsonProperty("data")
	private String message;

	public String getMessage() {
		return message;
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.ERROR;
	}

	@Override
	public String toString() {
		return super.toString()+"["+message+"]";
	}

}
