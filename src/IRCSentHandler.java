import irc.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created with IntelliJ IDEA.
 * Project: IRCClient
 * Author: Callum Macrae
 * Created: 08/11/2013 11:33
 */
public class IRCSentHandler implements ActionListener {
	private JTextField input;
	private Client client;

	public IRCSentHandler(JTextField input, Client client) {
		this.input = input;
		this.client = client;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String text = input.getText();
		input.setText("");

		if (text.charAt(0) == '/' && text.charAt(1) != '/') {
			String[] splitText = text.split(" ");
			String command = splitText[0].substring(1);

			if (command.equals("join")) {
				client.join(splitText[1]);
			}
		} else {
			client.sendMessage(text);
		}
	}
}