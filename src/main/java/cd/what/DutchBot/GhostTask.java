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

import java.util.TimerTask;

/**
 * Ghosts a nick after an amount of time
 * 
 * @author DutchDude
 *
 */
class GhostTask extends TimerTask {
	private final DutchBot bot;
	private final String nick;

	GhostTask(DutchBot bot, String nick) {
		super();
		this.bot = bot;
		this.nick = nick;
	}

	@Override
	public void run() {
		if (nick != bot.getNick()) {
			bot.ghost(nick);
			bot.changeNick(nick);
			GhostTask gt = new GhostTask(bot, nick);
			this.bot.getTimer().schedule(gt, 1000L);
		}

	}
}
