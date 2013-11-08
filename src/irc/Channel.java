package irc;

import java.util.ArrayList;

/**
 * Class to represent channels.
 */
public class Channel extends Communicator {
	public String topic;
	public String name;
	ArrayList<User> users = new ArrayList<User>();

	public boolean joined = false;

	public Channel(Client client) {
		super(client);
	}

	@Override
	public String getName() {
		return name;
	}
}
