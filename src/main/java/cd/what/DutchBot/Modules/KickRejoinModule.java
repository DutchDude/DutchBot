package cd.what.DutchBot.Modules;

import java.util.TimerTask;

import cd.what.DutchBot.AccessList;
import cd.what.DutchBot.DutchBot;
import cd.what.DutchBot.ModuleAbstract;
import cd.what.DutchBot.Privileges;
import cd.what.DutchBot.Events.IChannelKickEvent;

/**
 * Meanieprotection
 * 
 * @author DutchDude
 *
 */
public class KickRejoinModule extends ModuleAbstract implements
		IChannelKickEvent {

	/**
	 * @param bot
	 */
	public KickRejoinModule(DutchBot bot) {
		super(bot);

	}
	
	/* (non-Javadoc)
	 * @see cd.what.DutchBot.Events.IChannelKickEvent#notifyChannelKickEvent(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void notifyChannelKickEvent(String channel, String kickerNick,
			String kickerLogin, String kickerHostname, String recipientNick,
			String reason) {
		
		if(AccessList.isAllowed(kickerLogin, kickerHostname, Privileges.OPERATOR))
			return;
		
		if (this.getBot().getNick().equals(recipientNick)) {
			bot.logMessage("Got kicked from "+ channel + ", rejoining....", true);
			RejoinTask t = new RejoinTask(channel, this.getBot());
			this.getBot().getTimer().schedule(t, 0, 10000);
		}

	}

	/**
	 * Try to rejoin the channel max. 10 times
	 * 
	 * @author DutchDude
	 *
	 */
	class RejoinTask extends TimerTask {

		private final DutchBot bot;
		private final String channel;
		private int tries = 10;

		RejoinTask(String channel, DutchBot bot) {
			this.channel = channel;
			this.bot = bot;
		}

		@Override
		public void run() {

			this.bot.getChannel(this.channel).join();
			this.tries--;
			if (this.tries <= 0) {
				bot.logMessage("Failed to rejoin " + channel );
				this.cancel();
				
			}
		}
	}


}
