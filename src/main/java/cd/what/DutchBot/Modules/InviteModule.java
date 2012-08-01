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

import cd.what.DutchBot.AccessList;
import cd.what.DutchBot.DutchBot;
import cd.what.DutchBot.ModuleAbstract;
import cd.what.DutchBot.Privileges;
import cd.what.DutchBot.Events.IInviteEvent;

/**
 * Respond to invites
 * 
 * @author DutchDude
 *
 */
public class InviteModule extends ModuleAbstract implements IInviteEvent {

	/**
	 * Create a new instance
	 * 
	 * @param bot the bot instance
	 */
	public InviteModule(DutchBot bot) {
		super(bot);
	}

	/* (non-Javadoc)
	 * @see cd.what.DutchBot.Events.IInviteEvent#notifyInviteEvent(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void notifyInviteEvent(String targetNick, String sourceNick,
			String sourceLogin, String sourceHostname, String channel) {
		System.out.println("Invited to channel " + channel);
		System.out.println("The sourcehostname: " + sourceHostname);
		if ((targetNick.equals(this.getBot().getNick()) && AccessList
				.isAllowed(sourceLogin, sourceHostname, Privileges.OPERATOR))
				// also autojoin on chanserv invite
				|| (sourceLogin.equals("services") && sourceHostname
						.equals("what-network.net"))
				|| sourceHostname.equals("services.what-network.net")
				|| (sourceHostname.equals("god.what.cd") && sourceLogin
						.equals("god"))) {
			this.getBot().joinChannel(channel);
		} else if (targetNick.equals(this.getBot().getNick())) {
			System.out.println("Ignored invitation from " + sourceNick);
			this.getBot()
					.sendMessage(sourceNick,
							"Never accept an invitation from a stranger unless he gives you candy.");
			this.getBot().sendMessage(sourceNick, "   -- Linda Festa");
		}

	}

}
