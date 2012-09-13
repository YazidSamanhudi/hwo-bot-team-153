package shallowgreen.visualizer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shallowgreen.Connection;
import shallowgreen.GameFactory;
import shallowgreen.ShallowGreen;
import shallowgreen.visualizer.servlet.OverlayServlet;
import shallowgreen.visualizer.servlet.UpdateServlet;

public class Visualizer extends ShallowGreen {
	private static final Logger log=LoggerFactory.getLogger(Visualizer.class);

	public static String gameURL;
	public static final Set<WebSocketClient> webSocketClients=new CopyOnWriteArraySet<WebSocketClient>();

	public static void main(String[] args) {
		Visualizer visualizer=new Visualizer();
		visualizer.mainParser(args);
	}

	@Override
	protected Connection newConnection(String name, InetSocketAddress address, GameFactory gameFactory) {
		// initialize the visualizer before returning the Connection
		Server server=new Server(12765);

		ServletHandler updateServletHandler=new ServletHandler();
		updateServletHandler.addServletWithMapping(UpdateServlet.class,"/visualize/update");

		ServletHandler overlayServletHandler=new ServletHandler();
		overlayServletHandler.addServletWithMapping(OverlayServlet.class,"/visualize");

		HandlerList handlers=new HandlerList();
		handlers.setHandlers(new Handler[] { updateServletHandler, overlayServletHandler, new DefaultHandler() });
		server.setHandler(handlers);

		try {
			server.start();
			log.info("Visualizer can be found at: http://localhost:12765/visualize/");
		} catch(Exception e) {
			log.error("Failed to initialize the visualization server",e);
		}

		return new Connection(name,address,new VisualizerGameFactory(gameFactory));
	}

	public static void broadcastMessage(String message) {
		for(WebSocketClient wsc: webSocketClients) {
			try {
				wsc.sendMessage(message);
			} catch(IOException e) {
				log.info("Closing WebSocket connection {}",wsc);
				wsc.close();
			}
		}
	}

}
