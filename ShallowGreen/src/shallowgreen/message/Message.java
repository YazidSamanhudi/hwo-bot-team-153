package shallowgreen.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
	use=JsonTypeInfo.Id.NAME,
	include=JsonTypeInfo.As.PROPERTY,
	property="msgType")
@JsonSubTypes({
	@JsonSubTypes.Type(value=JoinMessage.class, name="join"),
	@JsonSubTypes.Type(value=RequestDuelMessage.class, name="requestDuel"),
	@JsonSubTypes.Type(value=JoinedMessage.class, name="joined"),
	@JsonSubTypes.Type(value=GameStartedMessage.class, name="gameStarted"),
	@JsonSubTypes.Type(value=GameIsOverMessage.class, name="gameIsOver"),
	@JsonSubTypes.Type(value=GameIsOnMessage.class, name="gameIsOn"),
	@JsonSubTypes.Type(value=ChangeDirMessage.class, name="changeDir"),
	@JsonSubTypes.Type(value=ErrorMessage.class, name="error"),
	@JsonSubTypes.Type(value=MissileReadyMessage.class, name="missileReady"),
	@JsonSubTypes.Type(value=LaunchMissileMessage.class, name="launchMissile"),
	@JsonSubTypes.Type(value=MissileLaunchedMessage.class, name="missileLaunched")
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
		,MISSILE_READY
		,LAUNCH_MISSILE
		,MISSILE_LAUNCHED
	}

	@JsonIgnore
	public abstract MessageType getMessageType();

}
