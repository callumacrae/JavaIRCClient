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
		JFrame frame = new JFrame("IRC");
		frame.setLayout(new BorderLayout());

		DefaultListModel channels = new DefaultListModel();
		Component channelsPane = new JScrollPane(new JList(channels));
		channelsPane.setPreferredSize(new Dimension(200, 0));
		frame.add(channelsPane, BorderLayout.WEST);

		DefaultListModel content = new DefaultListModel();
		Component contentPane = new JScrollPane(new JList(content));
		frame.add(contentPane, BorderLayout.CENTER);

		JTextField input = new JTextField();
		frame.add(input, BorderLayout.SOUTH);

		frame.setSize(800, 500);
		frame.setLocation(100, 100);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);


		Client connection = new Client("irc.freenode.net")
				.addEventListener(new IRCClientHandler(channels, content, frame))
				.setUserInfo("callum-test", "callum", "Callum Macrae");

		try {
			connection.connect();
		} catch (IRCException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
}
