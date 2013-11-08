package irc.communicator;

import irc.Client;
import java.util.ArrayList;

/**
 * Class to represent individual users.
 *
 * One object per user per network, not one object per user per channel.
 */
public class User extends Communicator {
	public String nick;
	public String user;
	public String host;

	public ArrayList<Channel> channels = new ArrayList<Channel>();

	public User(Client client) {
		super(client);
	}

	/**
	 * @return The user's nick.
	 */
	@Override
	public String getName() {
		return nick;
	}
}
