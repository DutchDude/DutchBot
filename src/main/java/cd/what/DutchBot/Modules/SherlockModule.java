/**
 * 
 * @author DutchDude
 */
package cd.what.DutchBot.Modules;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cd.what.DutchBot.AccessList;
import cd.what.DutchBot.DutchBot;
import cd.what.DutchBot.ModuleAbstract;
import cd.what.DutchBot.Privileges;
import cd.what.DutchBot.Events.IChannelMessageEvent;


/**
 * @author DutchDude
 *  !!!PRIVATE!!!
 */
public class SherlockModule extends ModuleAbstract implements
		IChannelMessageEvent {

	/**
	 * @param bot
	 */
	public SherlockModule(DutchBot bot) {
		super(bot);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cd.what.DutchBot.Modules.IChannelMessageEvent#
	 * notifyChannelMessageEvent (java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void notifyChannelMessageEvent(String channel, String sender,
			String login, String hostname, String message) {
		run(channel, sender, login, hostname, message);

	}

	/**
	 * Perform the actual command
	 * 
	 * @param target 
	 * @param sender
	 * @param login
	 * @param hostname
	 * @param message
	 */
	public void run(String target, String sender, String login,
			String hostname, String message) {
		if (AccessList.isAllowed(login, hostname, Privileges.USER)) {

			if (message.startsWith("!addme")) {
				System.out.println("Trying to add to sherlock db:");
				Matcher hostnameMatcher = Pattern.compile(
						"(.*?)\\..*\\.what\\.cd").matcher(hostname);

				if (!hostnameMatcher.matches())
					bot.sendMessage(target, sender
							+ ", are you logged in with Drone?");

				int userID = Integer.parseInt(login);
				String userName = hostnameMatcher.group(1);

				try {

					URL sherlock = new URL(
							String.format(
									"http://sherlock.whatbarco.de/adduser.php?username=%s&userid=%d",
									userName, userID));
					URLConnection conn = sherlock.openConnection();
					conn.connect();
					BufferedReader in = new BufferedReader(
							new InputStreamReader(conn.getInputStream()));
					String line = in.readLine();

					bot.sendMessage(
							target,
							line.trim().substring(0,
									Math.min(80, line.length())));
				} catch (MalformedURLException e) {
					bot.logMessage(e.getMessage(), true);
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					bot.logMessage(e.getMessage(), true);
					e.printStackTrace();
				}
			}
		}
	}
}
