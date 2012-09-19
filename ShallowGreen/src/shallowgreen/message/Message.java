package shallowgreen.message;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

@JsonTypeInfo(
	use=JsonTypeInfo.Id.NAME,
	include=JsonTypeInfo.As.PROPERTY,
	property="msgType")
@JsonSubTypes({
	@Type(value=JoinMessage.class, name="join"),
	@Type(value=RequestDuelMessage.class, name="requestDuel"),
	@Type(value=JoinedMessage.class, name="joined"),
	@Type(value=GameStartedMessage.class, name="gameStarted"),
	@Type(value=GameIsOverMessage.class, name="gameIsOver"),
	@Type(value=GameIsOnMessage.class, name="gameIsOn"),
	@Type(value=ChangeDirMessage.class, name="changeDir"),
	@Type(value=ErrorMessage.class, name="error")
})
public abstract class Message {

	public static enum MessageType {
		UNKNOWN
		,JOIN
		,REQUEST_DUEL
		,JOINED
		,GAME_STARTED
		,GAME_IS_OVER
		,GAME_IS_ON
		,CHANGE_DIR
		,ERROR
	}

	@JsonIgnore
	public abstract MessageType getMessageType();

}
