package shallowgreen;

import java.net.InetSocketAddress;
import java.util.Arrays;

public class ShallowGreen {

	private static boolean debugMode;

	public static void main(String[] args) {
		if (args.length == 4) {
			if ("-d".equals(args[0])) {
				debugMode = true;
				args = Arrays.copyOfRange(args, 1, args.length);
			}
		}
		if (args.length > 3 || args.length < 3 || isEmpty(args[0]) || isEmpty(args[1]) || isEmpty(args[2])) {
			usageAndExit("ShallowGreen.main(): incorrect argument count (!= 3) or given argument is empty.");
		}

		int port = -1;
		try {
			port = Integer.parseInt(args[2]);
		} catch (NumberFormatException e) {
			usageAndExit("ShallowGreen.main(): NumberFormatException with args[2]: '" + args[2] + "'.");
		}

		InetSocketAddress address = new InetSocketAddress(args[1], port);
		if (address.isUnresolved()) {
			usageAndExit("ShallowGreen.main(): InetSocketAddress address can not resolve.");
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
		System.err.println(message);
		System.err.println("Usage: ShallowGreen [name] [host] [port]");
		System.exit(1);
	}
}
