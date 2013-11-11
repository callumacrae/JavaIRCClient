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
public class SendHandler implements ActionListener {
	private JTextField input;
	private Client client;

	public SendHandler(JTextField input, Client client) {
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

			Commands switchBy = Commands.CNF;
			for (Commands value : Commands.values()) {
				if (value.name().equalsIgnoreCase(command)) {
					switchBy = value;
					break;
				}
			}

			switch (switchBy) {
				case JOIN:
					client.join(splitText[1]);
					break;

				case MSG:
				case QUERY:
					client.sendMessage(splitText[1], text.substring(splitText[1].length() + 6));
					break;

				case NICK:
					client.setNick(splitText[1]);
					break;

				case PART:
					if (splitText.length == 1) {
						// /part
						client.part(client.currentDestination);
					} else if (splitText[1].startsWith("#") && splitText.length == 2) {
						// /part #channel
						client.part(splitText[1]);
					} else if (splitText[1].startsWith("#")) {
						// /part #channel message here
						client.part(splitText[1], text.substring(splitText[1].length() + 7));
					} else {
						// /part message here
						client.part(client.currentDestination, text.substring(6));
					}
					break;

				case QUIT:
				case EXIT:
					if (splitText.length > 1) {
						client.quit(text.substring(6));
					} else {
						client.quit();
					}

					System.exit(0);
					break;

				case QUOTE:
					client.sendRaw(text.substring(7));
					break;

				case SAY:
					client.sendMessage(text.substring(5));
					break;

				case CNF:
				default:
					System.out.println("Command not found: " + command);
					break;
			}
		} else {
			client.sendMessage(text.charAt(0) == '/' ? text.substring(1) : text);
		}
	}
}
