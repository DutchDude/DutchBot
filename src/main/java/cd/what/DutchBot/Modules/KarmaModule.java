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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cd.what.DutchBot.AccessList;
import cd.what.DutchBot.DatabaseConnection;
import cd.what.DutchBot.DutchBot;
import cd.what.DutchBot.ModuleAbstract;
import cd.what.DutchBot.Privileges;
import cd.what.DutchBot.Events.IChannelMessageEvent;


/**
 * Karma! because everybody loves EPEEN
 * 
 * @author DutchDude
 * 
 */
public class KarmaModule extends ModuleAbstract implements IChannelMessageEvent {

	private static final String UPDATE_QUERY = "UPDATE karma SET value=value+? WHERE name = ?";
	private static final String SELECT_QUERY = "SELECT value FROM karma WHERE name = ?";
	private static final String INSERT_QUERY = "INSERT INTO karma VALUES (?, ?)";

	/**
	 * Initialize the module
	 * 
	 * @param bot
	 */
	public KarmaModule(DutchBot bot) {
		super(bot);

	}

	/**
	 * Notify for a channel message event
	 * 
	 * @see cd.what.DutchBot.Modules.IChannelMessageEvent#notifyChannelMessageEvent
	 *      (java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	@Override
	public void notifyChannelMessageEvent(String channel, String sender,
			String login, String hostname, String message) {

		String karmacmd = this.getBot().getCommandPrefix() + "karma ";
		int karmaValue = 1;

		if (AccessList.isAllowed(login, hostname, Privileges.USER)) {
			Matcher plusPlusMatcher = Pattern.compile(
					"(\\S+?)(:\\s)?\\s*\\+\\+.*").matcher(message);

			Matcher minMinMatcher = Pattern.compile("(\\S+?)(:\\s)?\\s*--.*")
					.matcher(message);

			if (plusPlusMatcher.matches()) {
				String item = plusPlusMatcher.group(1);
				if (item.equalsIgnoreCase(sender))
					karmaValue *= -1;
				crementKarma(item, karmaValue);
				return;
			} else if (minMinMatcher.matches()) {
				String item = minMinMatcher.group(1);
				crementKarma(item, -karmaValue);
				return;
			}

			if (message.toLowerCase().startsWith(karmacmd)) {
				String item = message.substring(karmacmd.length()).split(" ")[0]
						.trim();
				PreparedStatement ss;
				try {
					ss = DatabaseConnection.getInstance().getDb()
							.prepareStatement(SELECT_QUERY);
					ss.setString(1, item.toLowerCase());
					ResultSet rs = ss.executeQuery();
					int value = 0;

					if (rs.next()) {
						value = rs.getInt("value");
					}
					this.getBot().sendMessage(channel,
							"Karma: " + item + " has karma " + value);
				} catch (SQLException | NullPointerException e) {
					this.getBot().logMessage(
							"Error in preparing statement for KarmaModule! "
									+ e.getMessage(), true);
					e.printStackTrace();
				}

			}
		}
	}

	/**
	 * Increment or decrement item with delta
	 * 
	 * @param item
	 * @param delta
	 */
	private void crementKarma(String item, int delta) {
		if (Math.abs(delta) > 1)
			throw new IllegalArgumentException("Must be +1 or -1");
		try {
			PreparedStatement ss = DatabaseConnection.getInstance().getDb()
					.prepareStatement(SELECT_QUERY);
			PreparedStatement su = DatabaseConnection.getInstance().getDb()
					.prepareStatement(UPDATE_QUERY);
			ss.setString(1, item.toLowerCase());
			su.setString(2, item.toLowerCase());
			su.setInt(1, delta);

			ss.execute();

			if (ss.getResultSet().next())
				su.execute();
			else {
				PreparedStatement si = DatabaseConnection.getInstance().getDb()
						.prepareStatement(INSERT_QUERY);
				si.setString(1, item.toLowerCase());
				si.setInt(2, delta);
				si.execute();
				si.close();
			}
			ss.close();
			su.close();
		} catch (SQLException | NullPointerException e) {
			this.getBot().logMessage(
					"Error in preparing statement for KarmaModule! "
							+ e.getMessage(), true);
		}
	}
}
