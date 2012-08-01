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
