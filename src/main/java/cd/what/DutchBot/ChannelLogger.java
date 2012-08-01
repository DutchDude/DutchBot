/**
 * 
 */
package cd.what.DutchBot;

/**
 * @author DutchDude
 * 
 */
public class ChannelLogger {
	private final Channel _channel;
	private String _logId;
	private boolean _active = false;

	public ChannelLogger(Channel channel) {
		this._channel = channel;
	}

	public void startLog(String id) {
		if (this.isActive())
			return;
		this.setActive(true);
		this._logId = id;
		System.out.println("### STARTED LOGGING " + id);
	}

	public void closeLog() {
		if (!this.isActive())
			return;
		this.setActive(false);
		System.out.println("#### STOPPED LOGGING " + this._logId);
	}

	public void writeLog(String sender, String login, String host,
			String message) {
		// TODO
		if (this.isActive())
			System.out.println("## LOG: <" + sender + ">" + message);
	}

	/**
	 * @return the _active
	 */
	public boolean isActive() {
		return _active;
	}

	/**
	 * @param _active
	 *            the _active to set
	 */
	private void setActive(boolean active) {
		this._active = active;
	}

}
