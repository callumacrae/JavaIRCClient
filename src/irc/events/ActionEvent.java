package irc.events;

import irc.Client;
import irc.communicator.User;

/**
 * Used for the actionSent and actionReceived events.
 */
public class ActionEvent extends IRCEvent {
	public String action;
	public String destination;
	public User user;

	public ActionEvent(Client client) {
		super(client);
	}
}
