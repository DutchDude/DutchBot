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
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cd.what.DutchBot.AccessList;
import cd.what.DutchBot.DatabaseConnection;
import cd.what.DutchBot.DutchBot;
import cd.what.DutchBot.ModuleAbstract;
import cd.what.DutchBot.Privileges;
import cd.what.DutchBot.Events.IChannelMessageEvent;


/**
 * Insult people
 * 
 * @author DutchDude
 * 
 */
public class InsultModule extends ModuleAbstract implements
		IChannelMessageEvent {

	

	private static final String GET_INSULT_QUERY = "SELECT insult FROM insults ORDER BY random() LIMIT 1";
	
	/**
	 * Ignore if in a channel with this access level
	 */
	private static final Privileges minNotIgnorePrivilege = Privileges.USER;
	
	/**
	 * These people may perform the commands
	 */
	private static final Privileges minUsePrivilege = Privileges.AUTHORIZED;
	
	/**
	 * Bad words. 
	 */
	private final ArrayList<String> badwords = new ArrayList<String>();
	/**
	 * @param bot
	 */
	public InsultModule(DutchBot bot) {
		super(bot);
		
		for (String s : new String[]{"!rippy","!clear", "!enable", "!disable", "!warn",
		"!whatuser -f", "!whatuser", "!whatemail", "!whatnick", ".np", ".compare"})
		{
			badwords.add(s);
		}
		badwords.add(bot.getOwner());
		badwords.add(bot.getOwner()+"_");
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
		// We still need to ignore certain people.
		if (!AccessList.isChannelAllowed(channel, minNotIgnorePrivilege))
			return;
		
		
		
		Matcher insultCommand = Pattern.compile(
				Pattern.quote(this.getBot().getCommandPrefix())
						+ "insult( \\S.*)?").matcher(message);

		if (insultCommand.matches()
				&& (AccessList
						.isAllowed(login, hostname, minUsePrivilege) || AccessList
						.isChannelAllowed(channel, minUsePrivilege))) {
			String insult = "";
			try {
				DatabaseConnection db = DatabaseConnection.getInstance();
				PreparedStatement st = db.getDb().prepareStatement(
						GET_INSULT_QUERY);
				ResultSet rs = st.executeQuery();

				if (rs.next()) {
					insult = rs.getString("insult");
				}
			} catch (SQLException e) {
				this.getBot().sendMessage(channel,
						"ERROR: failed to get insult from database");
				this.getBot().logMessage(e.getMessage(), true);
			}
			String target = sender;
			if (insultCommand.groupCount() == 1) {
				
				if (insultCommand.group(1) != null)
					target = insultCommand.group(1).trim();
				
				
				if(target.startsWith("!") || target.startsWith("."))
					target = sender;
				
				String[] words = message.trim().toLowerCase().split("\\s");
				for(String word : words) {
					if (badwords.contains(word)) {
						target = sender;
						break;
					}
				}
			}
			
			if (insult.contains("#"))
				insult = insult.replace("#", target);
			else
				insult = target + ": " + insult;

			this.getBot().sendMessage(channel, insult);
		}

	}
}
