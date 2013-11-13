import irc.*;
import irc.communicator.*;

import javax.swing.*;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * Project: IRCClient
 * Author: Callum Macrae
 * Created: 07/11/2013 15:31
 */
public class ReceivedHandler implements EventListener {
	private Client client;
	private DefaultListModel channels;
	private HashMap<String, DefaultListModel> content;
	private JList contentJList;
	private JLabel topicBar;
	private JFrame frame;

	public ReceivedHandler(DefaultListModel channels, HashMap content, JList contentJList, JLabel topicBar, JFrame frame) {
		this.channels = channels;
		this.content = content;
		this.contentJList = contentJList;
		this.topicBar = topicBar;
		this.frame = frame;
	}

	/**
	 * Fired when an action is received from a channel.
	 *
	 * @param channel Channel object representing the channel.
	 * @param user    User object of sender.
	 * @param action The action.
	 */
	@Override
	public void actionReceived(Channel channel, User user, String action) {
		DefaultListModel channelList = content.get(channel.name);
		channelList.addElement(String.format("* %s %s", user.nick, action));
	}

	/**
	 * Fired when an action is send to the server. Useful for logging.
	 *
	 * @param destination String of nick or channel name.
	 * @param action      The action.
	 */
	@Override
	public void actionSent(String destination, String action) {
		content.get(destination).addElement(String.format("* %s %s", client.getNick(), action));
	}

	/**
	 * Fired when a channel is joined (when the server sends the join stuff,
	 * not when the user types /join).
	 *
	 * @param channel Channel object representing the channel connected to.
	 */
	@Override
	public void channelJoined(Channel channel) {
		channels.addElement(channel.name);
		DefaultListModel list = new DefaultListModel();
		list.addElement(String.format("You have joined %s", channel.name));
		content.put(channel.name, list);
		channel.switchTo();
	}

	/**
	 * Fired when another user joins a channel.
	 *
	 * @param channel Channel object representing the channel.
	 * @param user    User object representing the user.
	 */
	@Override
	public void channelJoined(Channel channel, User user) {
		DefaultListModel channelList = content.get(channel.name);
		channelList.addElement(String.format("%s has joined %s", user.nick, channel.name));
	}

	/**
	 * Fired when a channel is parted (when the server sends the part stuff,
	 * not when the user types /part).
	 *
	 * @param channel Channel object representing the channel.
	 */
	@Override
	public void channelParted(Channel channel) {
		channels.removeElement(channel.name);
		content.remove(channel.name);

		client.switchTo("console");
	}

	/**
	 * Fired when another user parts a channel.
	 *
	 * @param channel     Channel object representing the channel.
	 * @param user        User object representing the user.
	 * @param partMessage The part message of the user (or "" if not specified).
	 */
	@Override
	public void channelParted(Channel channel, User user, String partMessage) {
		DefaultListModel channelList = content.get(channel.name);

		String message;
		if (partMessage.equals("")) {
			message = String.format("%s has parted %s", user.nick, channel.name);
		} else {
			message = String.format("%s has parted %s (%s)", user.nick, channel.name, partMessage);
		}

		channelList.addElement(message);
	}

	/**
	 * Fired when the user switches channel. This shouldn't really be in the IRC package.
	 *
	 * @param channel String containing channel name.
	 */
	@Override
	public void channelSwitched(String channel) {
		contentJList.setModel(content.get(channel));

		String topic = null;
		if (channel.startsWith("#")) {
			topic = client.channels.get(channel).topic;
		}

		topicBar.setText(channel + (topic == null ? "" : ": " + topic));
	}

	/**
	 * Fired when a connection is established and user is connected.
	 *
	 * @param client Client object that has been connected to.
	 */
	@Override
	public void connected(Client client) {
		System.out.println("Connected!");
		this.client = client;
	}

	/**
	 * Fired when the client disconnects.
	 */
	@Override
	public void disconnected() {
	}

	/**
	 * Fired when a line of text is received from the server.
	 *
	 * @param line The line that was received.
	 */
	@Override
	public void lineReceived(String line) {
//		System.out.println("Received: " + line);
		DefaultListModel console = content.get("console");
		console.addElement(line);
	}

	/**
	 * Fired when a line of text is sent by us to the server.
	 *
	 * @param line The line that was sent.
	 */
	@Override
	public void lineSent(String line) {
//		System.out.println("Sent: " + line);
		DefaultListModel console = content.get("console");
		console.addElement(line);
	}

	/**
	 * Fired when a PRIVMSG is received from a channel.
	 *
	 * @param channel Channel object representing the channel.
	 * @param user    User object of sender.
	 * @param message The message.
	 */
	@Override
	public void messageReceived(Channel channel, User user, String message) {
		DefaultListModel channelList = content.get(channel.name);
		channelList.addElement(String.format("<%s> %s", user.nick, message));
	}

	/**
	 * Fired when a message is send to the server. Useful for logging.
	 *
	 * @param destination String of nick or channel name.
	 * @param message     The message.
	 */
	@Override
	public void messageSent(String destination, String message) {
		content.get(destination).addElement(String.format("<%s> %s", client.getNick(), message));
	}

	/**
	 * Fired when a nick is changed (either ours or someone else's). It is
	 * called before the nick is changed, but this behaviour should not be
	 * relied upon - user oldnick and newnick, not user.nick.
	 *
	 * @param user    User object of the user whose nick changed.
	 * @param oldnick The old nick.
	 * @param newnick The nick that the user changed to.
	 * @param us      True if us.
	 */
	@Override
	public void nickChanged(User user, String oldnick, String newnick, boolean us) {
		String message;
		if (us) {
			message = String.format("You are now known as %s", newnick);
		} else {
			message = String.format("%s is now known as %s", oldnick, newnick);
		}

		for (Channel channel : user.channels) {
			content.get(channel.name).addElement(message);
		}

		if (content.containsKey(oldnick)) {
			DefaultListModel convo = content.get(oldnick);
			convo.addElement(message);

			content.remove(oldnick);
			content.put(newnick, convo);

			channels.removeElement(oldnick);
			channels.addElement(newnick);
		}
	}

	/**
	 * Fired when an action is received in a query from a user.
	 *
	 * @param user    User object of sender.
	 * @param action The action.
	 */
	@Override
	public void queryActionReceived(User user, String action) {
		DefaultListModel query;
		if (content.containsKey(user.nick)) {
			query = content.get(user.nick);
		} else {
			channels.addElement(user.nick);
			query = new DefaultListModel();
			content.put(user.nick, query);
		}

		query.addElement(String.format("* %s %s", user.nick, action));

		user.switchTo();
	}

	/**
	 * Fired when a PRIVMSG is received from a user.
	 *
	 * @param user    User object of sender.
	 * @param message The message.
	 */
	@Override
	public void queryReceived(User user, String message) {
		DefaultListModel query;
		if (content.containsKey(user.nick)) {
			query = content.get(user.nick);
		} else {
			channels.addElement(user.nick);
			query = new DefaultListModel();
			content.put(user.nick, query);
		}

		query.addElement(String.format("<%s> %s", user.nick, message));

		user.switchTo();
	}

	/**
	 * Fired when a user quits. It is called after userQuitPerChannel.
	 *
	 * @param user    User object of user who just quit.
	 * @param message Quit message (or "" if not specified).
	 */
	@Override
	public void userQuit(User user, String message) {
		if (content.containsKey(user.nick)) {
			content.remove(user.nick);
			channels.removeElement(user.nick);
		}
	}

	/**
	 * Fired for each channel a user is in when the user quits. Is called before userQuit.
	 *
	 * @param user    User object of user who just quit.
	 * @param channel Channel object of channel user was in.
	 * @param message Quit message (or "" if not specified).
	 */
	@Override
	public void userQuitPerChannel(User user, Channel channel, String message) {
		DefaultListModel channelList = content.get(channel.name);

		if (message.equals("")) {
			message = String.format("%s has parted %s", user.nick, channel.name);
		} else {
			message = String.format("%s has parted %s (%s)", user.nick, channel.name, message);
		}

		channelList.addElement(message);
	}
}
