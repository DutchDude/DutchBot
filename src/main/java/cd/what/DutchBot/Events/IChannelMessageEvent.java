package cd.what.DutchBot.Events;

/**
 * Events called on channel messages
 * 
 * @author DutchDude
 *
 */
public interface IChannelMessageEvent {
	
	/**
	 * Notify of a channel message
	 * 
	 * @param channel channel message was sent to
	 * @param sender nick of sender
	 * @param login login of sender
	 * @param hostname hostname of sender
	 * @param message message that was sent
	 */
	public void notifyChannelMessageEvent(String channel, String sender,
			String login, String hostname, String message);
}
