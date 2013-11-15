package irc.events;

import irc.Client;

/**
 * Base IRCEvent. All IRC event classes should extend from this one.
 *
 * While this class can be used, it is better to create a new class that
 * extends from this one - you'll want to add information.
 */
public class IRCEvent {
	public Client client;

	public IRCEvent(Client client) {
		this.client = client;
	}
}
