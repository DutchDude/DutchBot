/**
 * 
 * @author DutchDude
 */
package cd.what.DutchBot.Events;

/**
 * Part events implement this interface
 * 
 * @author DutchDude
 * 
 */
public interface IPartEvent {
	
	/**
	 * Handle a part message
	 * 
	 * @param channel channel parted from
	 * @param sender nick of person parting
	 * @param login login of person parting
	 * @param hostname hostname of person parting
	 */
	public void notifyPartEvent(String channel, String sender, String login,
			String hostname);
}
