package irc;

import java.util.ArrayList;

/**
 * Class to represent channels.
 */
public class Channel {
	public String topic;
	public String name;
	ArrayList<User> users = new ArrayList<User>();

	public boolean joined = false;

	private Client client;

	public Channel(Client client) {
		this.client = client;
	}

	/**
	 * Send a message to the channel.
	 *
	 * Facade method: calls client.sendMessage for the channel.
	 *
	 * @param message The message to send.
	 * @return Returns itself to allow method chaining.
	 */
	// @todo: Put this in another class and extend from it.
	public Channel sendMessage(String message) {
		client.sendMessage(this, message);
		return this;
	}
}
