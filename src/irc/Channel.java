package irc;

import java.util.ArrayList;

/**
 * Class to represent channels.
 */
public class Channel {
	public String topic;
	public String name;
	ArrayList<User> users = new ArrayList<User>();

	public boolean joined = false;
}
