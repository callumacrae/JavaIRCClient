package irc.events;

import irc.Client;
import irc.communicator.User;

/**
 * Used for the nickChanged event.
 */
public class QuitEvent extends IRCEvent {
	public User user;
	public String quitMessage;

	public QuitEvent(Client client) {
		super(client);
	}
}
