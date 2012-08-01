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
import cd.what.DutchBot.Events.IChannelMessageEvent;
import cd.what.DutchBot.Events.IPrivateMessageEvent;

/**
 * Respond to join commands
 * 
 * @author DutchDude
 * 
 */
public class JoinModule extends ModuleAbstract implements IPrivateMessageEvent,
		IChannelMessageEvent {

	/**
	 * Create a new instance
	 * 
	 * @param bot
	 */
	public JoinModule(DutchBot bot) {
		super(bot);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cd.what.DutchBot.Events.IChannelMessageEvent#notifyChannelMessageEvent
	 * (java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void notifyChannelMessageEvent(String channel, String sender,
			String login, String hostname, String message) {
		final String joinCmd = this.getBot().getCommandPrefix() + "join";
		final String partCmd = bot.getCommandPrefix() + "part";
		if (message.toLowerCase().startsWith(joinCmd)) {
			String targetChannel = message.toLowerCase()
					.substring(joinCmd.length()).trim();
			this.joinCommand(channel, sender, login, hostname, targetChannel);
		} else if (message.toLowerCase().startsWith(partCmd)) {
			String targetChannel = message.toLowerCase()
					.substring(joinCmd.length()).trim();
			if(targetChannel.isEmpty()) targetChannel = channel;
			this.partCommand(channel, sender, login, hostname, targetChannel);
		}
		
		
		
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
		final String joinCmd = "join";
		final String partCmd = "part";
		if (message.toLowerCase().startsWith(joinCmd)) {
			String targetChannel = message.toLowerCase()
					.substring(joinCmd.length()).trim();
			this.joinCommand(sender, sender, login, hostname, targetChannel);
		} else if (message.toLowerCase().startsWith(partCmd)) {
			String targetChannel = message.toLowerCase()
					.substring(joinCmd.length()).trim();
			this.partCommand(sender, sender, login, hostname, targetChannel);
		}

	}

	/**
	 * Handle the join command 
	 * 
	 * @param target
	 * @param sender
	 * @param login
	 * @param hostname
	 * @param channel
	 */
	private void joinCommand(String target, String sender, String login,
			String hostname, String channel) {
		if (AccessList.isAllowed(login, hostname, Privileges.OPERATOR)) {
			if (channel.isEmpty()) {
				this.getBot().sendMessage(sender, "Usage: join <channel>");
				return;
			}

			this.getBot().join(channel);
			this.getBot().sendMessage(target, "Joined channel " + channel);
		}
	}


	/**
	 * Handle the part command
	 * 
	 * @param target
	 * @param sender
	 * @param login
	 * @param hostname
	 * @param targetChannel
	 */
	private void partCommand(String target, String sender, String login,
			String hostname, String targetChannel) {
		if (AccessList.isAllowed(login, hostname, Privileges.OPERATOR)) {
			if (targetChannel.isEmpty()) {
				this.getBot().sendMessage(sender, "Usage: part <channel>");
				return;
			}

			this.getBot().partChannel(targetChannel, "leaving because of " + sender);
			if (targetChannel != target)
				this.getBot().sendMessage(target, "left channel " + targetChannel);
		}
	}	
	
	
}
