package irc;

import java.util.ArrayList;

/**
 * Class to represent individual users.
 *
 * One object per user per network, not one object per user per channel.
 */
public class User {
	public String nick;
	public String user;
	public String host;

	ArrayList<Channel> channels = new ArrayList<Channel>();
}
