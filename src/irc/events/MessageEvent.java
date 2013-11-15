package irc.events;

import irc.Client;
import irc.communicator.Channel;
import irc.communicator.User;

/**
 * Used for the actionSent and actionReceived events.
 */
public class MessageEvent extends IRCEvent {
	public String message;
	public String destination;
	public Channel channel;
	public User user;
	public boolean us = false;

	private boolean cancelSend = false;

	public void cancelSend() {
		cancelSend = true;
	}

	public boolean sendCancelled() {
		return cancelSend;
	}

	public MessageEvent(Client client) {
		super(client);
	}
}
