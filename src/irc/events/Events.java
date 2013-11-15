package irc.events;

import irc.EventListener;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple events library written for callumacrae/JavaIRCClient
 *
 * Author: Callum Macrae
 * Created: 14/11/2013 10:49
 */
public class Events {
	private List<EventListener> listeners = new ArrayList<EventListener>();

	/**
	 * Adds an event listener.
	 *
	 * @param listener The event listener.
	 * @return Returns itself to allow method chaining.
	 */
	public Events addListener(EventListener listener) {
		listeners.add(listener);

		return this;
	}

	/**
	 * Fires an event with no IRCEvent object..
	 *
	 * @param listenerName The name of the event to be fired.
	 * @return Returns itself in order to allow method chaining.
	 */
	public Events fire(String listenerName) {
		for (EventListener listener : listeners) {
			try {
				Class<? extends EventListener> listenerClass = listener.getClass();
				Method method = listenerClass.getMethod(listenerName);
				method.invoke(listener);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return this;
	}

	/**
	 * Fires an event.
	 *
	 * @param listenerName The name of the event to be fired.
	 * @param event An object extended from IRCEvent.
	 * @return Returns itself in order to allow method chaining.
	 */
	public Events fire(String listenerName, IRCEvent event) {
		for (EventListener listener : listeners) {
			try {
				Class eventClass = event.getClass();
				Class<? extends EventListener> listenerClass = listener.getClass();
				Method method = listenerClass.getMethod(listenerName, eventClass);
				method.invoke(listener, event);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return this;
	}

	/**
	 * Removes an event listener.
	 *
	 * @param listener The event listener (must be the exact object).
	 * @return Returns itself to allow method chaining.
	 */
	public Events removeListener(EventListener listener) {
		listeners.remove(listener);

		return this;
	}
}
