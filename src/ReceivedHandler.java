import irc.*;
import irc.communicator.*;
import irc.events.*;

import javax.swing.*;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * Project: IRCClient
 * Author: Callum Macrae
 * Created: 07/11/2013 15:31
 */
public class ReceivedHandler implements EventListener {
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
	 * @param event The event object.
	 */
	@Override
	public void actionReceived(ActionEvent event) {
		DefaultListModel channelList = content.get(event.destination);
		channelList.addElement(String.format("* %s %s", event.user.nick, event.action));
	}

	/**
	 * Fired when an action is send to the server. Useful for logging.
	 *
	 * @param event The event object.
	 */
	@Override
	public void actionSent(ActionEvent event) {
		content.get(event.destination).addElement(String.format("* %s %s", event.client.getNick(), event.action));
	}

	/**
	 * Fired when a channel is joined by either us, or another user.
	 *
	 * @param event The event object. Contains:
	 *              Channel event.channel the channel joined.
	 *              User event.user The current user.
	 */
	@Override
	public void channelJoined(JoinedEvent event) {
		if (event.us) {
			Channel channel = event.channel;

			channels.addElement(channel.name);
			DefaultListModel list = new DefaultListModel();
			list.addElement(String.format("You have joined %s", channel.name));
			content.put(channel.name, list);
			channel.switchTo();
		} else {
			DefaultListModel channelList = content.get(event.channel.name);
			channelList.addElement(String.format("%s has joined %s", event.user.nick, event.channel.name));
		}
	}

	/**
	 * Fired when a channel is parted (when the server sends the part stuff,
	 * not when the user types /part).
	 *
	 * @param event The event object.
	 */
	@Override
	public void channelParted(PartedEvent event) {
		if (event.us) {
			channels.removeElement(event.channel.name);
			content.remove(event.channel.name);

			event.client.switchTo("console");
		} else {
			DefaultListModel channelList = content.get(event.channel.name);

			String message;
			String nick = event.user.nick;
			if (event.partMessage.equals("")) {
				message = String.format("%s has parted %s", nick, event.channel.name);
			} else {
				message = String.format("%s has parted %s (%s)", nick, event.channel.name, event.partMessage);
			}

			channelList.addElement(message);
		}
	}

	/**
	 * Fired when the user switches channel. This shouldn't really be in the IRC package.
	 *
	 * @param event The event object.
	 */
	@Override
	public void channelSwitched(ChannelSwitchedEvent event) {
		String channel = event.destination;

		contentJList.setModel(content.get(channel));

		String topic = null;
		if (channel.startsWith("#")) {
			topic = event.client.channels.get(channel).topic;
		}

		topicBar.setText(channel + (topic == null ? "" : ": " + topic));
	}

	/**
	 * Fired when a connection is established and user is connected.
	 */
	@Override
	public void connected() {
		System.out.println("Connected!");
	}

	/**
	 * Fired when the client disconnects.
	 */
	@Override
	public void disconnected() {
		System.out.println("Disconnected!");
	}

	/**
	 * Fired when a line of text is received from the server.
	 *
	 * @param event The event object.
	 */
	@Override
	public void lineReceived(RawEvent event) {
//		System.out.println("Received: " + event.line);
		DefaultListModel console = content.get("console");
		console.addElement(event.line);
	}

	/**
	 * Fired when a line of text is sent by us to the server.
	 *
	 * @param event The event object.
	 */
	@Override
	public void lineSent(RawEvent event) {
//		System.out.println("Sent: " + event.line);
		DefaultListModel console = content.get("console");
		console.addElement(event.line);
	}

	/**
	 * Fired when a PRIVMSG is received from a channel.
	 *
	 * @param event The event object
	 */
	@Override
	public void messageReceived(MessageEvent event) {
		DefaultListModel channelList = content.get(event.channel.name);
		channelList.addElement(String.format("<%s> %s", event.user.nick, event.message));
	}

	/**
	 * Fired when a message is send to the server. Useful for logging.
	 *
	 * @param event The event object.
	 */
	@Override
	public void messageSend(MessageEvent event) {
		DefaultListModel list;

		if (!event.destination.startsWith("#") && !content.containsKey(event.destination)) {
			channels.addElement(event.destination);
			list = new DefaultListModel();
			content.put(event.destination, list);
		} else {
			list = content.get(event.destination);
		}

		list.addElement(String.format("<%s> %s", event.client.getNick(), event.message));
	}

	/**
	 * Fired when a nick is changed (either ours or someone else's). It is
	 * called before the nick is changed, but this behaviour should not be
	 * relied upon - user oldnick and newnick, not user.nick.
	 *
	 * @param event The event object.
	 */
	@Override
	public void nickChanged(NickChangedEvent event) {
		String message;
		if (event.us) {
			message = String.format("You are now known as %s", event.newnick);
		} else {
			message = String.format("%s is now known as %s", event.oldnick, event.newnick);
		}

		for (Channel channel : event.user.channels) {
			content.get(channel.name).addElement(message);
		}

		if (content.containsKey(event.oldnick)) {
			DefaultListModel convo = content.get(event.oldnick);
			convo.addElement(message);

			content.remove(event.oldnick);
			content.put(event.newnick, convo);

			channels.removeElement(event.oldnick);
			channels.addElement(event.newnick);
		}
	}

	/**
	 * Fired when an action is received in a query from a user.
	 *
	 * @param event The event object.
	 */
	@Override
	public void queryActionReceived(ActionEvent event) {
		DefaultListModel query;
		User user = event.user;

		if (content.containsKey(user.nick)) {
			query = content.get(user.nick);
		} else {
			channels.addElement(user.nick);
			query = new DefaultListModel();
			content.put(user.nick, query);
		}

		query.addElement(String.format("* %s %s", user.nick, event.action));

		user.switchTo();
	}

	/**
	 * Fired when a PRIVMSG is received from a user.
	 *
	 * @param event The event object.
	 */
	@Override
	public void queryReceived(MessageEvent event) {
		DefaultListModel query;
		User user = event.user;

		if (content.containsKey(user.nick)) {
			query = content.get(user.nick);
		} else {
			channels.addElement(user.nick);
			query = new DefaultListModel();
			content.put(user.nick, query);
		}

		query.addElement(String.format("<%s> %s", user.nick, event.message));

		user.switchTo();
	}

	/**
	 * Fired when a user quits. It is called after userQuitPerChannel.
	 *
	 * @param event The event object.
	 */
	@Override
	public void userQuit(QuitEvent event) {
		if (content.containsKey(event.user.nick)) {
			content.remove(event.user.nick);
			channels.removeElement(event.user.nick);
		}

		String message = event.quitMessage;

		if (message.equals("")) {
			message = String.format("%s has quit", event.user.nick);
		} else {
			message = String.format("%s has quit (%s)", event.user.nick, message);
		}

		for (Channel channel : event.user.channels) {
			DefaultListModel channelList = content.get(channel.name);
			channelList.addElement(message);
		}
	}
}
