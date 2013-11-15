package irc.events;

import irc.Client;

/**
 * Used for the channelJoined event.
 */
public class ChannelSwitchedEvent extends IRCEvent {
	public String destination;

	public ChannelSwitchedEvent(Client client) {
		super(client);
	}
}
