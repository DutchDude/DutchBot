/**
 * This file is part of DutchBot.
 *
 * DutchBot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * DutchBot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DutchBot.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author DutchDude
 * @copyright © 2012, DutchDude
 * 
 * You are encouraged to send any changes you make to this code to the
 * author. See http://github.com/DutchDude/DutchBot.git
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
