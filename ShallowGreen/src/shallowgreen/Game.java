package shallowgreen;

import shallowgreen.message.Message;

public abstract class Game {

	protected Connection connection;

	public void setConnection(Connection connection) {
		this.connection=connection;
	}

	public abstract void handleMessage(Message message);

}
