package irc;

import java.util.ArrayList;

/**
 * Class to represent individual users.
 *
 * One object per user per network, not one object per user per channel.
 */
public class User {
	public String nick;
	public String user;
	public String host;

	ArrayList<Channel> channels = new ArrayList<Channel>();

	private Client client;

	public User(Client client) {
		this.client = client;
	}

	/**
	 * Send a message to the user.
	 *
	 * Facade method: calls client.sendMessage for the user.
	 *
	 * @param message The message to send.
	 * @return Returns itself to allow method chaining.
	 */
	// @todo: Put this in another class and extend from it.
	public User sendMessage(String message) {
		client.sendMessage(this, message);
		return this;
	}
}
