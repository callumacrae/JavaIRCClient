import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Project: IRCClient
 * Author: Callum Macrae
 * Created: 06/11/2013 00:16
 */
public class IRC {

	// Socket variables
	private PrintWriter pout;
	private BufferedReader bin;

	// List variables
	private List<IRCEventListener> listeners = new ArrayList<IRCEventListener>();
	private HashMap<String, IRCChannel> channels = new HashMap<String, IRCChannel>();
	private HashMap<String, IRCUser> users = new HashMap<String, IRCUser>();

	// Connection information variables
	private String host;
	private int port;
	private String user;
	private String nick;
	private String realname;

	// Miscellaneous variables
	private boolean connected = false;

	/**
	 * Server info is specified on object creation with optional port.
	 *
	 * @param host The host to connect to.
	 */
	public IRC(String host) {
		this(host, 6667);
	}

	/**
	 * Server info is specified on object creation with optional port.
	 *
	 * @param host The host to connect to.
	 * @param port The port to connect to.
	 */
	public IRC(String host, int port) {
		this.host = host;
		this.port = port;
	}

	/**
	 * Adds an event listener containing the methods defined in the IRCEventListener class.
	 *
	 * @param listener The event listener.
	 */
	public void addEventListener(IRCEventListener listener) {
		listeners.add(listener);
	}

	/**
	 * Attempt to create a socket and connect to the IRC network.
	 *
	 * @throws IRCException Will be thrown if an error occurs.
	 */
	public void connect() throws IRCException {
		InputStream in;
		OutputStream out;
		try {
			Socket socket = new Socket(host, port);
			in = socket.getInputStream();
			out = socket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();

			throw new IRCException("IOException occurred");
		}
		bin = new BufferedReader(new InputStreamReader(in));
		pout = new PrintWriter(out, true);

		// Check for messages in a thread because of the while loop
		new Thread(new Runnable() {
			@Override
			public void run() {
				String line;

				try {
					while ((line = bin.readLine()) != null) {
						handleNewLine(line);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();

		if (nick == null || user == null || realname == null) {
			throw new IRCException("Need more information; nick, user or realname not specified.");
		}

		// Connect if everything looks good
		sendLine(String.format("USER %s 8 * :%s", user, realname));
		sendLine("NICK " + nick);
	}

	/**
	 * Join a channel. Doesn't return the IRCChannel object because it probably isn't populated yet; wait for the
	 * channelJoined event to be fired.
	 *
	 * @param channel The channel name.
	 */
	public void join(String channel) {
		if (channel.charAt(0) != '#') {
			channel = '#' + channel;
		}

		sendLine("JOIN " + channel);

		IRCChannel chanInfo = new IRCChannel();
		chanInfo.name = channel;
		channels.put(channel, chanInfo);
	}

	/**
	 * Send a line of text over the socket. It will add \r\n, no need to manually add it.
	 *
	 * @param line  text to send.
	 */
	public void sendLine(String line) {
		pout.write(line + "\r\n");
		pout.flush();

		// Fire lineSent event
		for (IRCEventListener listener : listeners) {
			listener.lineSent(line);
		}
	}

	/**
	 * Set user info ready for connection.
	 *
	 * @param nick Nickname to use.
	 * @param user User / ident to use.
	 * @param realname Real name to use.
	 */
	public void setUserInfo(String nick, String user, String realname) {
		this.nick = nick;
		this.user = user;
		this.realname = realname;
	}

	/**
	 * Private method to handle new lines from the IRC server. Basically just exists so that less indents are used.
	 *
	 * @param line The received line.
	 * @throws IRCException Rarely throws this; just on nick already taken.
	 */
	private void handleNewLine(String line) throws IRCException {
		if (!connected) {
			// Connected
			if (line.contains("004")) {
				connected = true;

				// Fire connected event
				for (IRCEventListener listener : listeners) {
					listener.connected(this);
				}
			// Failed to connect; nick already taken
			// @todo: Handle this better, perhaps with an alt nick
			} else if (line.contains("433")) {
				throw new IRCException("Nick already in use.");
			}
		} else {
			String[] splitLine = line.split(" ");

			// Handle PINGs
			if (splitLine[0].equalsIgnoreCase("PING")) {
				sendLine("PONG " + splitLine[1]);

			// Channel topic on join
			} else if (splitLine[1].equals("332")) {
				IRCChannel channel = channels.get(splitLine[3]);
				channel.topic = line.substring(line.indexOf(":", 3));

			// Channel users on join
			} else if (splitLine[1].equals("353")) {
				IRCChannel channel = channels.get(splitLine[4]);
				String[] nicks = line.substring(line.indexOf(":", 3)).split(" ");

				for (String nick : nicks) {
					char priv = ' ';

					// Nick "@callumacrae" separates to '@' and "callumacrae" (defaults to ' ')
					// @todo: Reimplement this
					if (nick.matches("^[@+]")) {
						priv = nick.charAt(0);
						nick = nick.substring(1);
					}

					IRCUser user;

					// If user already exists, get user object
					if (users.containsKey(nick)) {
						user = users.get(nick);

					// If user isn't known, create user object
					} else {
						user = new IRCUser();
						user.nick = nick;
					}

					user.channels.add(channel);
					channel.users.add(user);
				}

			// Joined channel
			} else if (splitLine[1].equals("366")) {
				IRCChannel channel = channels.get(splitLine[3]);
				channel.joined = true;

				// Fire channelJoined event
				for (IRCEventListener listener : listeners) {
					listener.channelJoined(channel);
				}
			}
		}

		// Fire lineReceived event
		for (IRCEventListener listener : listeners) {
			listener.lineReceived(line);
		}
	}
}

/**
 * IRCListener interface to be used when giving an class to the addEventListener() method.
 *
 * Not all methods are called yet, WIP.
 */
interface IRCEventListener {
	public void connected(IRC connection);
	public void disconnected();

	public void lineSent(String line);
	public void lineReceived(String line);

	public void channelJoined(IRCChannel channel);
	public void messageReceived(String channel, IRCUser user, String message);
}

/**
 * Exception class to be used by the IRC library.
 */
class IRCException extends Exception {
	public IRCException(String message) {
		super(message);
	}
}

/**
 * Class to represent individual users.
 *
 * One object per user per network, not one object per user per channel.
 */
class IRCUser {
	public String nick;
	public String user;
	public String host;

	// This cannot be an array of IRCChannels, because recursion is bad
	ArrayList<IRCChannel> channels = new ArrayList<IRCChannel>();
}

/**
 * Class to represent channels.
 */
class IRCChannel {
	public String topic;
	public String name;
	ArrayList<IRCUser> users = new ArrayList<IRCUser>();

	public boolean joined = false;
}