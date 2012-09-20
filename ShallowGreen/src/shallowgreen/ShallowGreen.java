package shallowgreen;

import java.net.InetSocketAddress;

import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;

public class ShallowGreen {

	static {
		// before anything starts, add a Logback shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				ILoggerFactory ilf=LoggerFactory.getILoggerFactory();
				if(ilf instanceof LoggerContext) {
					((LoggerContext)ilf).stop();
				}
			}
		});
	}

	public static void main(String[] args) {
		ShallowGreen sg=new ShallowGreen();
		sg.mainParser(args);
	}

	public void mainParser(String[] args) {
		String duelistName=null;
		if(args.length==4) {
			if(isEmpty(args[3])) {
				usageAndExit("ShallowGreen: duelist name was empty");
			}
			duelistName=args[3];
			String[] newArgs=new String[3];
			System.arraycopy(args,0,newArgs,0,newArgs.length);
			args=newArgs;
		}
		if (args.length > 3 || args.length < 3 || isEmpty(args[0]) || isEmpty(args[1]) || isEmpty(args[2])) {
			usageAndExit("ShallowGreen: Incorrect argument count ("+args.length+"!= 3) or given argument is empty.");
		}

		int port = -1;
		try {
			port = Integer.parseInt(args[2]);
			if(port>65535)
				throw new NumberFormatException("too high");
		} catch (NumberFormatException e) {
			usageAndExit("ShallowGreen: '"+args[2]+"' is not a valid port number.");
		}

		InetSocketAddress address=new InetSocketAddress(args[1],port);
		if(address.isUnresolved()) {
			usageAndExit("ShallowGreen: '"+args[1]+"' cannot be resolved as an address.");
		}

		Connection connection=newConnection(args[0],address,new GameFactory(),duelistName);
		connection.run();
	}

	protected boolean isEmpty(String s) {
		if (s == null || s.length() < 1 || s.trim().length() < 1) {
			return true;
		}
		return false;
	}

	protected void usageAndExit(String message) {
		if(message!=null)
			System.err.println(message);
		System.err.println("Usage: "+getClass().getSimpleName()+" [name] [host] [port] <duelopponent>");
		System.exit(1);
	}

	protected Connection newConnection(String name, InetSocketAddress address, GameFactory gameFactory, String duelistName) {
		return new Connection(name,address,new GameFactory(),duelistName);
	}

}
