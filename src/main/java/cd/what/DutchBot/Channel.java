/**
 * Manages channels
 */
package cd.what.DutchBot;

import java.lang.reflect.InvocationTargetException;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.User;

/**
 * @author DutchDude
 * 
 */
public class Channel {

	/**
	 * channel name
	 */
	private final String channelName;
	/**
	 * channel key
	 */
	private final String key;
	/**
	 * Do we need to use chanserv?
	 */
	private final boolean chanservInvite;
	/**
	 * Bot instance
	 */
	private final DutchBot bot;

	/**
	 * Channel topic
	 */
	private String topic;
	/**
	 * Module manager:
	 */
	private final ModuleManager modulemanager;

	/**
	 * Delegate to module manager of this class
	 * 
	 * @param channel
	 * @param kickerNick
	 * @param kickerLogin
	 * @param kickerHostname
	 * @param recipientNick
	 * @param reason
	 */
	public void notifyChannelKickEvent(String channel, String kickerNick,
			String kickerLogin, String kickerHostname, String recipientNick,
			String reason) {
		this.modulemanager.notifyChannelKickEvent(channel, kickerNick,
				kickerLogin, kickerHostname, recipientNick, reason);
	}

	/**
	 * Delegate to channel part event of module manager instance of this class
	 * 
	 * @param channel
	 * @param sender
	 * @param login
	 * @param hostname
	 * @see cd.what.DutchBot.ModuleManager#notifyPartEvent(java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public void notifyPartEvent(String channel, String sender, String login,
			String hostname) {
		this.modulemanager.notifyPartEvent(channel, sender, login, hostname);
	}

	/**
	 * Delegate to modulemanager of this class
	 * 
	 * @param channel
	 * @param sender
	 * @param login
	 * @param hostname
	 */
	public void notifyChannelJoinEvent(String channel, String sender,
			String login, String hostname) {
		this.modulemanager.notifyChannelJoinEvent(channel, sender, login,
				hostname);
	}

	/**
	 * Delegate to modulemanager
	 * 
	 * @param channel
	 * @param sender
	 * @param login
	 * @param hostname
	 * @param message
	 */
	public void notifyChannelMessageEvent(String channel, String sender,
			String login, String hostname, String message) {
		this.modulemanager.notifyChannelMessageEvent(channel, sender, login,
				hostname, message);
	}

	/**
	 * Delegate to modulemanager
	 * 
	 * @param module
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public void loadModule(String module) throws ClassNotFoundException,
			NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		modulemanager.loadModule(module);
	}

	public void notifyQuitEvent(String sourceNick, String sourceLogin,
			String sourceHostname, String reason) {
		this.modulemanager.notifyQuitEvent(sourceNick, sourceLogin,
				sourceHostname, reason);
	}

	private boolean joined = false;

	public Channel(DutchBot bot, String name) {
		this.bot = bot;
		modulemanager = new ModuleManager(bot);
		this.key = "";
		this.channelName = name;
		this.chanservInvite = false;
	}

	public Channel(DutchBot bot, String name, String key) {
		this.bot = bot;
		modulemanager = new ModuleManager(bot);
		this.key = key;
		this.channelName = name;
		this.chanservInvite = false;
	}

	public Channel(DutchBot bot, String name, String key, boolean chanservInvite) {
		modulemanager = new ModuleManager(bot);
		this.bot = bot;
		this.key = key;
		this.channelName = name;
		this.chanservInvite = chanservInvite;
	}

	public void join() {

		if (key != null) {
			if (!key.trim().isEmpty()) {
				this.bot.joinChannel(this.channelName, key);
			}
		}
		if (chanservInvite) {
			this.bot.logMessage("Trying to join " + channelName
					+ " by CS invite");
			this.bot.sendRawLine("CS invite " + this.channelName);
			this.bot.joinChannel(channelName);
		} else
			this.bot.joinChannel(channelName);

	}

	public void changeTopic(String topic) {
		this.bot.setTopic(channelName, topic);
	}

	public void processMessage(String sender, String login, String host,
			String message) {

	}

	public void hasJoined() {
		this.joined = true;
	}

	public User[] getUsers() {
		return this.bot.getUsers(this.channelName);
	}

	public User getUser(String nick) throws IrcException {
		if (!this.joined)
			throw new IrcException("Not joined to channel");
		for (User user : this.getUsers()) {
			if (user.getNick().equalsIgnoreCase(nick))
				return user;
		}
		return null;
	}

	@Override
	public String toString() {
		return this.channelName;
	}

	/**
	 * @return the topic
	 */
	public String getTopic() {
		return topic;
	}

	/**
	 * @param topic
	 *            the topic to set
	 */
	public void setTopic(String topic) {
		this.topic = topic;
	}

}
