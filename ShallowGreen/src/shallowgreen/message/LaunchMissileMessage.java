package shallowgreen.message;

public class LaunchMissileMessage extends Message {

	public LaunchMissileMessage() {
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.LAUNCH_MISSILE;
	}

}
