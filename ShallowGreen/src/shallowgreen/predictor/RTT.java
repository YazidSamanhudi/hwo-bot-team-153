package shallowgreen.predictor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dogo
 * 
 * Make a guesstimate about network conditions and RTT to game server.
 * Java does not have native bindings (outside some 3rd party JNI stuff)
 * for ICMP, so instead of ping, connect to game server TCP ports and
 * then close the connections.
 * 
 * The time it takes for the .connect() to return* is (empirically) very
 * close to ping results. The difference to ping results is often less
 * than 500 µs (0.5 milliseconds) and reflects the time it takes for the
 * game server to spawn new process and TCP socket.
 */
public class RTT implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(RTT.class);
	private static final int CONNECT_TIMEOUT_MS = 1000;
	private static final int RTT_SAMPLE_INTERVAL_MS = 500;       // n ms between connections (sleeps after .connect())
	private static final int RTT_SAMPLE_COUNT = 5;               // size for the ring buffer to keep samples
	private InetSocketAddress address;                           // Game server address/port
	private List<Long> rtt = new ArrayList<>(RTT_SAMPLE_COUNT);  // Used as ring buffer for n samples
	private long currentEMA = 0;                                 // Last exponential moving average (RTT_SAMPLE_COUNT base)
	private volatile boolean running = true;                     // This thread can run and stop
	private boolean printStatistics = false;                     // Don't pollute output with constantly telling about RTT

	public RTT(InetSocketAddress address) {
		this.address = address;
	}

	public void run() {
		char counter = 0;
		long[] ema = {-1, -1};                                     // ema = exponential moving average
		long start, end;
		long usecs = -1;
		Socket socket;
		log.info("RTT estimator thread running.");
		while (running) {
			try {
				socket = new Socket();                                 // socket needs to be remade every time after .close()
				socket.setTcpNoDelay(true);                            // may or may not have any effect
				start = System.nanoTime();                             // save start time nanoseconds
				socket.connect(address, CONNECT_TIMEOUT_MS);           // ...connect()
				end = System.nanoTime();                               // save time just after connection has been established
				usecs = (end - start) / 1000;                          // and measure time taken. Note that the .connect()
				socket.close();                                        // seems to return right after it sends the last ACK
				// and measured time is very close to ping results.
				if (rtt.isEmpty()) {
					log.debug("RTT estimator: first time init.");
					for (int i = 0; i < 5; i++) {
						rtt.add(usecs);
					}
					ema[0] = ema[1] = usecs;                             // put first value to both EMA slots
				}

			} catch (SocketTimeoutException ste) {
				log.error("RTT estimator: socket.connect() timed out.");
				usecs = ema[0];
			} catch (IOException ioe) {
				log.error("RTT estimator: IOException.");
			}

			// Record last µs to ring buffer, calculate EMA, increase counter.
			rtt.set((counter % RTT_SAMPLE_COUNT), usecs);
			log.debug("RTT estimator: socket.connect() took " + usecs + " µs.");

			// EMA = exponential moving average. EMA_now = EMA_previous + 2/(RTT_SAMPLE_COUNT+1) * (RTT_now - EMA_previous)
			ema[(counter % 2)] = (long) (ema[(counter + 1) % 2] + ((double) 2.0 / (RTT_SAMPLE_COUNT + 1.0) * (usecs - ema[(counter + 1) % 2])));
			currentEMA = ema[counter % 2];
			counter += 1;

			// If statistics are requested, print them still only every tenth time (5-6 seconds)
			if (counter % 10 == 0 && printStatistics) {
				log.info("Current RTT estimate: " + this.getRTTmsEstimate() + " ms.");
			}

			// Don't hammer game server to death.
			try {
				Thread.sleep(RTT_SAMPLE_INTERVAL_MS);
			} catch (InterruptedException ie) {
				log.error("RTT estimator: InterruptedException.");
			}
		}
	}

	public double getRTTmsEstimate() {
		return (currentEMA / 1000.0);
	}

	public void stop() {
		running = false;
	}

	public void printStatistics(boolean bool) {
		this.printStatistics = bool;
	}
}
