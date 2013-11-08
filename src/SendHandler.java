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

			switch (Commands.valueOf(command.toUpperCase())) {
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
			}
		} else {
			client.sendMessage(text);
		}
	}
}
