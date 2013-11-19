package irc.events;

import irc.Client;
import irc.communicator.Communicator;

/**
 * Base IRCEvent. All IRC event classes should extend from this one.
 *
 * While this class can be used, it is better to create a new class that
 * extends from this one - you'll want to add information.
 */
public class IRCEvent {
	public Client client;
	public Communicator current;

	public IRCEvent(Client client) {
		this.client = client;
		
		String destination = client.currentDestination;

		if (destination == null) {
			return;
		}

		if (destination.equalsIgnoreCase("console")) {
			current = null;
		} else if (destination.startsWith("#")) {
			current = client.channels.get(destination);
		} else {
			current = client.users.get(destination);
		}
	}
}
