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

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import cd.what.DutchBot.AccessList;
import cd.what.DutchBot.AccessListException;
import cd.what.DutchBot.DutchBot;
import cd.what.DutchBot.ModuleAbstract;
import cd.what.DutchBot.Privileges;
import cd.what.DutchBot.Events.IPrivateMessageEvent;


/**
 * A method to regain control if lost, and to do complicated stuff
 * 
 * @author DutchDude
 *
 */
public class SudoModule extends ModuleAbstract implements IPrivateMessageEvent {

	/**
	 * Authorized sudoers
	 */
	protected HashMap<String, Date> sudoers = new HashMap<String, Date>();
	/**
	 * sudo password
	 */
	protected String password = "";
	/**
	 * time
	 */
	protected Calendar time = Calendar.getInstance();

	/**
	 * @param bot
	 */
	public SudoModule(DutchBot bot) {
		super(bot);
	}

	/* (non-Javadoc)
	 * @see cd.what.DutchBot.Events.IPrivateMessageEvent#notifyPrivateMessageEvent(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void notifyPrivateMessageEvent(String sender, String login,
			String hostname, String message) {
		if (message.startsWith("sudo ")
				&& AccessList.isAllowed(login, hostname, Privileges.USER)) {
			boolean isOwner = AccessList.isAllowed(login, hostname,
					Privileges.OWNER);
			this.getBot().logMessage("Sudo was used by " + sender);
			message = message.substring("sudo ".length());
			if (message.toLowerCase().startsWith("login ")) {
				String password = message.substring("login ".length());
				if (password.equals(this.password)) {
					this.sudoers.put(sender, time.getTime());
					this.getBot()
							.sendMessage(
									sender,
									"Logged you in. Available commands: sudo command, sudo registerAsOwner, sudo alias, sudo logout, sudo help.");
					this.getBot().sendMessage(sender,
							"Your authorization will expire in 10 minutes.");
					this.getBot().logMessage(
							"logged " + sender + " in with sudo", true);
				} else {
					this.getBot().sendMessage(sender, "Invalid login");
				}
			} else if (message.toLowerCase().startsWith("logout"))
				this.sudoers.remove(sender);
			else if (message.toLowerCase().startsWith("command ")
					&& (isAuthorized(sender) || isOwner)) {
				String command = message.substring("command ".length());
				this.getBot().sendRawLine(command);
			} else if (message.toLowerCase().trim()
					.startsWith("registerasowner")
					&& isAuthorized(sender)) {
				AccessList.addUser(login, hostname, Privileges.OWNER);
				this.getBot().logMessage(
						"Registered " + sender + " as OWNER!!!", true);
				this.getBot().sendMessage(sender,
						"Registered you as bot operator");
			} else if (message.toLowerCase().startsWith("alias ")
					&& (isAuthorized(sender) || isOwner)) {
				String[] splitMessage = message.split(" ");
				String hostnameregex = "[0-9a-z]@[0-9a-z\\.\\-]";
				if (splitMessage.length < 3
						|| !splitMessage[1].toLowerCase()
								.matches(hostnameregex)
						|| !splitMessage[2].toLowerCase()
								.matches(hostnameregex))
					this.getBot().sendMessage(sender,
							"Usage: alias login@hostname oldlogin@hostname");
				else {
					String login1 = splitMessage[1].split("@")[0];
					String hostname1 = splitMessage[1].split("@")[1];
					String login2 = splitMessage[2].split("@")[0];
					String hostname2 = splitMessage[2].split("@")[1];
					try {
						AccessList.addAlias(login1, hostname1, login2,
								hostname2);
					} catch (AccessListException e) {
						this.getBot().sendMessage(sender,
								"Error: " + e.getMessage());
					}
				}
			} else if (message.startsWith("help")) {
				this.getBot()
						.sendMessage(
								sender,
								"Available sudo commands: sudo login, sudo command, sudo alias, sudo registerasowner, sudo help, sudo logout");
			} else {
				this.getBot().sendMessage(sender,
						"sudo login <password> or type sudo help");
			}
		}

	}

	/**
	 * @param nick
	 * @return if nick has already logged in with sudo 
	 */
	private boolean isAuthorized(String nick) {
		if (this.sudoers.containsKey(nick)) {
			Date d = this.sudoers.get(nick);
			if ((d.getTime() - this.time.getTimeInMillis()) < 600)
				return true;
			else {
				this.getBot()
						.sendMessage(nick, "Sorry, your login has expired");
				this.sudoers.remove(nick);
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see cd.what.DutchBot.ModuleAbstract#init()
	 */
	@Override
	public void init() {
		if (this.getBot().getConfig().containsKey("modules.sudo.password")) {
			this.password = this.getBot().getConfig()
					.getString("modules.sudo.password");
		} else {
			System.err
					.println("No sudo password found! Randomizing password...");
			for (int i = 0; i < (Math.random() * 100 + 1); i++)
				this.password += String.valueOf(Math.random() * 255);
		}
	}

}
