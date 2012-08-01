package cd.what.DutchBot.Events;

/**
 * Event called on channel kicks
 * 
 * @author DutchDude
 *
 */
public interface IChannelKickEvent {

	/**
	 * Notifies of a channel kick
	 * 
	 * @param channel channel in which it happened
	 * @param kickerNick nick of person kicking 
	 * @param kickerLogin login of person kicking 
	 * @param kickerHostname hostname of person kicking 
	 * @param recipientNick nick being kicked
	 * @param reason kick reason
	 */
	void notifyChannelKickEvent(String channel, String kickerNick,
			String kickerLogin, String kickerHostname, String recipientNick,
			String reason);

}
