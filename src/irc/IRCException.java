package irc;

/**
 * Exception class to be used by the IRC library.
 */
public class IRCException extends Exception {
	public IRCException(String message) {
		super(message);
	}
}
