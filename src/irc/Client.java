package irc;
import irc.communicator.*;
import irc.events.*;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;

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
	public HashMap<String, Channel> channels = new HashMap<String, Channel>();
	public HashMap<String, User> users = new HashMap<String, User>();
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

	public Events events = new Events();

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
	 * Attempt to create a socket and connect to the IRC network.
	 *
	 * @return Returns itself to allow method chaining.
	 * @throws IRCException Will be thrown if an error occurs.
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
						Thread.sleep(5);
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
	 *
	 * @return The nick.
	 */
	public String getNick() {
		return nick;
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
	 * @param message     The message to use as part message.
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
		events.fire("disconnected");

		return this;
	}

	/**
	 * Send an action to a Communicator object (a user or channel).
	 *
	 * @param destination Communicator object representing the destination
	 *                    to send the action to.
	 * @param action     The action to send.
	 * @return Returns itself to allow method chaining.
	 */
	public Client sendAction(Communicator destination, String action) {
		sendAction(destination.getName(), action);
		return this;
	}

	/**
	 * Send an action to the current destination (the currently open window).
	 *
	 * @param action The action to send.
	 * @return Returns itself to allow method chaining.
	 */
	public Client sendAction(String action) {
		sendAction(currentDestination, action);
		return this;
	}

	/**
	 * Send an action to a channel or user.
	 *
	 * @param destination The channel or user to send to.
	 * @param action     The action to send.
	 * @return Returns itself to allow method chaining.
	 */
	public Client sendAction(String destination, String action) {
		sendRaw(String.format("PRIVMSG %s :\u0001ACTION %s\u0001", destination, action));

		ActionEvent event = new ActionEvent(this);
		event.user = users.get(nick);
		event.destination = destination;
		event.action = action;

		events.fire("actionSent", event);

		return this;
	}

	/**
	 * Send a message to a Communicator object (a user or channel).
	 *
	 * @param destination Communicator object representing the destination
	 *                    to send the message to.
	 * @param message     The message to send.
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
	 * @param message     The message to send.
	 * @return Returns itself to allow method chaining.
	 */
	public Client sendMessage(String destination, String message) {
		// Fire messageSend event
		MessageEvent event = new MessageEvent(this);
		event.destination = destination;
		event.message = message;
		event.user = users.get(nick);
		event.us = true;
		events.fire("messageSend", event);

		if (!event.sendCancelled()) {
			sendRaw(String.format("PRIVMSG %s :%s", destination, message));
		}

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
		RawEvent rawEvent = new RawEvent(this);
		rawEvent.line = line;
		events.fire("lineSent", rawEvent);

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
	 * Set user info ready for connection.
	 *
	 * @param nick     Nickname to use.
	 * @param user     User / ident to use.
	 * @param realname Real nick to use.
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

		ChannelSwitchedEvent event = new ChannelSwitchedEvent(this);
		event.destination = destination;
		events.fire("channelSwitched", event);

		return this;
	}

	/**
	 * Private method to handle new lines from the IRC server. Basically
	 * just exists so that less indents are used.
	 *
	 * @param line The received line.
	 * @return Returns itself to allow method chaining.
	 * @throws IRCException Rarely throws this; just on nick already taken.
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
				events.fire("connected");

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
			} catch (NumberFormatException e) {
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
			JoinedEvent joinedEvent;

			switch (switchBy) {
				case PING:
					System.out.println("ping!");
					sendRaw("PONG " + splitLine[1]);
					break;

				case N332:
					channel = channels.get(splitLine[3]);
					channel.topic = line.substring(line.indexOf(":", 3) + 1);
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
					joinedEvent = new JoinedEvent(this);
					joinedEvent.channel = channel;
					joinedEvent.user = user;
					joinedEvent.us = true;
					events.fire("channelJoined", joinedEvent);
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
					joinedEvent = new JoinedEvent(this);
					joinedEvent.channel = channel;
					joinedEvent.user = user;
					joinedEvent.us = false;
					events.fire("channelJoined", joinedEvent);
					break;

				case NICK:
					user = getUser(splitLine[0]);
					String newnick = splitLine[2].substring(1);

					// Special case if it is us
					if (user.nick.equals(nick)) {
						nick = newnick;
					}

					// Fire nickChanged event. Warning, fired BEFORE user.nick change.
					NickChangedEvent nickChangedEvent = new NickChangedEvent(this);
					nickChangedEvent.user = user;
					nickChangedEvent.oldnick = user.nick;
					nickChangedEvent.newnick = newnick;
					nickChangedEvent.us = nick.equals(newnick);
					events.fire("nickChanged", nickChangedEvent);

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
					PartedEvent partedEvent = new PartedEvent(this);
					partedEvent.user = user;
					partedEvent.channel = channel;
					partedEvent.partMessage = "";
					partedEvent.us = user.nick.equals(nick);

					if (partedEvent.us) {
						channel.joined = false;
					} else if (line.contains("PART " + splitLine[2] + " :")) {
						int offset = splitLine[0].length() + splitLine[2].length() + 8;
						partedEvent.partMessage = line.substring(offset);
					}

					events.fire("channelParted", partedEvent);

					break;

				case PRIVMSG:
					user = getUser(splitLine[0]);
					String channelName = splitLine[2]; // Might not be a channel
					String message = line.substring(line.indexOf(":", 2) + 1);

					if (channelName.equals(nick)) {
						// See whether ACTION or normal PRIVMSG
						if (message.startsWith("\u0001ACTION") && message.endsWith("\u0001")) {
							message = message.substring(8, message.length() - 1);

							// Fire queryActionReceived event
							ActionEvent event = new ActionEvent(this);
							event.user = user;
							event.destination = user.nick;
							event.action = message;

							events.fire("queryActionReceived", event);
						} else {
							// Fire queryReceived event
							MessageEvent event = new MessageEvent(this);
							event.user = user;
							event.destination = user.nick;
							event.message = message;

							events.fire("queryReceived", event);
						}
					} else {
						channel = channels.get(channelName);

						// See whether ACTION or normal PRIVMSG
						if (message.startsWith("\u0001ACTION") && message.endsWith("\u0001")) {
							message = message.substring(8, message.length() - 1);

							// Fire actionReceived event
							ActionEvent event = new ActionEvent(this);
							event.user = user;
							event.destination = channel.name;
							event.action = message;

							events.fire("actionReceived", event);
						} else {
							// Fire messageReceived event
							MessageEvent event = new MessageEvent(this);
							event.channel = channel;
							event.user = user;
							event.destination = user.nick;
							event.message = message;

							events.fire("messageReceived", event);
						}
					}
					break;

				case QUIT:
					user = getUser(splitLine[0]);

					if (user.nick.equals(nick)) {
						break;
					}

					String quitMessage = "";
					if (line.contains("QUIT :")) {
						quitMessage = line.substring(splitLine[0].length() + splitLine[2].length() + 7);
					}

					for (Channel chan : user.channels) {
						chan.users.remove(user);
					}

					QuitEvent quitEvent = new QuitEvent(this);
					quitEvent.user = user;
					quitEvent.quitMessage = quitMessage;
					events.fire("userQuit", quitEvent);

					// Remove user object
					users.remove(user.nick);

					break;

				default:
					break;
			}
		}

		// Fire lineReceived event
		RawEvent rawEvent = new RawEvent(this);
		rawEvent.line = line;
		events.fire("lineReceived", rawEvent);

		return this;
	}
}
