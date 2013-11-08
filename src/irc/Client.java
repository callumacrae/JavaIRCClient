package irc;
import irc.communicator.*;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * An IRC library.
 *
 * @author Callum Macrae (callum@macr.ae)
 * @since 06/11/2013 00:16
 */
public class Client {

	// Socket variables
	private PrintWriter pout;
	private BufferedReader bin;

	// List variables
	private List<EventListener> listeners = new ArrayList<EventListener>();
	private HashMap<String, Channel> channels = new HashMap<String, Channel>();
	private HashMap<String, User> users = new HashMap<String, User>();

	// Connection information variables
	private String host;
	private int port;
	private String user;
	private String nick;
	private String realname;

	// Miscellaneous variables
	private boolean connected = false;
	private String currentDestination;

	/**
	 * Server info is specified on object creation with optional port.
	 *
	 * @param host The host to connect to.
	 */
	public Client(String host) {
		this(host, 6667);
	}

	/**
	 * Server info is specified on object creation with optional port.
	 *
	 * @param host The host to connect to.
	 * @param port The port to connect to.
	 */
	public Client(String host, int port) {
		this.host = host;
		this.port = port;
	}

	/**
	 * Adds an event listener containing the methods defined in the
	 * IRCEventListener class.
	 *
	 * @param listener The event listener.
	 * @return Returns itself to allow method chaining.
	 */
	public Client addEventListener(EventListener listener) {
		listeners.add(listener);

		return this;
	}

	/**
	 * Attempt to create a socket and connect to the IRC network.
	 *
	 * @throws IRCException Will be thrown if an error occurs.
	 * @return Returns itself to allow method chaining.
	 */
	public Client connect() throws IRCException {
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
		sendRaw(String.format("USER %s 8 * :%s", user, realname));
		sendRaw("NICK " + nick);

		return this;
	}

	/**
	 * Join a channel. Doesn't return the IRCChannel object because it probably
	 * isn't populated yet; wait for the channelJoined event to be fired.
	 *
	 * @param channel The channel nick.
	 * @return Returns itself to allow method chaining.
	 */
	public Client join(String channel) {
		if (channel.charAt(0) != '#') {
			channel = '#' + channel;
		}

		sendRaw("JOIN " + channel);

		Channel chanInfo = new Channel(this);
		chanInfo.name = channel;
		channels.put(channel, chanInfo);

		return this;
	}

	/**
	 * Send a line of text over the socket. It will add \r\n, no need to
	 * manually add it.
	 *
	 * @param line Text to send.
	 * @return Returns itself to allow method chaining.
	 */
	// @todo: Make private, eventually
	public Client sendRaw(String line) {
		pout.write(line + "\r\n");
		pout.flush();

		// Fire lineSent event
		for (EventListener listener : listeners) {
			listener.lineSent(line);
		}

		return this;
	}

	/**
	 * Send a message to a Communicator object (a user or channel).
	 *
	 * @param destination Communicator object representing the destination
	 *                    to send the message to.
	 * @param message The message to send.
	 * @return Returns itself to allow method chaining.
	 */
	public Client sendMessage(Communicator destination, String message) {
		sendMessage(destination.getName(), message);
		return this;
	}

	/**
	 * Send a message to the current destination (the currently open window).
	 *
	 * @param message The message to send.
	 * @return Returns itself to allow method chaining.
	 */
	public Client sendMessage(String message) {
		sendMessage(currentDestination, message);
		return this;
	}

	/**
	 * Send a message to a channel or user.
	 *
	 * @param destination The channel or user to send to.
	 * @param message The message to send.
	 * @return Returns itself to allow method chaining.
	 */
	public Client sendMessage(String destination, String message) {
		sendRaw(String.format("PRIVMSG %s :%s", destination, message));

		return this;
	}

	/**
	 * Set user info ready for connection.
	 *
	 * @param nick Nickname to use.
	 * @param user User / ident to use.
	 * @param realname Real nick to use.
	 *
	 * @return Returns itself to allow method chaining.
	 */
	public Client setUserInfo(String nick, String user, String realname) {
		this.nick = nick;
		this.user = user;
		this.realname = realname;

		return this;
	}

	/**
	 * Switches to the specified user or channel.
	 *
	 * @param destination The Communicator object for the user or channel to
	 *                    switch to.
	 * @return Returns itself to allow method chaining.
	 */
	public Client switchTo(Communicator destination) {
		switchTo(destination.getName());
		return this;
	}

	/**
	 * Switches to the specified user or channel.
	 *
	 * @param destination The nick of the user or the channel nick.
	 * @return Returns itself to allow method chaining.
	 */
	public Client switchTo(String destination) {
		this.currentDestination = destination;
		return this;
	}

	/**
	 * Private method to handle new lines from the IRC server. Basically
	 * just exists so that less indents are used.
	 *
	 * @param line The received line.
	 * @throws IRCException Rarely throws this; just on nick already taken.
	 *
	 * @return Returns itself to allow method chaining.
	 */
	private Client handleNewLine(String line) throws IRCException {
		if (!connected) {
			// Connected
			if (line.contains("004")) {
				connected = true;

				// Fire connected event
				for (EventListener listener : listeners) {
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
				sendRaw("PONG " + splitLine[1]);

			// Channel topic on join
			} else if (splitLine[1].equals("332")) {
				Channel channel = channels.get(splitLine[3]);
				channel.topic = line.substring(line.indexOf(":", 3));

			// Channel users on join
			} else if (splitLine[1].equals("353")) {
				Channel channel = channels.get(splitLine[4]);
				String[] nicks = line.substring(line.indexOf(":", 3)).split(" ");

				for (String nick : nicks) {
					char priv = ' ';

					// Nick "@callumacrae" separates to '@' and "callumacrae" (defaults to ' ')
					// @todo: Reimplement this
					if (nick.matches("^[@+]")) {
						priv = nick.charAt(0);
						nick = nick.substring(1);
					}

					User user;

					// If user already exists, get user object
					if (users.containsKey(nick)) {
						user = users.get(nick);

					// If user isn't known, create user object
					} else {
						user = new User(this);
						users.put(nick, user);
						user.nick = nick;
					}

					user.channels.add(channel);
					channel.users.add(user);
				}

			// Joined channel
			} else if (splitLine[1].equals("366")) {
				Channel channel = channels.get(splitLine[3]);
				channel.joined = true;

				// Fire channelJoined event
				for (EventListener listener : listeners) {
					listener.channelJoined(channel);
				}
			}
		}

		// Fire lineReceived event
		for (EventListener listener : listeners) {
			listener.lineReceived(line);
		}

		return this;
	}
}
