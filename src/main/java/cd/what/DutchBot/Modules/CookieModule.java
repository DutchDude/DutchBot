/**
 * This file is part of DutchBot.
 *
 * DutchBot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * DutchBot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DutchBot.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author DutchDude
 * @copyright © 2012, DutchDude
 * 
 * You are encouraged to send any changes you make to this code to the
 * author. See http://github.com/DutchDude/DutchBot.git
 */
package cd.what.DutchBot.Modules;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cd.what.DutchBot.AccessList;
import cd.what.DutchBot.DutchBot;
import cd.what.DutchBot.ModuleAbstract;
import cd.what.DutchBot.Privileges;
import cd.what.DutchBot.Events.IChannelMessageEvent;


/**
 * Hand out cookies. Because everybody loves cookies
 * 
 * @author DutchDude
 * 
 */
public class CookieModule extends ModuleAbstract implements
		IChannelMessageEvent {

	/**
	 * Minimum access needed
	 */
	private static final Privileges MINIMUM_ACCESS = Privileges.USER;

	/**
	 * @param bot
	 */
	public CookieModule(DutchBot bot) {
		super(bot);
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

		Matcher cookieCommand = Pattern.compile(
				Pattern.quote(this.getBot().getCommandPrefix())
						+ "cookie( \\s*(\\S.*))?", Pattern.CASE_INSENSITIVE)
				.matcher(message);
		Matcher giveCommand = Pattern.compile(
				Pattern.quote(this.getBot().getCommandPrefix())
						+ "give \\s*(\\S+) \\s*(\\S.*)",
				Pattern.CASE_INSENSITIVE).matcher(message);

		if (cookieCommand.matches()
				&& AccessList.isAllowed(login, hostname, MINIMUM_ACCESS)) {
			if (cookieCommand.groupCount() == 2
					&& cookieCommand.group(2) != null)
				this.getBot().sendAction(channel,
						"gives " + cookieCommand.group(2).trim() + " a cookie");
			else
				this.getBot().sendAction(channel,
						"gives " + sender + " a cookie");
		} else if (giveCommand.matches()
				&& AccessList.isAllowed(login, hostname, MINIMUM_ACCESS)) {
			String user = giveCommand.group(1);
			String thing = giveCommand.group(2).trim();
			this.getBot().sendAction(channel, "gives " + user + " " + thing);
		} else if (message
				.startsWith(this.getBot().getCommandPrefix() + "give")
				&& AccessList.isAllowed(login, hostname, MINIMUM_ACCESS))
			this.getBot().sendNotice(sender, "Usage: give <user> <things>");
		else if (message
				.startsWith(this.getBot().getCommandPrefix() + "cookie")
				&& AccessList.isAllowed(login, hostname, MINIMUM_ACCESS))
			this.getBot().sendNotice(sender, "Usage: cookie [user]");
	}
}
