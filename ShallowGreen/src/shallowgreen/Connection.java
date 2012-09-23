package shallowgreen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shallowgreen.message.JoinMessage;
import shallowgreen.message.Message;
import shallowgreen.message.RequestDuelMessage;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import shallowgreen.predictor.RTT;

public class Connection implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(Connection.class);
	private static final ObjectMapper objectMapper = new ObjectMapper();

	static {
		objectMapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(Visibility.ANY));
	}
	private String name;
	private InetSocketAddress address;
	private BufferedWriter bw;
	private BufferedReader br;
	private RTT rttEstimator;
	private int gamesPlayed = 0;
	private int gamesWon = 0;
	private GameFactory gameFactory;
	private Statistics stats;
	private String duelistName;

	@SuppressWarnings("unused")
	private Connection() {
	}

	public Connection(String name, InetSocketAddress address, GameFactory gameFactory) {
		this.name = name;
		this.address = address;
		this.gameFactory = gameFactory;
		this.stats = new Statistics();
		this.rttEstimator = new RTT(address);
		new Thread(rttEstimator).start();
		rttEstimator.stop();
	}

	public Connection(String name, InetSocketAddress address, GameFactory gameFactory, String duelistName) {
		this(name,address,gameFactory);
		this.duelistName=duelistName;
	}

	public void run() {
		// TODO: beautiful Channel-based IO?
		try (Socket socket = new Socket()) {
			try {
				log.debug("Connect to {}", address);
				socket.setTcpNoDelay(true);
				socket.connect(address);
				bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				Game game = null;

				// tell the server we are here
				if(duelistName!=null) {
					RequestDuelMessage requestDuelMessage=new RequestDuelMessage(name,duelistName);
					sendMessage(requestDuelMessage);
				} else {
					JoinMessage joinMessage = new JoinMessage(name);
					sendMessage(joinMessage);
				}

				Message message;
				while ((message = readMessage()) != null) {
					if (message.getMessageType() == Message.MessageType.GAME_STARTED) {
						game = gameFactory.newGame();
						game.setConnection(this);
						game.setRTTEstimator(rttEstimator);
					}
					if (game != null) {
						game.handleMessage(message);
					}
					if (message.getMessageType() == Message.MessageType.GAME_IS_OVER) {
						if (game.getStatistics().getGamesWon() == 1) { stats.gameWon(); } else { stats.gameLost(); }
						gamesPlayed = stats.getGamesPlayed();
						gamesWon = stats.getGamesWon();
						stats.setMaxX(Math.max(stats.getMaxX(), game.getStatistics().getMaxX()));
						stats.setMinX(Math.min(stats.getMinX(), game.getStatistics().getMinX()));
						stats.setMaxY(Math.max(stats.getMaxY(), game.getStatistics().getMaxY()));
						stats.setMinY(Math.min(stats.getMinY(), game.getStatistics().getMinY()));
						
						log.info("Stats for this bot: games played: {}, won {}, losses {}, X: ({}, {}), Y: ({}, {})", new Object[]{gamesPlayed, gamesWon, (gamesPlayed - gamesWon), stats.getMinX(), stats.getMaxX(), stats.getMinY(), stats.getMaxY()});
						game = null;
					}
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.error("devfail", e);
			}
		} catch (IOException e) {
			log.error("Socket close failed", e);
		}
		log.debug("Done.");
	}

	public void sendMessage(Message message) throws JsonGenerationException, JsonMappingException, IOException {
		// TODO: ratelimit
		String s = objectMapper.writerWithType(message.getClass()).writeValueAsString(message);
//		log.debug(">*{}",s);
		write(s);
	}

	private Message readMessage() throws IOException {
		String s = read();
		if (s == null) {
			return null;
		}
		Message message = objectMapper.readValue(s, Message.class);
//		log.debug("<*{}",message);
		return message;
	}

	private void write(String s) throws IOException {
		log.debug("> {}", s);
		bw.write(s);
		bw.write('\n');
		bw.flush();
	}

	private String read() throws IOException {
//		log.debug("BufferedReader br.ready(): {}", br.ready() );
		String s = br.readLine();
		log.debug("< {}", s);
		return s;
	}
}
