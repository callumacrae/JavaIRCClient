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
	private JFrame frame;

	public ReceivedHandler(DefaultListModel channels, HashMap content, JList contentJList, JFrame frame) {
		this.channels = channels;
		this.content = content;
		this.contentJList = contentJList;
		this.frame = frame;
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
	 * Fired when a PRIVMSG is received from a channel.
	 *
	 * @param channel String containing channel name.
	 * @param user User object of sender.
	 * @param message The message.
	 */
	@Override
	public void messageReceived(String channel, User user, String message) {
		content.get(channel).addElement(String.format("<%s> %s", user.nick, message));
	}

	/**
	 * Fired when a message is send to the server. Useful for logging.
	 *
	 * @param destination String of nick or channel name.
	 * @param message The message.
	 */
	@Override
	public void messageSent(String destination, String message) {
		content.get(destination).addElement(String.format("<%s> %s", client.getNick(), message));
	}

	/**
	 * Fired when a PRIVMSG is received from a user.
	 * @param user User object of sender.
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
	 * @param user User object representing the user.
	 */
	@Override
	public void channelJoined(Channel channel, User user) {
		DefaultListModel channelList = content.get(channel.name);
		channelList.addElement(String.format("%s has joined %s", user.nick, channel.name));
	}

	/**
	 * Fired when the user switches channel. This shouldn't really be in the IRC package.
	 * @param channel String containing channel name.
	 */
	@Override
	public void channelSwitched(String channel) {
		contentJList.setModel(content.get(channel));
	}

	/**
	 * Fired when a nick is changed (either ours or someone else's). It is
	 * called before the nick is changed, but this behaviour should not be
	 * relied upon - user oldnick and newnick, not user.nick.
	 *
	 * @param user User object of the user whose nick changed.
	 * @param oldnick The old nick.
	 * @param newnick The nick that the user changed to.
	 * @param us True if us.
	 */
	@Override
	public void nickChanged(User user, String oldnick, String newnick, boolean us) {

	}
}
