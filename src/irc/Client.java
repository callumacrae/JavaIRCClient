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
	private Socket socket;
	private PrintWriter pout;
	private BufferedReader bin;

	// List variables
	private List<EventListener> listeners = new ArrayList<EventListener>();
	private HashMap<String, Channel> channels = new HashMap<String, Channel>();
	private HashMap<String, User> users = new HashMap<String, User>();
	private HashMap<String, String> serverInfo = new HashMap<String, String>();

	// Connection information variables
	private String host;
	private int port;
	private String user;
	private String nick;
	private String realname;

	private String defaultQuitMessage = "$user$";
	private String defaultPartMessage = "$user$";

	// Miscellaneous variables
	private boolean connected = false;
	public String currentDestination;

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
			socket = new Socket(host, port);
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
	 * Gets the nick of our user.
	 * @return The nick.
	 */
	public String getNick() {
		return nick;
	}

	/**
	 * Gets a user object from the user string (:nick!ident@host).
	 *
	 * @param userString User string to get / retrieve user data from.
	 * @return User object representing user.
	 */
	public User getUser(String userString) {
		userString = userString.substring(1);
		String[] splitString = userString.split("[!@]");

		User user;

		if (users.containsKey(splitString[0])) {
			user = users.get(splitString[0]);

			if (user.user == null) {
				user.user = splitString[1];
			}
			if (user.host == null) {
				user.host = splitString[2];
			}
		} else {
			// This code is unlikely to ever be ran, actually.
			user = new User(this);
			user.nick = splitString[0];
			user.user = splitString[1];
			user.host = splitString[2];

			users.put(user.nick, user);
		}

		return user;
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

		// Fire messageSent event
		for (EventListener listener : listeners) {
			listener.messageSent(destination, message);
		}

		return this;
	}

	/**
	 * Sets the default part message.
	 *
	 * @param message The message to set as default.
	 * @return Returns itself to allow method chaining.
	 */
	public Client setDefaultPartMessage(String message) {
		defaultPartMessage = message;
		return this;
	}

	/**
	 * Sets the default quit message.
	 *
	 * @param message The message to set as default.
	 * @return Returns itself to allow method chaining.
	 */
	public Client setDefaultQuitMessage(String message) {
		defaultQuitMessage = message;
		return this;
	}

	/**
	 * Changes the users' nick. Changing the nick property will be handled when then server confirms the nick change.
	 *
	 * @param nick The nick to change to.
	 * @return Returns itself to allow method chaining.
	 */
	public Client setNick(String nick) {
		sendRaw("NICK " + nick);
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

		// Fire channelSwitched event
		for (EventListener listener : listeners) {
			listener.channelSwitched(destination);
		}

		return this;
	}

	/**
	 * Parts the specified channel using a default part message.
	 *
	 * @param channel Channel object representing the channel to part.
	 * @return Returns itself to allow method chaining.
	 */
	public Client part(Channel channel) {
		part(channel.name);
		return this;
	}

	/**
	 * Parts the specified channel using a default part message.
	 *
	 * @param channelName The name of the channel to part.
	 * @return Returns itself to allow method chaining.
	 */
	public Client part(String channelName) {
		part(channelName, defaultPartMessage.replace("$user$", nick));
		return this;
	}

	/**
	 * Parts the specified channel using specified part message.
	 *
	 * @param channel Channel object representing the channel to part.
	 * @param message The message to use as part message.
	 * @return Returns itself to allow method chaining.
	 */
	public Client part(Channel channel, String message) {
		part(channel.name, message);
		return this;
	}

	/**
	 * Parts the specified channel using specified part message.
	 *
	 * @param channelName The name of the channel to part.
	 * @param message The message to use as part message.
	 * @return Returns itself to allow method chaining.
	 */
	public Client part(String channelName, String message) {
		sendRaw(String.format("PART %s :%s", channelName, message));
		return this;
	}

	/**
	 * Close the IRC connection using a default quit message.
	 *
	 * @return Returns itself to allow method chaining.
	 */
	public Client quit() {
		quit(defaultQuitMessage.replace("$user$", nick));
		return this;
	}

	/**
	 * Close the IRC connection.
	 *
	 * @param message Quit message to use.
	 * @return Returns itself to allow method chaining.
	 */
	public Client quit(String message) {
		sendRaw("QUIT :" + message);
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Fire disconnected event
		for (EventListener listener : listeners) {
			listener.disconnected();
		}

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

				User you = new User(this);
				you.nick = nick;
				users.put(nick, you);

				// Fire connected event
				for (EventListener listener : listeners) {
					listener.connected(this);
				}

			// Get server info
			} else if (line.contains("005")) {
				String[] splitLine = line.split(" ");
				for (int i = 0; i < splitLine.length; i++) {
					if (splitLine[i].contains("=")) {
						String[] splitSplit = splitLine[i].split("=");
						if (splitSplit.length == 2) {
							serverInfo.put(splitSplit[0], splitSplit[1]);
						}
					}
				}

			// Failed to connect; bad nickname
			} else if (line.contains("432")) {
				throw new IRCException("Erroneous Nickname returned by server");

			// Failed to connect; nick already taken
			} else if (line.contains("433")) {
				int maxLength = serverInfo.containsKey("NICKLEN") ? Integer.parseInt(serverInfo.get("NICKLEN")) : 16;
				if (nick.length() >= maxLength) {
					throw new IRCException("Nick already in use");
				}

				nick += "_";
				sendRaw("NICK " + nick);
			}
		} else {
			String[] splitLine = line.split(" ");

			String command = splitLine[1].toUpperCase();
			boolean commandIsNumber = true;
			try {
				Integer.parseInt(command);
			} catch(NumberFormatException e) {
				commandIsNumber = false;
			}

			command = (commandIsNumber ? "N" : "") + command;

			Commands switchBy = Commands.CNF;
			for (Commands value : Commands.values()) {
				if (value.name().equalsIgnoreCase(command)) {
					switchBy = value;
					break;
				}
			}

			Channel channel;
			User user;
			switch (switchBy) {
				case PING:
					sendRaw("PONG " + splitLine[1]);
					break;

				case N332:
					channel = channels.get(splitLine[3]);
					channel.topic = line.substring(line.indexOf(":", 3));
					break;

				case N353:
					channel = channels.get(splitLine[4]);
					String[] nicks = line.substring(line.indexOf(":", 3)).split(" ");

					for (String nick : nicks) {
						char priv = ' ';

						// Nick "@callumacrae" separates to '@' and "callumacrae" (defaults to ' ')
						// @todo: Reimplement this
						if (nick.matches("^[@+]")) {
							priv = nick.charAt(0);
							nick = nick.substring(1);
						}

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
					break;

				case N366:
					user = users.get(nick);
					channel = channels.get(splitLine[3]);
					channel.joined = true;

					channel.users.add(user);
					user.channels.add(channel);

					// Fire channelJoined event
					for (EventListener listener : listeners) {
						listener.channelJoined(channel);
					}
					break;

				case JOIN:
					user = getUser(splitLine[0]);
					channel = channels.get(splitLine[2]);

					// If our user, fire on N366
					if (user.nick.equals(nick)) {
						break;
					}

					channel.users.add(user);
					user.channels.add(channel);

					// Fire channelJoined event
					for (EventListener listener : listeners) {
						listener.channelJoined(channel, user);
					}
					break;

				case NICK:
					user = getUser(splitLine[0]);
					String newnick = splitLine[2].substring(1);

					// Special case if it is us
					if (user.nick.equals(nick)) {
						nick = newnick;
					}

					// Fire channelJoined event. Warning, fired BEFORE user.nick change.
					for (EventListener listener : listeners) {
						listener.nickChanged(user, user.nick, newnick, nick.equals(newnick));
					}

					users.remove(user.nick);
					users.put(newnick, user);
					user.nick = newnick;
					break;

				case PART:
					user = getUser(splitLine[0]);
					channel = channels.get(splitLine[2]);

					channel.users.remove(user);
					user.channels.remove(channel);

					// Fire channelParted event
					if (user.nick.equals(nick)) {
						channel.joined = false;

						for (EventListener listener : listeners) {
							listener.channelParted(channel);
						}
					} else {
						String partMessage = "";
						if (line.contains("PART " + splitLine[2] + " :")) {
							partMessage = line.substring(splitLine[0].length() + splitLine[2].length() + 8);
						}

						for (EventListener listener : listeners) {
							listener.channelParted(channel, user, partMessage);
						}
					}

					break;

				case PRIVMSG:
					user = getUser(splitLine[0]);
					String channelName = splitLine[2];
					String message = line.substring(line.indexOf(":", 2) + 1);

					if (channelName.equals(nick)) {
						// Fire queryReceived event
						for (EventListener listener : listeners) {
							listener.queryReceived(user, message);
						}
					} else {
						// Fire messageReceived event
						for (EventListener listener : listeners) {
							listener.messageReceived(channelName, user, message);
						}
					}
					break;

				default:
					break;
			}
		}

		// Fire lineReceived event
		for (EventListener listener : listeners) {
			listener.lineReceived(line);
		}

		return this;
	}
}
