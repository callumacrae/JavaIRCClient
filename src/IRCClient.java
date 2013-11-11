import irc.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
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

		// Create the frame
		JFrame frame = new JFrame("IRC");
		frame.setLayout(new BorderLayout());

		// Create the channels pane
		final DefaultListModel channels = new DefaultListModel();
		channels.addElement("console");
		JList channelsJList = new JList(channels);
		Component channelsPane = new JScrollPane(channelsJList);
		channelsPane.setPreferredSize(new Dimension(200, 0));
		frame.add(channelsPane, BorderLayout.WEST);

		// Create the content area
		HashMap<String, DefaultListModel> content = new HashMap<String, DefaultListModel>();
		DefaultListModel console = new DefaultListModel();
		content.put("console", console);
		JList contentJList = new JList(console);
		Component contentPane = new JScrollPane(contentJList);
		frame.add(contentPane, BorderLayout.CENTER);

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
				.addEventListener(new ReceivedHandler(channels, content, contentJList, frame))
				.setUserInfo("foo-world", "callum", "Callum Macrae")
				.setDefaultQuitMessage("Test quit message ($user$)");

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
}
