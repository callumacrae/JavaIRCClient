import irc.*;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * A simple IRC client.
 *
 * Project: IRCClient
 * Author: Callum Macrae
 * Created: 05/11/2013 15:33
 */
public class IRCClient {
	public static void main(String[] args) {

		// Create the frames
		JFrame frame = new JFrame("IRC");
		frame.setLayout(new BorderLayout());

		Container centreFrame = new Container();
		centreFrame.setLayout(new BorderLayout());

		// Create the channels pane
		final DefaultListModel channels = new DefaultListModel();
		channels.addElement("console");
		JList channelsJList = new JList(channels);
		Component channelsPane = new JScrollPane(channelsJList);
		channelsPane.setPreferredSize(new Dimension(200, 0));
		frame.add(channelsPane, BorderLayout.WEST);

		// Reorder channel list on contents change
		channels.addListDataListener(new ListDataListener() {
			private boolean running = false;

			/**
			 * Sort the channels. DRYs the code.
			 */
			private void sortChannels() {
				if (!running) {
					running = true;
					sortChannelList(channels);
					running = false;
				}
			}

			@Override
			public void intervalAdded(ListDataEvent listDataEvent) {
				sortChannels();
			}

			@Override
			public void intervalRemoved(ListDataEvent listDataEvent) {
				sortChannels();
			}

			@Override
			public void contentsChanged(ListDataEvent listDataEvent) {
				sortChannels();
			}
		});

		// Create the content area
		HashMap<String, DefaultListModel> content = new HashMap<String, DefaultListModel>();
		DefaultListModel console = new DefaultListModel();
		content.put("console", console);
		JList contentJList = new JList(console);
		Component contentPane = new JScrollPane(contentJList);
		centreFrame.add(contentPane, BorderLayout.CENTER);

		// Create the topic bar
		JLabel topicBar = new JLabel("Console");
		JScrollPane topicBarScroll = new JScrollPane(topicBar);
		topicBarScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		centreFrame.add(topicBarScroll, BorderLayout.NORTH);

		// Create the names list
		DefaultListModel names = new DefaultListModel();
		JList namesJList = new JList(names);
		Component namesPane = new JScrollPane(namesJList);
		namesPane.setPreferredSize(new Dimension(150, 0));
		centreFrame.add(namesPane, BorderLayout.EAST);

		frame.add(centreFrame, BorderLayout.CENTER);

		// Create the input
		final JTextField input = new JTextField();
		frame.add(input, BorderLayout.SOUTH);

		// Finish setting up the frame
		frame.setSize(800, 500);
		frame.setLocation(100, 100);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);


		// Set up the client
		final Client client = new Client("irc.freenode.net")
				.setUserInfo("foo-world", "callum", "Callum Macrae")
				.setDefaultQuitMessage("Test quit message ($user$)");

		client.currentDestination = "console";

		client.events.addListener(new ReceivedHandler(channels, content, contentJList, topicBar, names, frame));

		// Connect
		try {
			client.connect();
		} catch (IRCException e) {
			e.printStackTrace();
			System.exit(0);
		}

		// Quit properly on shutdown
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				client.quit();
			}
		});

		// Finish setting up the input
		input.requestFocus();
		input.addActionListener(new SendHandler(input, client));


		// Set up the channel event listener
		channelsJList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				ListSelectionModel cList = (ListSelectionModel) e.getSource();

				if (!cList.isSelectionEmpty() && cList.getMinSelectionIndex() == cList.getMaxSelectionIndex()) {
					String channel = (String) channels.getElementAt(cList.getMinSelectionIndex());
					client.switchTo(channel);
				}

				cList.clearSelection();
			}
		});
	}

	/**
	 * Sorts the channel list by IRC order - "console", followed by channels
	 * in alphabetical order, followed by queries in alphabetical order.
	 *
	 * @param channels The DefaultListModel to reorder.
	 */
	public static void sortChannelList(DefaultListModel channels) {
		ArrayList<String> channelArray = new ArrayList<String>();
		for (int i = 0; i < channels.size(); i++) {
			channelArray.add((String) channels.elementAt(i));
		}

		// Sort so that channels are above users (and console above everything).
		Collections.sort(channelArray, new Comparator<String>() {
			@Override
			public int compare(String chan1, String chan2) {
				if (chan1.equals("console") || (chan1.startsWith("#") && !chan2.startsWith("#"))) {
					return -1;
				}

				return chan1.compareToIgnoreCase(chan2);
			}
		});

		channels.clear();

		for (String channelName : channelArray) {
			channels.addElement(channelName);
		}
	}
}
