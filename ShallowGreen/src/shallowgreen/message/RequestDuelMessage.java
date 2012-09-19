package shallowgreen.message;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestDuelMessage extends Message {
	@JsonProperty("data")
	private String[] names;

	public RequestDuelMessage() {
	}

	public RequestDuelMessage(String[] names) {
		this.names=names;
	}

	public RequestDuelMessage(String myName, String opponentName) {
		names=new String[] { myName,opponentName };
	}

	public String[] getNames() {
		return names;
	}

	public void setNames(String[] names) {
		this.names=names;
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.REQUEST_DUEL;
	}

	/** shortcut/convenience to getNames()[0] */
	public String getMyName() {
		if(names==null)
			return null;
		return names[0];
	}

	/** shortcut/convenience to getNames()[0]=myName */
	public void setMyName(String myName) {
		if(names==null)
			names=new String[2];
		names[0]=myName;
	}

	/** shortcut/convenience to getNames()[1] */
	public String getOpponentName() {
		if(names==null)
			return null;
		return names[1];
	}

	/** shortcut/convenience to getNames()[1]=myName */
	public void setOpponentName(String opponentName) {
		if(names==null)
			names=new String[2];
		names[1]=opponentName;
	}

}
