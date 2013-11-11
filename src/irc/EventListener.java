package irc;

import irc.communicator.Channel;
import irc.communicator.User;

/**
 * IRCListener interface to be used when giving an class to the
 * addEventListener() method.
 * <p/>
 * Not all methods are called yet, WIP.
 */
public interface EventListener {
	/**
	 * Fired when a channel is joined (when the server sends the join stuff,
	 * not when the user types /join).
	 *
	 * @param channel Channel object representing the channel connected to.
	 */
	public void channelJoined(Channel channel);

	/**
	 * Fired when another user joins a channel.
	 *
	 * @param channel Channel object representing the channel.
	 * @param user    User object representing the user.
	 */
	public void channelJoined(Channel channel, User user);

	/**
	 * Fired when a channel is parted (when the server sends the part stuff,
	 * not when the user types /part).
	 *
	 * @param channel Channel object representing the channel.
	 */
	public void channelParted(Channel channel);

	/**
	 * Fired when another user parts a channel.
	 *
	 * @param channel     Channel object representing the channel.
	 * @param user        User object representing the user.
	 * @param partMessage The part message of the user (or "" if not specified).
	 */
	public void channelParted(Channel channel, User user, String partMessage);

	/**
	 * Fired when the user switches channel. This shouldn't really be in the IRC package.
	 *
	 * @param channel String containing channel name.
	 */
	// @todo: Move out of IRC package
	public void channelSwitched(String channel);

	/**
	 * Fired when a connection is established and user is connected.
	 *
	 * @param client Client object that has been connected to.
	 */
	public void connected(Client client);

	/**
	 * Fired when the client disconnects.
	 */
	public void disconnected();

	/**
	 * Fired when a line of text is received from the server.
	 *
	 * @param line The line that was received.
	 */
	public void lineReceived(String line);

	/**
	 * Fired when a line of text is sent by us to the server.
	 *
	 * @param line The line that was sent.
	 */
	public void lineSent(String line);

	/**
	 * Fired when a PRIVMSG is received from a channel.
	 *
	 * @param channel String containing channel name.
	 * @param user    User object of sender.
	 * @param message The message.
	 */
	public void messageReceived(String channel, User user, String message);

	/**
	 * Fired when a message is send to the server. Useful for logging.
	 *
	 * @param destination String of nick or channel name.
	 * @param message     The message.
	 */
	public void messageSent(String destination, String message);

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
	public void nickChanged(User user, String oldnick, String newnick, boolean us);

	/**
	 * Fired when a PRIVMSG is received from a user.
	 *
	 * @param user    User object of sender.
	 * @param message The message.
	 */
	public void queryReceived(User user, String message);
}
