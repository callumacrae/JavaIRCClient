import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.Socket;

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


		IRC connection = new IRC("irc.freenode.net");
		connection.setUserInfo("callum-test", "callum", "Callum Macrae");

		connection.addEventListener(new IRCEventListener() {
			IRC connection;

			@Override
			public void connected(IRC connection) {
				System.out.println("Connected!");
				this.connection = connection;

				connection.join("#webdevrefinery");
			}

			@Override
			public void disconnected() {
				System.out.println("Disconnected :-(");
			}

			@Override
			public void lineSent(String line) {
				System.out.println("Sent: " + line);
			}

			@Override
			public void lineReceived(String line) {
				System.out.println("Received: " + line);
			}

			@Override
			public void messageReceived(String channel, IRCUser user, String message) {
				//To change body of implemented methods use File | Settings | File Templates.
			}

			@Override
			public void channelJoined(IRCChannel channel) {
				System.out.println(channel);
			}
		});

		try {
			connection.connect();
		} catch (IRCException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
}
