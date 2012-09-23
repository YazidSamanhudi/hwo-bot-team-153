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

import shallowgreen.message.GameIsOverMessage;
import shallowgreen.message.GameStartedMessage;
import shallowgreen.message.JoinMessage;
import shallowgreen.message.Message;
import shallowgreen.message.RequestDuelMessage;
import shallowgreen.predictor.RTT;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;

public class Connection implements Runnable {
	private static final Logger log=LoggerFactory.getLogger(Connection.class);
	private static final Logger logDataflow=LoggerFactory.getLogger("dataflow");

	private static final ObjectMapper objectMapper=new ObjectMapper();

	static {
		objectMapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(Visibility.ANY));
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
	}

	private String name;
	private InetSocketAddress address;
	private BufferedWriter bw;
	private BufferedReader br;
	private RTT rttEstimator;
	private int gamesPlayed;
	private int gamesWon;
	private GameFactory gameFactory;
	private Statistics stats;
	private String duelistName;

	@SuppressWarnings("unused")
	private Connection() {}

	public Connection(String name, InetSocketAddress address, GameFactory gameFactory) {
		this.name=name;
		this.address=address;
		this.gameFactory=gameFactory;
		this.stats=new Statistics();
		this.rttEstimator=new RTT(address);
		new Thread(rttEstimator).start();
		rttEstimator.stop();
	}

	public Connection(String name, InetSocketAddress address, GameFactory gameFactory, String duelistName) {
		this(name,address,gameFactory);
		this.duelistName=duelistName;
	}

	public void run() {
		// TODO: beautiful Channel-based IO?
		try (Socket socket=new Socket()) {
			try {
				log.debug("Connect to {}",address);
				socket.setTcpNoDelay(true);
				socket.connect(address);
				bw=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				br=new BufferedReader(new InputStreamReader(socket.getInputStream()));

				// tell the server we are here
				if(duelistName!=null) {
					RequestDuelMessage requestDuelMessage=new RequestDuelMessage(name,duelistName);
					sendMessage(requestDuelMessage);
				} else {
					JoinMessage joinMessage=new JoinMessage(name);
					sendMessage(joinMessage);
				}

				Game game=null;
				GameStartedMessage latestGameStartedMessage=null;

				while(true) {
					Message message;
					try {
						message=readMessage();
					} catch(JsonProcessingException e) {
						log.error("Unknown JSON",e);
						continue;
					}
					if(message==null)
						break;
					if(message instanceof GameStartedMessage) {
						latestGameStartedMessage=(GameStartedMessage)message;
						game=gameFactory.newGame();
						game.setConnection(this);
						game.setRTTEstimator(rttEstimator);
					}
					if(game!=null) {
						try {
							game.handleMessage(message);
						} catch(Throwable t) {
							// short-circuit IOExceptions - nothing we can do about them
							if(t instanceof IOException)
								throw (IOException)t;
							// game had an error - pretend it didn't happen by re-instantiating the Game
							// and sending it the GameStartedMessage and the actual message
							game=gameFactory.newGame();
							try {
								// latestGameStartedMessage is always non-null here
								if(latestGameStartedMessage!=null && !(message instanceof GameStartedMessage))
									game.handleMessage(latestGameStartedMessage);
								game.handleMessage(message);
							} catch(Throwable t2) {
								// still failing, not much we can do about it
								log.error("Second try of processing message failed",t2);
								t2.addSuppressed(t);
								throw t2;
							}
						}
					}
					if(message instanceof GameIsOverMessage) {
						if(game.getStatistics().getGamesWon()==1) {
							stats.gameWon();
						} else {
							stats.gameLost();
						}
						gamesPlayed=stats.getGamesPlayed();
						gamesWon=stats.getGamesWon();
						stats.setMaxX(Math.max(stats.getMaxX(),game.getStatistics().getMaxX()));
						stats.setMinX(Math.min(stats.getMinX(),game.getStatistics().getMinX()));
						stats.setMaxY(Math.max(stats.getMaxY(),game.getStatistics().getMaxY()));
						stats.setMinY(Math.min(stats.getMinY(),game.getStatistics().getMinY()));

						log.info("Stats for this bot: games played: {}, won {}, losses {}, X: ({}, {}), Y: ({}, {})",new Object[] { gamesPlayed,gamesWon,(gamesPlayed-gamesWon),stats.getMinX(),stats.getMaxX(),stats.getMinY(),stats.getMaxY() });
						game=null;
					}
				}

			} catch(IOException e) {
				log.error("Communications failure",e);
			}
		} catch(IOException e) {
			log.error("Socket close failed",e);
		}
		log.debug("Done.");
	}

	public void sendMessage(Message message) throws JsonGenerationException, JsonMappingException, IOException {
		// rate limit needs to be handled on a higher tier
		String s=objectMapper.writerWithType(message.getClass()).writeValueAsString(message);
		write(s);
	}

	private Message readMessage() throws IOException, JsonProcessingException {
		String s=read();
		if(s==null) {
			return null;
		}
		return objectMapper.readValue(s,Message.class);
	}

	private void write(String s) throws IOException {
		log.debug("> {}",s);
		logDataflow.debug(">\t{}\t{} ",System.currentTimeMillis(),s);
		bw.write(s);
		bw.write('\n');
		bw.flush();
	}

	private String read() throws IOException {
		String s=br.readLine();
		log.debug("< {}",s);
		logDataflow.debug("<\t{}\t{}",System.currentTimeMillis(),s);
		return s;
	}

}
