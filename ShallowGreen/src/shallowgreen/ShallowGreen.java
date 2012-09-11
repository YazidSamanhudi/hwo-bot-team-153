package shallowgreen;

import java.net.InetSocketAddress;

public class ShallowGreen {

	public static void main(String[] args) {
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

		Connection connection = new Connection(args[0], address);
		connection.run();
	}

	private static boolean isEmpty(String s) {
		if (s == null || s.length() < 1 || s.trim().length() < 1) {
			return true;
		}
		return false;
	}

	private static void usageAndExit(String message) {
		if(message!=null)
			System.err.println(message);
		System.err.println("Usage: ShallowGreen [name] [host] [port]");
		System.exit(1);
	}
}
