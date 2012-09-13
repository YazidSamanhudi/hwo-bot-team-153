package shallowgreen.visualizer.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketFactory;

import shallowgreen.visualizer.WebSocketClient;

public class UpdateServlet extends HttpServlet {
	private static final long serialVersionUID=2435500928824686797L;

	private WebSocketFactory webSocketFactory;

	@Override
	public void init() throws ServletException {
		webSocketFactory=new WebSocketFactory(new WebSocketFactoryAcceptor());
		webSocketFactory.setMaxIdleTime(10000);
		webSocketFactory.setBufferSize(10240);
	}

	@Override
		protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if(!webSocketFactory.acceptWebSocket(request,response))
			response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,"Only WebSockets supported");
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		doGet(request,response);
	}

	private class WebSocketFactoryAcceptor implements WebSocketFactory.Acceptor {
		@Override
		public boolean checkOrigin(HttpServletRequest request, String origin) {
			return true;
		}

		@Override
		public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
			if("ShallowGreenVisualizer".equals(protocol))
				return new WebSocketClient();
			return null;
		}
	}

}
