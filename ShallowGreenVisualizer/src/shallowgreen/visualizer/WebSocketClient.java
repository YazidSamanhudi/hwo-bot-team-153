package shallowgreen.visualizer;

import java.io.IOException;

import org.eclipse.jetty.websocket.WebSocket;

public class WebSocketClient implements WebSocket.OnTextMessage {

	private volatile Connection connection;

	@Override
	public void onOpen(Connection connection) {
		this.connection=connection;
		Visualizer.webSocketClients.add(this);
	}

	@Override
	public void onClose(int closeCode, String message) {
		Visualizer.webSocketClients.remove(this);
	}

	@Override
	public void onMessage(String data) {
		// we don't respond to messages from the client
	}

	public void sendMessage(String message) throws IOException {
		connection.sendMessage(message);
	}

	public void close() {
		connection.close();
	}

}
