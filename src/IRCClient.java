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

		DefaultListModel names = new DefaultListModel();
		Component namesPane = new JScrollPane(new JList(names));
		namesPane.setPreferredSize(new Dimension(200, 0));
		frame.add(namesPane, BorderLayout.WEST);

		DefaultListModel content = new DefaultListModel();
		Component contentPane = new JScrollPane(new JList(content));
		frame.add(contentPane, BorderLayout.CENTER);

		JTextField input = new JTextField();
		frame.add(input, BorderLayout.SOUTH);

		frame.setSize(800, 500);
		frame.setLocation(100, 100);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);


		irc.Client connection = new irc.Client("irc.freenode.net")
				.addEventListener(new IRCClientHandler())
				.setUserInfo("callum-test", "callum", "Callum Macrae");

		try {
			connection.connect();
		} catch (irc.IRCException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
}
