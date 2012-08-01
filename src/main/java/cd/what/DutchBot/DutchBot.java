/*
 *  Dutchbot, a bot for What-Network.
 *  
 *  
 *  
 */
package cd.what.DutchBot;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;

/**
 * @author DutchDude
 * 
 */
public class DutchBot extends PircBot {

	/**
	 * @Override
	 */
	private static final String VERSION = "½";

	/**
	 * Owner of the bot
	 */
	private String owner;

	/**
	 * Log channel
	 */
	private String logchannel;

	/**
	 * contains the password for nickserv
	 */
	private String _nickservPassword;

	/**
	 * Timer Utility
	 */
	private Timer _timer = new Timer(true);

	/**
	 * Prefix for the commands
	 */
	private String _commandPrefix = "\\";

	/**
	 * connection protector task
	 */
	private final ConnectionProtectorTask _connectionProtector;

	/**
	 * the server to connect to
	 */
	private String _serverAddress;

	/**
	 * Port used to connect to server
	 */
	private int _ircPort = 6667;

	/**
	 * Server password to use on connect
	 */
	private String _serverPassword;

	/**
	 * Module manager
	 */
	private ModuleManager moduleManager;
	/**
	 * Config
	 */
	private final PropertiesConfiguration _config = new PropertiesConfiguration();

	/**
	 * Channels the bot is active in
	 */
	private final HashMap<String, Channel> _channelList = new HashMap<String, Channel>();

	private String[] droneChannels;

	/**
	 * Initializes bot.
	 * 
	 * @param name
	 *            Nickname used when connecting.
	 */
	public DutchBot(String configfile) {

		try {
			this._config.load(configfile);
			this._config.setAutoSave(true);
			this._config.setFileName(configfile);
			AccessList.loadFromConfig(configfile);
			AccessList.setBot(this);
		} catch (ConfigurationException e) {
			this.logMessage("There was an error with your config file! ", true);
			e.printStackTrace();
			System.exit(1);
		} catch (FileNotFoundException e) {
			this.logMessage("The config file could not be found! ", true);
			System.exit(1);
		}

		// initialize the finals
		this.setServerAddress(this._config.getString("server.host"));
		this.setIrcPort(this._config.getInt("server.port", 6667));
		this.setServerPassword(this._config.getString("server.password", ""));
		this.setNickservPassword(this._config.getString("irc.nickservpass", ""));
		this.setName(this._config.getString("irc.nick", "DutchBot"));
		this.setVersion("DutchBot " + VERSION + " by DutchDude");
		this.setLogin(this._config.getString("irc.nick", "DutchBot"));
		_connectionProtector = new ConnectionProtectorTask(this);
		this.getTimer().schedule(_connectionProtector, 1000L, 1000L);
		this.setOwner(this._config.getString("bot.owner", ""));
		this.setLogchannel(this._config.getString("bot.logchannel", ""));
		this.moduleManager = new ModuleManager(this);

	}

	/**
	 * Load the config files
	 * 
	 */
	private void loadConfig() {

		if (this._config.containsKey("irc.nick")
				&& !this.getNick().equals(
						this._config.getString("irc.nick", "")))
			this.changeNick(this._config.getString("irc.nick"));

		if (this._config.containsKey("bot.globalmodules"))
			for (Object m : this._config.getList("bot.globalmodules").toArray()) {

				String module = m.toString().substring(0, 1).toUpperCase()
						+ m.toString().substring(1) + "Module";
				try {
					this.moduleManager.loadModule(module);
				} catch (ClassNotFoundException | NoSuchMethodException
						| SecurityException | InstantiationException
						| IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					this.logMessage("loading global module " + m + " failed",
							true);
					this.logMessage(e.getMessage(), true);
					e.printStackTrace();
				}
			}

		if (this._config.containsKey("db.database")
				&& this._config.containsKey("db.username")
				&& this._config.containsKey("db.password"))
			DatabaseConnection.getInstance().connect(
					this._config.getString("db.host"),
					this._config.getString("db.database"),
					this._config.getString("db.username"),
					this._config.getString("db.password"));
		droneLogin();
		TimerTask tt = new TimerTask() {

			@Override
			public void run() {
				sendRawLine("HS ON");
				loadChannels();

			}

		};

		this.getTimer().schedule(tt, 3000);

	}

	/**
	 * Logs in with drone
	 */
	private void droneLogin() {
		if (this._config.containsKey("drone.username")
				&& this._config.containsKey("drone.password")
				&& !this._config.containsKey("drone.channels"))
			this.sendMessage("drone",
					"identify " + this._config.getString("drone.username")
							+ " " + this._config.getString("drone.password"));
		else if (this._config.containsKey("drone.username")
				&& this._config.containsKey("drone.password")
				&& this._config.containsKey("drone.channels")) {

			this.sendMessage(
					"drone",
					"enter "
							+ implodeArray(this._config
									.getStringArray("drone.channels"), ",")
							+ " " + this._config.getString("drone.username")
							+ " " + this._config.getString("drone.password"));

			this.droneChannels = this._config.getStringArray("drone.channels");
		}

	}

	/**
	 * Method to join array elements of type string
	 * 
	 * @author Hendrik Will, imwill.com
	 * @param inputArray
	 *            Array which contains strings
	 * @param glueString
	 *            String between each array element
	 * @return String containing all array elements seperated by glue string
	 */
	public static String implodeArray(String[] inputArray, String glueString) {

		/** Output variable */
		String output = "";

		if (inputArray.length > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append(inputArray[0]);

			for (int i = 1; i < inputArray.length; i++) {
				sb.append(glueString);
				sb.append(inputArray[i]);
			}

			output = sb.toString();
		}

		return output;
	}

	/**
	 * Load the channels from the config.
	 */
	private void loadChannels() {
		this._channelList.clear();

		// join all the channels configured
		Iterator<String> channels = this._config.getKeys("irc.channel");
		while (channels.hasNext()) {

			String channelname = channels.next().toLowerCase();

			// channelname uit de keys halen
			int end = channelname.lastIndexOf(".");
			if (end == -1 || end < "irc.channel.".length())
				end = channelname.length();
			channelname = channelname.substring("irc.channel.".length(), end);

			// als we hem al hebben, skippen
			if (this._channelList.containsKey("#" + channelname))
				continue;

			// andere keys ophalen voor nieuwe Channel instance
			String key = _config.getString("irc.channel." + channelname
					+ ".key", null);
			Boolean channelservInvite = _config.getBoolean("irc.channel."
					+ channelname + ".chanservinvite", false);
			List<?> modules = null;
			if (_config.containsKey("irc.channel." + channelname + ".modules")) {
				modules = _config.getList("irc.channel." + channelname
						+ ".modules");
			}

			channelname = "#" + channelname;
			this.logMessage("Joinging Channel: " + channelname);
			Channel chan = new Channel(this, channelname, key,
					channelservInvite);

			// add the modules for this channel
			if (modules != null) {
				for (Object mod : modules) {
					String name = (String) mod;
					name = name.substring(0, 1).toUpperCase()
							.concat(name.substring(1).toLowerCase())
							.concat("Module");
					try {
						chan.loadModule(name);
					} catch (ClassNotFoundException | NoSuchMethodException
							| SecurityException | InstantiationException
							| IllegalAccessException | IllegalArgumentException
							| InvocationTargetException e) {
						e.printStackTrace();
						this.logMessage("Messed up while loading module "
								+ name + " for channel " + channelname, true);
						this.logMessage(e.getMessage());
					}
				}
			}
			this.join(chan);

		}
		if (droneChannels != null) {
			for (String channel : this.droneChannels) {
				if (!_channelList.containsKey(channel)) {
					Channel chan = new Channel(this, channel);
					this.join(chan);
				}
				this.getChannel(channel).hasJoined();
			}
		}

	}

	public final void logMessage(String message) {
		this.logMessage(message, false);
	}

	/**
	 * Log an error
	 * 
	 * @param message
	 * @param error
	 */
	public final void logMessage(String message, boolean notice) {
		if (notice) {
			message = "**notice** " + message;
			if (!this.getLogchannel().isEmpty())
				this.sendMessage(this.getLogchannel(), message);
			if (!this.getOwner().isEmpty())
				this.sendMessage(this.getOwner(), message);
		}
		super.log("### " + message);
	}

	/**
	 * Try to connect to the server, return the result
	 * 
	 * @return Are we connected now?
	 * @throws IOException
	 * @throws IrcException
	 * @throws InterruptedException
	 * @throws ConfigurationException
	 */
	public final boolean tryConnect() throws IOException, IrcException,
			InterruptedException, ConfigurationException {
		this.moduleManager = new ModuleManager(this);
		this._channelList.clear();
		
		if (!this.isConnected()) {
			try {
				String nick = this.getName();
				this.setAutoNickChange(true);
				this.connect(this.getServerAddress(), this.getIrcPort(),
						this.getServerPassword());
				GhostTask gt = new GhostTask(this, nick);
				if (this.getNick() != nick) {
					this.getTimer().schedule(gt, 1000L);
				}
				this.changeNick(nick);
			} catch (NickAlreadyInUseException e) {
				this.logMessage("Nick already in use - switching nicks failed",
						true);
				return false;
			}
			this.identify(this.getNickservPassword());

		}

		loadConfig();
		return this.isConnected();
	}

	/**
	 * NickServ-GHOSTs a nick using the NickServ password
	 * 
	 * @param nick
	 */
	final void ghost(String nick) {
		this.sendRawLine("NickServ GHOST " + nick + " "
				+ this.getNickservPassword());
	}

	/**
	 * Runs on invite
	 */
	@Override
	protected void onInvite(String targetNick, String sourceNick,
			String sourceLogin, String sourceHostname, String channel) {

		this.moduleManager.notifyInviteEvent(targetNick, sourceNick,
				sourceLogin, sourceHostname, channel);
	}

	/**
	 * Runs on PM
	 */
	@Override
	protected void onPrivateMessage(String sender, String login,
			String hostname, String message) {
		this.moduleManager.notifyPrivateMessageEvent(sender, login, hostname,
				message);
	}

	/**
	 * Runs on message
	 */
	@Override
	protected void onMessage(String channel, String sender, String login,
			String hostname, String message) {
		// also treat "Dutchbot: <cmd>" lines as the command prefix.
		if (message.toLowerCase().startsWith(
				this.getNick().toLowerCase() + ": "))
			message = _commandPrefix
					+ message.substring((this.getNick() + ": ").length());
		else if (message.startsWith(this.getNick() + " "))
			message = _commandPrefix
					+ message.substring((this.getNick() + " ").length());

		this.moduleManager.notifyChannelMessageEvent(channel, sender, login,
				hostname, message);
		this.getChannel(channel).notifyChannelMessageEvent(channel, sender,
				login, hostname, message);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jibble.pircbot.PircBot#onQuit(java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	protected void onQuit(String sourceNick, String sourceLogin,
			String sourceHostname, String reason) {
		this.moduleManager.notifyQuitEvent(sourceNick, sourceLogin,
				sourceHostname, reason);
		for (Channel c : this._channelList.values()) {
			c.notifyQuitEvent(sourceNick, sourceLogin, sourceHostname, reason);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jibble.pircbot.PircBot#onTopic(java.lang.String,
	 * java.lang.String, java.lang.String, long, boolean)
	 */
	@Override
	protected void onTopic(String channel, String topic, String setBy,
			long date, boolean changed) {
		this.getChannel(channel).setTopic(topic);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jibble.pircbot.PircBot#onKick(java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	protected void onKick(String channel, String kickerNick,
			String kickerLogin, String kickerHostname, String recipientNick,
			String reason) {
		this.moduleManager.notifyChannelKickEvent(channel, kickerNick,
				kickerLogin, kickerHostname, recipientNick, reason);
		this.getChannel(channel).notifyChannelKickEvent(channel, kickerNick,
				kickerLogin, kickerHostname, recipientNick, reason);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jibble.pircbot.PircBot#onPart(java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	protected void onPart(String channel, String nick, String login,
			String hostname) {
		this.moduleManager.notifyPartEvent(channel, nick, login, hostname);
		this.getChannel(channel)
				.notifyPartEvent(channel, nick, login, hostname);
	}

	/**
	 * Join a channel
	 * 
	 * @param channel
	 */
	public void join(Channel channel) {
		_channelList.put(channel.toString().toLowerCase(), channel);
		channel.join();
	}

	/**
	 * join a channel
	 * 
	 * @param channel
	 */
	public void join(String channel) {
		channel = channel.toLowerCase();
		Channel chan = new Channel(this, channel);
		_channelList.put(channel, chan);
		chan.join();
	}

	/**
	 * Join a channel with a key
	 * 
	 * @param channel
	 * @param key
	 */
	public void join(String channel, String key) {
		channel = channel.toLowerCase();
		Channel chan = new Channel(this, channel, key);
		_channelList.put(channel, chan);
		chan.join();
	}

	/**
	 * Get the Channel instance for channel "channel"
	 * 
	 * @param channel
	 * @return
	 */
	public Channel getChannel(String channel) {
		channel = channel.toLowerCase();
		return this._channelList.get(channel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jibble.pircbot.PircBot#onJoin(java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void onJoin(String channel, String sender, String login,
			String hostname) {
		channel = channel.toLowerCase();
		if (this.getNick().equals(sender)) {
			if (this._channelList.containsKey(channel))
				this._channelList.get(channel).hasJoined();
			else {
				Channel chan = new Channel(this, channel);
				chan.hasJoined();
				this._channelList.put(channel, chan);
			}
		}

		this.moduleManager.notifyChannelJoinEvent(channel, sender, login,
				hostname);
		this.getChannel(channel).notifyChannelJoinEvent(channel, sender, login,
				hostname);

	}

	/**
	 * ALWAYS call in a separate thread from modules / InputThread
	 * 
	 * @param nick
	 * @return
	 */
	public Hostmask getHostmask(String nick) {
		whoisResult = null;
		this.sendRawLineViaQueue("WHOIS " + nick);
		if (whoisResult == null)
			try {
				whoisSemaphore.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return whoisResult;
	}

	/**
	 * Gets the nickserv password
	 * 
	 * @return String
	 */
	private final String getNickservPassword() {
		return _nickservPassword;
	}

	/**
	 * Sets the nickserv password
	 * 
	 * @param nickservPassword
	 */
	public final void setNickservPassword(String nickservPassword) {
		this._nickservPassword = nickservPassword;
	}

	/**
	 * @return the timer
	 */
	public Timer getTimer() {
		return _timer;
	}

	/**
	 * @param timer
	 *            the timer to set
	 */
	public void setTimer(Timer timer) {
		this._timer = timer;
	}

	/**
	 * Returns the command prefix
	 * 
	 * @return the commandPrefix
	 */
	public String getCommandPrefix() {
		return _commandPrefix;
	}

	/**
	 * Sets the command prefix
	 * 
	 * @param prefix
	 */
	public void setCommandPrefix(String prefix) {
		this._commandPrefix = prefix;
	}

	/**
	 * @return the serverAddress
	 */
	public String getServerAddress() {
		return _serverAddress;
	}

	/**
	 * @param serverAddress
	 *            the serverAddress to set
	 */
	public void setServerAddress(String serverAddress) {
		this._serverAddress = serverAddress;
	}

	/**
	 * @return the ircPort
	 */
	public int getIrcPort() {
		return _ircPort;
	}

	/**
	 * @param ircPort
	 *            the ircPort to set
	 */
	public void setIrcPort(int ircPort) {
		this._ircPort = ircPort;
	}

	/**
	 * 
	 * @param name
	 */
	public void setBotName(String name) {
		this.setName(name);

	}

	/**
	 * 
	 * @return the password used on connect
	 */
	public String getServerPassword() {
		return this._serverPassword;
	}

	/**
	 * sets the password used to connect to the server
	 * 
	 * @param password
	 */
	public void setServerPassword(String password) {
		this._serverPassword = password;
	}

	/**
	 * Returns the owner
	 * 
	 * @return
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * Sets the owner
	 * 
	 * @param owner
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * Gets the log channel
	 * 
	 * @return
	 */
	public String getLogchannel() {
		return logchannel;
	}

	/**
	 * Sets the log channel
	 * 
	 * @param logchannel
	 */
	public void setLogchannel(String logchannel) {
		this.logchannel = logchannel;
	}

	/**
	 * @return the moduleManager
	 */
	public ModuleManager getModuleManager() {
		return moduleManager;
	}

	/**
	 * 
	 * @return the config
	 */
	public PropertiesConfiguration getConfig() {
		return this._config;
	}

}
