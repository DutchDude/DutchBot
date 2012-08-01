package cd.what.DutchBot.Events;

/**
 * Event called on channel joins
 * 
 * @author DutchDude
 *
 */
public interface IChannelJoinEvent {

	/**
	 * Handle the channel join event.
	 * 
	 * @param channel channel event happened
	 * @param sender nick of person joined
	 * @param login login of person joined
	 * @param hostname hostname of person joined
	 */
	void notifyChannelJoinEvent(String channel, String sender, String login,
			String hostname);
}
