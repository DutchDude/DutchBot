package cd.what.DutchBot.Events;

/**
 * Events listening for private messages implement this 
 * 
 * @author DutchDude
 *
 */
public interface IPrivateMessageEvent {

	/**
	 * Handle an private message
	 * 
	 * @param sender nick of person sending the pm
	 * @param login login of person sending the pm
	 * @param hostname hostname of person sending the pm
	 * @param message message sent
	 */
	void notifyPrivateMessageEvent(String sender, String login,
			String hostname, String message);

}
