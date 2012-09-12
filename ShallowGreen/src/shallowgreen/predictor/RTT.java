/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
 */
public class RTT implements Runnable {
	private static final Logger log = LoggerFactory.getLogger(RTT.class);

	private static final int CONNECT_TIMEOUT_MS = 1000;
	private static final int RTT_SAMPLE_INTERVAL_MS = 500;              // n ms sleep after close before new .connect()
	private static final int RTT_SAMPLE_COUNT = 5;                      // size for the ring buffer to keep samples

	private InetSocketAddress address;
	private List<Long> rtt = new ArrayList<>(RTT_SAMPLE_COUNT);  // Ring buffer for n samples
	private long currentEMA = 0;

	private volatile boolean running = true;
	private boolean printStatistics = false;

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
				socket = new Socket();
				socket.setTcpNoDelay(true);
				log.debug("RTT: socket.connect() to {}", address);
				start = System.nanoTime();
				socket.connect(address, CONNECT_TIMEOUT_MS);
				end = System.nanoTime();
				usecs = (end - start) / 1000;
				socket.close();

				if (rtt.isEmpty()) {
					log.debug("RTT estimator: first time init.");
					for (int i = 0; i < 5; i++) {
						rtt.add(usecs);
					}
					ema[0] = ema[1] = usecs;
				}

			} catch (SocketTimeoutException ste) {
				log.error("RTT estimator: socket.connect() timed out.");
				usecs = ema[0];
			} catch (IOException ioe) {
				log.error("RTT estimator: IOException.");
			}

			rtt.set((counter % RTT_SAMPLE_COUNT), usecs);
			log.debug("RTT estimator: socket.connect() took " + usecs + " µs.");

			// EMA = exponential moving average. EMA_now = EMA_previous + 2/(RTT_SAMPLE_COUNT+1) * (RTT_now - EMA_previous)
			ema[(counter % 2)] = (long) (ema[(counter + 1) % 2] + ( (double)2.0 / (RTT_SAMPLE_COUNT + 1.0) * (usecs - ema[(counter + 1) % 2])));
			currentEMA = ema[counter % 2];
			log.debug("RTT estimator: currentEMA = " + currentEMA + ", ema[0] = " + ema[0] + ", ema[1] = " + ema[1] + ".");

			counter += 1;
			
			if (counter % 10 == 0 && printStatistics) {
				log.info("Current RTT estimate: " + this.getRTTmsEstimate() + " ms.");
			}

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
