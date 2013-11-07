/**
 * Created with IntelliJ IDEA.
 * Project: IRCClient
 * Author: Callum Macrae
 * Created: 07/11/2013 15:31
 */
public class IRCClientHandler implements irc.EventListener {
	irc.Client connection;

	@Override
	public void connected(irc.Client connection) {
		System.out.println("Connected!");
		this.connection = connection;

		connection.join("#webdevrefinery");
	}

	@Override
	public void disconnected() {
		System.out.println("Disconnected :-(");
	}

	@Override
	public void lineSent(String line) {
		System.out.println("Sent: " + line);
	}

	@Override
	public void lineReceived(String line) {
		System.out.println("Received: " + line);
	}

	@Override
	public void messageReceived(String channel, irc.User user, String message) {

	}

	@Override
	public void channelJoined(irc.Channel channel) {
		System.out.println(channel);
	}
}
