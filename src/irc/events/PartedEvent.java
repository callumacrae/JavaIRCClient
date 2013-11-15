package irc.events;

import irc.Client;
import irc.communicator.Channel;
import irc.communicator.User;

/**
 * Used for the nickChanged event.
 */
public class PartedEvent extends IRCEvent {
	public Channel channel;
	public User user;
	public String partMessage;
	public boolean us;

	public PartedEvent(Client client) {
		super(client);
	}
}
