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

import java.io.IOException;
import java.util.TimerTask;

import org.jibble.pircbot.IrcException;

/**
 * @author DutchDude
 * 
 */
public class ConnectionProtectorTask extends TimerTask {

	private final DutchBot bot;
	private boolean wasConnected;

	/**
     * 
     */
	public ConnectionProtectorTask(DutchBot bot) {
		this.bot = bot;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
		if (!this.bot.isConnected() && this.wasConnected) {
			System.out.println("Reconnecting...");
			try {
				bot.tryConnect();
			} catch (IrcException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (this.bot.isConnected()) {
			this.wasConnected = true;
		}

	}

}
