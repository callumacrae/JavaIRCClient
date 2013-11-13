package irc.communicator;

import irc.Client;

/**
 * Class to represent things that can be communicated with such as users and
 * channels. irc.User and irc.Channel both extend from this.
 */
public abstract class Communicator {

	private Client client;

	public Communicator(Client client) {
		this.client = client;
	}

	/**
	 * Should return how to send a message to the Communicator (so, the nick
	 * of a user or the name of a channel).
	 *
	 * @return Where to send a message to.
	 */
	public abstract String getName();

	/**
	 * Send an action to the user or channel.
	 *
	 * Facade method: calls client.sendAction for the destination.
	 *
	 * @param action The action to send.
	 * @return Returns itself to allow method chaining.
	 */
	public Communicator sendAction(String action) {
		client.sendAction(this, action);
		return this;
	}

	/**
	 * Send a message to the user or channel.
	 *
	 * Facade method: calls client.sendMessage for the destination.
	 *
	 * @param message The message to send.
	 * @return Returns itself to allow method chaining.
	 */
	public Communicator sendMessage(String message) {
		client.sendMessage(this, message);
		return this;
	}

	/**
	 * Switches to the user or channel.
	 *
	 * Facade method: calls client.sendMessage for the destination.
	 *
	 * @return Returns itself to allow method chaining.
	 */
	public Communicator switchTo() {
		client.switchTo(this);
		return this;
	}
}
