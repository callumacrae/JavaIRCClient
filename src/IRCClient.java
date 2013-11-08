import irc.*;
import javax.swing.*;
import java.awt.*;

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
		DefaultListModel channels = new DefaultListModel();
		Component channelsPane = new JScrollPane(new JList(channels));
		channelsPane.setPreferredSize(new Dimension(200, 0));
		frame.add(channelsPane, BorderLayout.WEST);

		// Create the content area
		DefaultListModel content = new DefaultListModel();
		Component contentPane = new JScrollPane(new JList(content));
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
		Client client = new Client("irc.freenode.net")
				.addEventListener(new IRCReceivedHandler(channels, content, frame))
				.setUserInfo("callum-test", "callum", "Callum Macrae");

		// Connect
		try {
			client.connect();
		} catch (IRCException e) {
			e.printStackTrace();
			System.exit(0);
		}

		// Finish setting up the input
		input.requestFocus();
		input.addActionListener(new IRCSendHandler(input, client));
	}
}
