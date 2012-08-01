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
