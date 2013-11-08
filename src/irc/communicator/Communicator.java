package irc.communicator;

import irc.Client;

/**
 * Created with IntelliJ IDEA.
 * Project: IRCClient
 * Author: Callum Macrae
 * Created: 08/11/2013 11:52
 */
public abstract class Communicator {

	private Client client;

	public Communicator(Client client) {
		this.client = client;
	}

	public abstract String getName();

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

	public Communicator switchTo() {
		client.switchTo(this);
		return this;
	}
}
