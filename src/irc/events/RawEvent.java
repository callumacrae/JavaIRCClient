package irc.events;

import irc.Client;

/**
 * Used for the nickChanged event.
 */
public class RawEvent extends IRCEvent {
	public String line;

	public RawEvent(Client client) {
		super(client);
	}
}
