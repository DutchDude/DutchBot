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
import cd.what.DutchBot.AccessListException;
import cd.what.DutchBot.DutchBot;
import cd.what.DutchBot.ModuleAbstract;
import cd.what.DutchBot.Privileges;
import cd.what.DutchBot.Events.IChannelMessageEvent;
import cd.what.DutchBot.Events.IPrivateMessageEvent;


/**
 * Advanced ACL commands
 * 
 * @author DutchDude
 * 
 */
public class AclModule extends ModuleAbstract implements IChannelMessageEvent,
		IPrivateMessageEvent {

	private final String ACL_COMMAND = "acl ";

	/**
	 * @param bot
	 */
	public AclModule(DutchBot bot) {
		super(bot);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cd.what.DutchBot.Modules.IPrivateMessageEvent#
	 * notifyPrivateMessageEvent (java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void notifyPrivateMessageEvent(String sender, String login,
			String hostname, String message) {
		message = message.toLowerCase();

		if (message.startsWith(this.ACL_COMMAND)) {
			message = message.substring(this.ACL_COMMAND.length());
			this.run(sender, sender, login, hostname, message);
		}

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
		message = message.toLowerCase();
		if (message.startsWith(this.getBot().getCommandPrefix()
				.concat(ACL_COMMAND))) {
			message = message.substring(this.getBot().getCommandPrefix()
					.concat(ACL_COMMAND).length());
			this.run(channel, sender, login, hostname, message);
		}

	}

	/**
	 * Validates user privileges and runs the command
	 * 
	 * @param target
	 *            output target
	 * @param sender
	 *            sender of the command
	 * @param login
	 *            ident of the sender
	 * @param hostname
	 *            hostname of the sender
	 * @param message
	 *            message sent
	 */
	private void run(String target, String sender, String login,
			String hostname, String message) {

		Matcher authorizeMatcher = Pattern.compile(
				"authorize \\s*(\\S+)@(\\S+) \\s*(\\S+)( .*)?")
				.matcher(message);
		Matcher aliasMatcher = Pattern.compile(
				"alias \\s*(\\S+)@(\\S+) \\s*(\\S+)@(\\S+)( .*)?").matcher(
				message);
		Matcher deleteMatcher = Pattern.compile("delete (\\S+)@(\\S+)( .*)?")
				.matcher(message);

		if (authorizeMatcher.matches()
				&& AccessList.isAllowed(login, hostname, Privileges.OPERATOR)) {

			String targetLogin = authorizeMatcher.group(1);
			String targetHostname = authorizeMatcher.group(2);
			String targetPrivileges = authorizeMatcher.group(3);

			for (Privileges priv : Privileges.values()) {
				if (priv.toString().equalsIgnoreCase(targetPrivileges)
						&& AccessList.isAllowed(login, hostname, priv) 
						&& !AccessList.isAllowed(targetLogin, targetHostname, Privileges.OWNER)) {
					AccessList.addUser(targetLogin, targetHostname, priv);
					this.getBot().sendMessage(
							target,
							"Added " + targetLogin + "@" + targetHostname
									+ " to the ACL with privileges "
									+ targetPrivileges);
					break;
				}
			}
		} else if (aliasMatcher.matches()
				&& AccessList.isAllowed(login, hostname, Privileges.OPERATOR)) {
			String originalLogin = aliasMatcher.group(1);
			String originalHostname = aliasMatcher.group(2);
			String targetLogin = aliasMatcher.group(3);
			String targetHostname = aliasMatcher.group(4);
			try {
				AccessList.addAlias(targetLogin, targetHostname, originalLogin,
						originalHostname);
				this.getBot().sendMessage(target, "added alias");
			} catch (AccessListException e) {
				this.getBot().sendMessage(target, "Error: " + e.getMessage());
				this.getBot().logMessage(e.getMessage(), true);
			}
		} else if (deleteMatcher.matches()
				&& AccessList.isAllowed(login, hostname, Privileges.OPERATOR)) {
			try {
				AccessList.delUser(deleteMatcher.group(1),
						deleteMatcher.group(2));
			} catch (AccessListException e) {
				this.getBot().sendMessage(target, "Deletion failed");
			}
			this.getBot().sendMessage(target, "deleted user");
		} else if (AccessList.isAllowed(login, hostname, Privileges.OPERATOR)) {
			this.getBot()
					.sendMessage(target,
							"Did not recognize command. Try authorize, alias or delete");
		} else {
			this.bot.kick(target, sender, "NO");
		}

	}
}
