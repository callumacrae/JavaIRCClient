import irc.*;
import irc.communicator.*;
import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * Project: IRCClient
 * Author: Callum Macrae
 * Created: 07/11/2013 15:31
 */
public class ReceivedHandler implements EventListener {
	private Client client;
	private DefaultListModel channels, content;
	private JFrame frame;

	public ReceivedHandler(DefaultListModel channels, DefaultListModel content, JFrame frame) {
		this.channels = channels;
		this.content = content;
		this.frame = frame;
	}

	@Override
	public void connected(Client client) {
		System.out.println("Connected!");
		this.client = client;
	}

	@Override
	public void disconnected() {
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
		channel.switchTo();
	}
}
