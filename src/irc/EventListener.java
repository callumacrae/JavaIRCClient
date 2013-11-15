package irc;
import irc.events.*;

/**
 * IRCListener interface to be used when giving an class to the
 * addEventListener() method.
 * <p/>
 * Not all methods are called yet, WIP.
 */
public interface EventListener {
	/**
	 * Fired when an action is received from a channel.
	 *
	 * @param event The event object.
	 */
	public void actionReceived(ActionEvent event);

	/**
	 * Fired when a message is send to the server. Useful for logging.
	 *
	 * @param event The event object.
	 */
	public void actionSent(ActionEvent event);

	/**
	 * Fired when a channel is joined (when the server sends the join stuff,
	 * not when the user types /join).
	 *
	 * @param event The event object.
	 */
	public void channelJoined(JoinedEvent event);

	/**
	 * Fired when the user or another user parts a channel.
	 *
	 * @param event The event object.
	 */
	public void channelParted(PartedEvent event);

	/**
	 * Fired when the user switches channel. This shouldn't really be in the IRC package.
	 *
	 * @param event The event object.
	 */
	// @todo: Move out of IRC package
	public void channelSwitched(ChannelSwitchedEvent event);

	/**
	 * Fired when a connection is established and user is connected.
	 */
	public void connected();

	/**
	 * Fired when the client disconnects.
	 */
	public void disconnected();

	/**
	 * Fired when a line of text is received from the server.
	 *
	 * @param event The event object.
	 */
	public void lineReceived(RawEvent event);

	/**
	 * Fired when a line of text is sent by us to the server.
	 *
	 * @param event The event object.
	 */
	public void lineSent(RawEvent event);

	/**
	 * Fired when a PRIVMSG is received from a channel.
	 *
	 * @param event The message object.
	 */
	public void messageReceived(MessageEvent event);

	/**
	 * Fired when a message is about to be sent to the server. Useful for logging.
	 *
	 * Send can be cancelled using event.cancelSend().
	 *
	 * @param event The event object.
	 */
	public void messageSend(MessageEvent event);

	/**
	 * Fired when a nick is changed (either ours or someone else's). It is
	 * called before the nick is changed, but this behaviour should not be
	 * relied upon - user oldnick and newnick, not user.nick.
	 *
	 * @param event The event object.
	 */
	public void nickChanged(NickChangedEvent event);

	/**
	 * Fired when an action is received in a query from a user.
	 *
	 * @param event The event object.
	 */
	public void queryActionReceived(ActionEvent event);

	/**
	 * Fired when a PRIVMSG is received from a user.
	 *
	 * @param event The event object.
	 */
	public void queryReceived(MessageEvent event);

	/**
	 * Fired when a user quits. It is called after userQuitPerChannel.
	 *
	 * @param event The event object.
	 */
	public void userQuit(QuitEvent event);
}
