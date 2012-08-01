/**
 * 
 * @author DutchDude
 */
package cd.what.DutchBot.Events;

/**
 * @author DutchDude
 * 
 */
public interface IQuitEvent {
	
	/**
	 * handle someone quitting 
	 * @param sourceNick nick of person quitting
	 * @param sourceLogin login of person quitting
	 * @param sourceHostname hostname of person quitting
	 * @param reason reason why they quit
	 */
	public void notifyQuitEvent(String sourceNick, String sourceLogin,
			String sourceHostname, String reason);
}
