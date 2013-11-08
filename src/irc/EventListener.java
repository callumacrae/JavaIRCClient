package irc;
import irc.communicator.*;

/**
 * IRCListener interface to be used when giving an class to the
 * addEventListener() method.
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

	// This is called BEFORE the users' nick is changed. Use oldnick and newnick, not user.nick.
	public void nickChanged(User user, String oldnick, String newnick, boolean us);
}
