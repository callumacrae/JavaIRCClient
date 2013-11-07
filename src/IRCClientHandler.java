import irc.*;
import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * Project: IRCClient
 * Author: Callum Macrae
 * Created: 07/11/2013 15:31
 */
public class IRCClientHandler implements EventListener {
	private Client client;
	private DefaultListModel channels, content;
	private JFrame frame;

	public IRCClientHandler(DefaultListModel channels, DefaultListModel content, JFrame frame) {
		this.channels = channels;
		this.content = content;
		this.frame = frame;
	}

	@Override
	public void connected(Client client) {
		System.out.println("Connected!");
		this.client = client;

		client.join("#webdevrefinery");
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
	public void messageReceived(String channel, User user, String message) {

	}

	@Override
	public void channelJoined(Channel channel) {
		channel.sendMessage("This is a test. I blame callumacrae.");
	}
}
