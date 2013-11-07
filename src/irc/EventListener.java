package irc;

/**
 * IRCListener interface to be used when giving an class to the addEventListener() method.
 *
 * Not all methods are called yet, WIP.
 */
public interface EventListener {
	public void connected(Client connection);
	public void disconnected();

	public void lineSent(String line);
	public void lineReceived(String line);

	public void channelJoined(Channel channel);
	public void messageReceived(String channel, User user, String message);
}
