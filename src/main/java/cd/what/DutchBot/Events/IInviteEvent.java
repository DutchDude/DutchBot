package cd.what.DutchBot.Events;

/**
 * Invite events
 * 
 * @author DutchDude
 *
 */
public interface IInviteEvent {

	/**
	 * Get notified of an invite happening 
	 * 
	 * @param targetNick nick invited
	 * @param sourceNick nick invite originated from
	 * @param sourceLogin login of inviter
	 * @param sourceHostname hostname of inviter
	 * @param channel channel which targetNick got invited to
	 */
	void notifyInviteEvent(String targetNick, String sourceNick,
			String sourceLogin, String sourceHostname, String channel);

}
