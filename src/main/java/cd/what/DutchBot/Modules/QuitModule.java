/**
 * 
 */
package cd.what.DutchBot.Modules;

import cd.what.DutchBot.AccessList;
import cd.what.DutchBot.DutchBot;
import cd.what.DutchBot.ModuleAbstract;
import cd.what.DutchBot.Privileges;
import cd.what.DutchBot.Events.IChannelMessageEvent;
import cd.what.DutchBot.Events.IPrivateMessageEvent;

/**
 * Module for the quit command
 * 
 * @author Thom
 * 
 */
public class QuitModule extends ModuleAbstract implements IChannelMessageEvent,
		IPrivateMessageEvent {

	/**
	 * Create a new instance
	 * 
	 * @param bot
	 */
	public QuitModule(DutchBot bot) {
		super(bot);
	}

	/**
	 * Initialize
	 * 
	 * @see cd.what.DutchBot.Modules.ModuleAbstract#init()
	 */
	@Override
	public void init() {
		return;
	}

	/**
	 * Notify this module of a channel event
	 */
	@Override
	public void notifyChannelMessageEvent(String channel, String sender,
			String login, String hostname, String message) {
		if (message.startsWith(this.getBot().getCommandPrefix() + "quit"))
			this.run(channel, sender, login, hostname, message);
		if(message.startsWith(this.bot.getCommandPrefix() + "restart")) 
			this.run(channel, sender, login, hostname, message.substring(1));
	}

	/**
	 * Voer de quit uit
	 * 
	 * @param source
	 * @param sender
	 * @param login
	 * @param hostname
	 * @param message
	 */
	private void run(String source, String sender, String login,
			String hostname, String message) {

		if (!AccessList.isAllowed(login, hostname, Privileges.OWNER)) {
			this.getBot().logMessage(sender + " tried \\quit");
			return;
		}
		if(message.startsWith("quit")) {
			this.getBot().quitServer("This bot was killed by: " + sender);
			System.out.println("Shutdown invoked by: " + sender + "!" + login + "@"
					+ hostname + " in " + source);
			System.exit(0);
		} else if (message.startsWith("restart")){
			this.getBot().quitServer("BRB restart -- " + sender);
			System.out.println("Shutdown invoked by: " + sender + "!" + login + "@"
					+ hostname + " in " + source);
			System.exit(1337);
		}
		
		return;

	}

	/**
	 * Notify of an private message
	 * 
	 * @see cd.what.DutchBot.Modules.IPrivateMessageEvent#notifyPrivateMessageEvent
	 *      (java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public void notifyPrivateMessageEvent(String sender, String login,
			String hostname, String message) {
		if (!message.startsWith("quit") || !message.startsWith("restart")
				|| !AccessList.isAllowed(login, hostname, Privileges.OWNER)) {
			return;
		}
		this.run(sender, sender, login, hostname, message);

	}
}
