package irc.events;

import irc.Client;
import irc.communicator.Channel;
import irc.communicator.User;

/**
 * Used for the channelJoined event.
 */
public class JoinedEvent extends IRCEvent {
	public Channel channel;
	public User user;
	public boolean us;

	public JoinedEvent(Client client) {
		super(client);
	}
}
