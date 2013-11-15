package irc.events;

import irc.Client;
import irc.communicator.User;

/**
 * Used for the nickChanged event.
 */
public class NickChangedEvent extends IRCEvent {
	public User user;
	public String oldnick;
	public String newnick;
	public boolean us;

	public NickChangedEvent(Client client) {
		super(client);
	}
}
