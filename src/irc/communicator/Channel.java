package irc.communicator;

import irc.Client;
import java.util.ArrayList;

/**
 * Class to represent channels.
 */
public class Channel extends Communicator {
	public String topic;
	public String name;
	public ArrayList<User> users = new ArrayList<User>();

	public boolean joined = false;

	public Channel(Client client) {
		super(client);
	}

	/**
	 * @return The channel name.
	 */
	@Override
	public String getName() {
		return name;
	}
}
